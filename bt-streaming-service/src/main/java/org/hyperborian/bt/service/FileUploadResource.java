package org.hyperborian.bt.service;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hyperborian.bt.service.api.seed.GetAddTorrent;
import org.hyperborian.bt.service.api.seed.RpcResponseError;

import com.google.gson.Gson;
import com.turn.ttorrent.common.TorrentMetadata;
import com.turn.ttorrent.common.TorrentParser;
import com.turn.ttorrent.common.creation.MetadataBuilder;

import bt.runtime.BtClient;

/**
 * 
 * @author geekmj Single File and Multiple Files upload example
 */
@Path("/upload")
public class FileUploadResource {
	
	private Gson gson;
	
	public FileUploadResource() {
		gson = new Gson();
	}
	
	public static List<String> PUBLIC_TRACKERS = Arrays.asList(
			"udp://tracker.coppersurfer.tk:6969\n",
			"udp://tracker.opentrackr.org:1337/announce"
			);
	
	public static Map<String, FileUploadResource> saveFileThreads  = new HashMap<String, FileUploadResource>();
	
	private String id;

	private String torrentFileId;

	public String getId() {
		return id;
	}
	
	public String getTorrentFileId() {
		return torrentFileId;
	}

	@Path("/files")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA+";charset=utf-8")
	@Produces({MediaType.TEXT_HTML})
	public Response createTorrent(@DefaultValue("") @FormDataParam("tags") String tags,
			@FormDataParam("files") List<FormDataBodyPart> bodyParts,
			@FormDataParam("files") FormDataContentDisposition fileDispositions) throws IOException {

		if(saveFileThreads.size()>10) {
			 return Response.status(Status.SERVICE_UNAVAILABLE).entity("Service is over capacity").build();
		}
		this.id = getRandomHexString(12);
		StringBuffer fileDetails = new StringBuffer("");

		/* Save multiple files */
		MetadataBuilder builder = new MetadataBuilder();
		java.nio.file.Path path = FileSystems.getDefault().getPath(TorrentSessionStateService.getDownloadPath());
		java.nio.file.Path tmpPath = path.resolve(id);
		long dataSize = 0;
		for (int i = 0; i < bodyParts.size(); i++) {
			/*
			 * Casting FormDataBodyPart to BodyPartEntity, which can give us
			 * InputStream for uploaded file
			 */
			BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyParts.get(i).getEntity();
			String fileName = new String(bodyParts.get(i).getContentDisposition().getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
			if(!Files.exists(path)) {
				Files.createDirectories(path);
			}
			java.nio.file.Path tmp = Files.createDirectories(tmpPath).resolve(fileName);
			
			long size = saveFile(bodyPartEntity.getInputStream(), tmp);
			dataSize+=size;
			fileDetails.append(" File saved at "+TorrentSessionStateService.getDownloadPath() + fileName);
			builder.addFile(tmp.toFile());
		}
		builder.doPublic();
		builder.setCreationTime(new Date().getTime());
		builder.setCreatedBy("https://www.hyperborian.org");
		//builder.addTrackers(PUBLIC_TRACKERS);
		byte[] torrentBinary = builder.buildBinary();
		TorrentParser parser = new TorrentParser();
		TorrentMetadata metadata = parser.parse(torrentBinary);
		FileUploadResource object = saveFileThreads.get(metadata.getHexInfoHash());
		if(object!=null) {
			FileUtils.deleteDirectory(tmpPath.toFile());
			return Response.status(Response.Status.CREATED).header("Location", TorrentSessionStateService.getBaseUrl()+"/"+object.getId()+"/"+object.getTorrentFileId()+".torrent").header("Access-Control-Expose-Headers", "Location").build();
		}
		String torrentBinaryBase64 = Base64.getEncoder().encodeToString(torrentBinary);
		GetAddTorrent addTorrent = new GetAddTorrent(this.id);
		addTorrent.addParams(torrentBinaryBase64);
		addTorrent.addParams(new ArrayList<Object>());
		Map<String, String> options = new HashMap<String, String>();
		options.put("dir", path.resolve(id).toString());
		addTorrent.addParams(options);
		String addTorrentRequest = gson.toJson(addTorrent, GetAddTorrent.class);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(TorrentSessionStateService.getSeedingApiUrl());
		String encoding = Base64.getEncoder().encodeToString(("vadym:basebase").getBytes(Charset.forName("UTF-8")));
		request.setHeader("Authorization", "Basic " + encoding);
		StringEntity params = new StringEntity(addTorrentRequest, "utf-8");
		Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
		params.setContentType(header);
		params.setContentEncoding("UTF-8");
		request.setEntity(params);
		HttpResponse response = httpClient.execute(request);
		HttpEntity entity = response.getEntity();
		String responseStatus = EntityUtils.toString(entity, "UTF-8");
		
		if (response.getStatusLine().getStatusCode() > 300) {
			RpcResponseError responseError = gson.fromJson(responseStatus, RpcResponseError.class);
			String message = responseError.getError().getMessage();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
		}
		this.torrentFileId = DigestUtils.sha1Hex(torrentBinary);
		saveFileThreads.put(metadata.getHexInfoHash(), this);
		/**
		 * insert into DB
		 */
		
		return Response.status(Response.Status.OK).entity(responseStatus).
				header("Location", TorrentSessionStateService.getBaseUrl()+"/"+id+"/"+torrentFileId+".torrent").
				header("Content-InfoHash", metadata.getHexInfoHash()).
				header("Data-Length", dataSize).
				header("Access-Control-Expose-Headers", "Location").build();
	}
	
	public static String getMagnetLink(String infoHash) throws UnsupportedEncodingException {
		return "magnet:?xt=urn:btih:"+infoHash;
	}
	
	private BtClient client;
	
	public BtClient getClient() {
		return client;
	}

	private long saveFile(InputStream inputStream, java.nio.file.Path path) throws IOException {
	    OutputStream outStream = new FileOutputStream(path.toFile());
	    long dataSize = 0;
	    byte[] buffer = new byte[8 * 1024];
	    int bytesRead;
	    while ((bytesRead = inputStream.read(buffer)) != -1) {
	    	dataSize+=bytesRead;
	        outStream.write(buffer, 0, bytesRead);
	    }
	    IOUtils.closeQuietly(inputStream);
	    IOUtils.closeQuietly(outStream);
	    return dataSize;
	}
	
	private String getRandomHexString(int numchars){
        SecureRandom r = new SecureRandom();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < numchars){
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }
	
}