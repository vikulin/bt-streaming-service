package org.hyperborian.bt.service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
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

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hyperborian.bt.pojo.Torrent;

import com.turn.ttorrent.common.TorrentMetadata;
import com.turn.ttorrent.common.TorrentParser;
import com.turn.ttorrent.common.creation.MetadataBuilder;

import bt.StandaloneClientBuilder;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.runtime.BtClient;

/**
 * 
 * @author geekmj Single File and Multiple Files upload example
 */
@Path("/upload")
public class FileUploadResource {
	
	public static List<String> PUBLIC_TRACKERS = Arrays.asList(
			
			"udp://public.popcorn-tracker.org:6969/announce",
			"http://182.176.139.129:6969/announce",
			"http://5.79.83.193:2710/announce",
			"http://91.218.230.81:6969/announce",
			"udp://tracker.ilibr.org:80/announce",
			"http://atrack.pow7.com/announce",
			"http://bt.henbt.com:2710/announce",
			"http://mgtracker.org:2710/announce",
			"http://mgtracker.org:6969/announce",
			"http://open.touki.ru/announce.php",
			"http://p4p.arenabg.ch:1337/announce",
			"http://pow7.com:80/announce",
			"http://retracker.krs-ix.ru:80/announce",
			"http://secure.pow7.com/announce",
			"http://t1.pow7.com/announce",
			"http://t2.pow7.com/announce",
			"http://thetracker.org:80/announce",
			"http://torrentsmd.com:8080/announce",
			"http://tracker.bittor.pw:1337/announce",
			"http://tracker.dutchtracking.com:80/announce",
			"http://tracker.dutchtracking.nl:80/announce",
			"http://tracker.edoardocolombo.eu:6969/announce",
			"http://tracker.ex.ua:80/announce",
			"http://tracker.kicks-ass.net:80/announce",
			"http://tracker1.wasabii.com.tw:6969/announce",
			"http://tracker2.itzmx.com:6961/announce",
			"http://www.wareztorrent.com:80/announce",
			"udp://62.138.0.158:6969/announce",
			"udp://eddie4.nl:6969/announce",
			"udp://explodie.org:6969/announce",
			"udp://shadowshq.eddie4.nl:6969/announce",
			"udp://shadowshq.yi.org:6969/announce",
			"udp://tracker.eddie4.nl:6969/announce",
			"udp://tracker.mg64.net:2710/announce",
			"udp://tracker.sktorrent.net:6969",
			"udp://tracker2.indowebster.com:6969/announce",
			"udp://tracker4.piratux.com:6969/announce",
			"http://atrack.pow7.com/announce",
			"http://bt.henbt.com:2710/announce",
			"http://mgtracker.org:2710/announce",
			"http://mgtracker.org:6969/announce",
			"http://open.touki.ru/announce.php",
			"http://p4p.arenabg.ch:1337/announce",
			"http://pow7.com:80/announce",
			"http://retracker.krs-ix.ru:80/announce",
			"http://secure.pow7.com/announce",
			"http://t1.pow7.com/announce",
			"http://t2.pow7.com/announce",
			"http://thetracker.org:80/announce",
			"http://torrentsmd.com:8080/announce",
			"http://tracker.bittor.pw:1337/announce",
			"http://tracker.dutchtracking.com/announce",
			"http://tracker.dutchtracking.com:80/announce",
			"http://tracker.dutchtracking.nl:80/announce",
			"http://tracker.edoardocolombo.eu:6969/announce",
			"http://tracker.ex.ua:80/announce",
			"http://tracker.kicks-ass.net:80/announce",
			"http://tracker.mg64.net:6881/announce",
			"http://tracker.tfile.me/announce",
			"http://tracker1.wasabii.com.tw:6969/announce",
			"http://tracker2.itzmx.com:6961/announce",
			"http://tracker2.wasabii.com.tw:6969/announce",
			"http://www.wareztorrent.com:80/announce",
			"udp://bt.xxx-tracker.com:2710/announce",
			"udp://eddie4.nl:6969/announce",
			"udp://shadowshq.eddie4.nl:6969/announce",
			"udp://shadowshq.yi.org:6969/announce",
			"udp://tracker.eddie4.nl:6969/announce",
			"udp://tracker.mg64.net:2710/announce",
			"udp://tracker.mg64.net:6969/announce",
			"udp://tracker.opentrackr.org:1337/announce",
			"udp://tracker.sktorrent.net:6969",
			"udp://tracker2.indowebster.com:6969/announce",
			"udp://tracker4.piratux.com:6969/announce",
			"udp://tracker.coppersurfer.tk:6969/announce",
			"http://tracker.opentrackr.org:1337/announce",
			"udp://zer0day.ch:1337/announce",
			"udp://zer0day.to:1337/announce",
			"http://explodie.org:6969/announce",
			"udp://tracker.leechers-paradise.org:6969/announce",
			"udp://9.rarbg.com:2710/announce",
			"udp://9.rarbg.me:2780/announce",
			"udp://9.rarbg.to:2730/announce",
			"udp://p4p.arenabg.com:1337/announce",
			"udp://tracker.sktorrent.net:6969/announce",
			"http://p4p.arenabg.com:1337/announce",
			"udp://tracker.aletorrenty.pl:2710/announce",
			"http://tracker.aletorrenty.pl:2710/announce",
			"http://tracker.bittorrent.am/announce",
			"udp://tracker.kicks-ass.net:80/announce",
			"http://tracker.kicks-ass.net/announce",
			"http://tracker.baravik.org:6970/announce",
			"udp://torrent.gresille.org:80/announce",
			"http://torrent.gresille.org/announce",
			"http://tracker.skyts.net:6969/announce",
			"http://tracker.internetwarriors.net:1337/announce",
			"udp://tracker.skyts.net:6969/announce",
			"http://tracker.dutchtracking.nl/announce",
			"udp://tracker.yoshi210.com:6969/announce",
			"udp://tracker.tiny-vps.com:6969/announce",
			"udp://tracker.internetwarriors.net:1337/announce",
			"udp://mgtracker.org:2710/announce",
			"http://tracker.yoshi210.com:6969/announce",
			"http://tracker.tiny-vps.com:6969/announce",
			"udp://tracker.filetracker.pl:8089/announce",
			"udp://tracker.ex.ua:80/announce",
			"udp://91.218.230.81:6969/announce",
			"https://www.wareztorrent.com/announce",
			"http://www.wareztorrent.com/announce",
			"http://tracker.filetracker.pl:8089/announce",
			"http://tracker.ex.ua/announce",
			"http://tracker.calculate.ru:6969/announce",
			"udp://tracker.grepler.com:6969/announce",
			"udp://tracker.flashtorrents.org:6969/announce",
			"udp://tracker.bittor.pw:1337/announce",
			"http://tracker.tvunderground.org.ru:3218/announce",
			"http://tracker.grepler.com:6969/announce",
			"http://tracker.flashtorrents.org:6969/announce",
			"http://retracker.gorcomnet.ru/announce",
			"http://bt.pusacg.org:8080/announce",
			"http://87.248.186.252:8080/announce",
			"udp://tracker.kuroy.me:5944/announce",
			"udp://182.176.139.129:6969/announce",
			"http://tracker.kuroy.me:5944/announce",
			"http://retracker.krs-ix.ru/announce",
			"http://open.acgtracker.com:1096/announce",
			"udp://open.stealth.si:80/announce",
			"udp://208.67.16.113:8000/announce",
			"http://tracker.dler.org:6969/announce",
			"http://bt2.careland.com.cn:6969/announce",
			"http://open.lolicon.eu:7777/announce",
			"http://tracker.opentrackr.org:1337/announce",
			"http://explodie.org:6969/announce",
			"http://p4p.arenabg.com:1337/announce",
			"http://tracker.aletorrenty.pl:2710/announce",
			"http://tracker.bittorrent.am/announce",
			"http://tracker.kicks-ass.net/announce",
			"http://tracker.baravik.org:6970/announce",
			"http://torrent.gresille.org/announce",
			"http://tracker.skyts.net:6969/announce",
			"http://tracker.internetwarriors.net:1337/announce",
			"http://tracker.dutchtracking.nl/announce",
			"http://tracker.yoshi210.com:6969/announce",
			"http://tracker.tiny-vps.com:6969/announce",
			"http://www.wareztorrent.com/announce",
			"http://tracker.filetracker.pl:8089/announce",
			"http://tracker.ex.ua/announce",
			"http://tracker.calculate.ru:6969/announce",
			"http://tracker.tvunderground.org.ru:3218/announce",
			"http://tracker.grepler.com:6969/announce",
			"http://tracker.flashtorrents.org:6969/announce",
			"http://retracker.gorcomnet.ru/announce",
			"http://bt.pusacg.org:8080/announce",
			"http://87.248.186.252:8080/announce",
			"http://tracker.kuroy.me:5944/announce",
			"http://retracker.krs-ix.ru/announce",
			"http://open.acgtracker.com:1096/announce",
			"http://bt2.careland.com.cn:6969/announce",
			"http://open.lolicon.eu:7777/announce",
			"https://www.wareztorrent.com/announce",
			"udp://213.163.67.56:1337/announce",
			"http://213.163.67.56:1337/announce",
			"udp://185.86.149.205:1337/announce",
			"http://74.82.52.209:6969/announce",
			"udp://94.23.183.33:6969/announce",
			"udp://74.82.52.209:6969/announce",
			"udp://151.80.120.114:2710/announce",
			"udp://109.121.134.121:1337/announce",
			"udp://168.235.67.63:6969/announce",
			"http://109.121.134.121:1337/announce",
			"udp://178.33.73.26:2710/announce",
			"http://178.33.73.26:2710/announce",
			"http://85.17.19.180/announce",
			"udp://85.17.19.180:80/announce",
			"http://210.244.71.25:6969/announce",
			"http://85.17.19.180/announce",
			"http://213.159.215.198:6970/announce",
			"udp://191.101.229.236:1337/announce",
			"http://178.175.143.27/announce",
			"udp://89.234.156.205:80/announce",
			"http://91.216.110.47/announce",
			"http://114.55.113.60:6969/announce",
			"http://195.123.209.37:1337/announce",
			"udp://114.55.113.60:6969/announce",
			"http://210.244.71.26:6969/announce",
			"udp://107.150.14.110:6969/announce",
			"udp://5.79.249.77:6969/announce",
			"udp://195.123.209.37:1337/announce",
			"udp://37.19.5.155:2710/announce",
			"http://107.150.14.110:6969/announce",
			"http://5.79.249.77:6969/announce",
			"udp://185.5.97.139:8089/announce",
			"udp://194.106.216.222:80/announce",
			"udp://91.218.230.81:6969/announce",
			"https://104.28.17.69/announce",
			"http://104.28.16.69/announce",
			"http://185.5.97.139:8089/announce",
			"http://194.106.216.222/announce",
			"http://80.246.243.18:6969/announce",
			"http://37.19.5.139:6969/announce",
			"udp://5.79.83.193:6969/announce",
			"udp://46.4.109.148:6969/announce",
			"udp://51.254.244.161:6969/announce",
			"udp://188.165.253.109:1337/announce",
			"http://91.217.91.21:3218/announce",
			"http://37.19.5.155:6881/announce",
			"http://46.4.109.148:6969/announce",
			"http://51.254.244.161:6969/announce",
			"http://104.28.1.30:8080/announce",
			"http://81.200.2.231/announce",
			"http://157.7.202.64:8080/announce",
			"http://87.248.186.252:8080/announce",
			"udp://128.199.70.66:5944/announce",
			"udp://182.176.139.129:6969/announce",
			"http://128.199.70.66:5944/announce",
			"http://188.165.253.109:1337/announce",
			"http://93.92.64.5/announce",
			"http://173.254.204.71:1096/announce",
			"udp://195.123.209.40:80/announce",
			"udp://62.212.85.66:2710/announce",
			"udp://208.67.16.113:8000/announce",
			"http://125.227.35.196:6969/announce",
			"http://59.36.96.77:6969/announce",
			"http://87.253.152.137/announce",
			"http://158.69.146.212:7777/announce",
			"udp://tracker.coppersurfer.tk:6969/announce",
			"udp://zer0day.ch:1337/announce",
			"udp://tracker.leechers-paradise.org:6969/announce",
			"udp://9.rarbg.com:2710/announce",
			"udp://p4p.arenabg.com:1337/announce",
			"udp://tracker.sktorrent.net:6969/announce",
			"udp://tracker.aletorrenty.pl:2710/announce",
			"udp://tracker.kicks-ass.net:80/announce",
			"udp://torrent.gresille.org:80/announce",
			"udp://tracker.skyts.net:6969/announce",
			"udp://tracker.yoshi210.com:6969/announce",
			"udp://tracker.tiny-vps.com:6969/announce",
			"udp://tracker.internetwarriors.net:1337/announce",
			"udp://mgtracker.org:2710/announce",
			"udp://tracker.filetracker.pl:8089/announce",
			"udp://tracker.ex.ua:80/announce",
			"udp://91.218.230.81:6969/announce",
			"udp://tracker.grepler.com:6969/announce",
			"udp://tracker.flashtorrents.org:6969/announce",
			"udp://tracker.bittor.pw:1337/announce",
			"udp://tracker.kuroy.me:5944/announce",
			"udp://182.176.139.129:6969/announce",
			"udp://open.stealth.si:80/announce",
			"udp://208.67.16.113:8000/announce",
			"udp://tracker.coppersurfer.tk:6969/announce",
			"http://tracker.opentrackr.org:1337/announce",
			"udp://zer0day.ch:1337/announce",
			"http://explodie.org:6969/announce",
			"udp://tracker.leechers-paradise.org:6969/announce",
			"udp://9.rarbg.com:2710/announce",
			"udp://p4p.arenabg.com:1337/announce",
			"udp://tracker.sktorrent.net:6969/announce",
			"http://p4p.arenabg.com:1337/announce",
			"udp://tracker.aletorrenty.pl:2710/announce",
			"http://tracker.aletorrenty.pl:2710/announce",
			"http://tracker.bittorrent.am/announce",
			"udp://tracker.kicks-ass.net:80/announce",
			"http://tracker.kicks-ass.net/announce",
			"http://tracker.baravik.org:6970/announce",
			"udp://tracker.piratepublic.com:1337/announce",
			"udp://213.163.67.56:1337/announce",
			"http://213.163.67.56:1337/announce",
			"udp://185.86.149.205:1337/announce",
			"http://74.82.52.209:6969/announce",
			"udp://94.23.183.33:6969/announce",
			"udp://74.82.52.209:6969/announce",
			"udp://151.80.120.114:2710/announce",
			"udp://109.121.134.121:1337/announce",
			"udp://168.235.67.63:6969/announce",
			"http://109.121.134.121:1337/announce",
			"udp://178.33.73.26:2710/announce",
			"http://178.33.73.26:2710/announce",
			"http://85.17.19.180/announce",
			"udp://85.17.19.180:80/announce",
			"http://210.244.71.25:6969/announce",
			"http://85.17.19.180/announce"


			
			);
	
	public static Map<String, FileUploadResource> saveFileThreads  = new HashMap<String, FileUploadResource>();
	private String id;

	public String getId() {
		return id;
	}

	@Path("/files")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA+";charset=utf-8")
	@Produces({MediaType.TEXT_HTML})
	public Response createTorrent(@DefaultValue("") @FormDataParam("tags") String tags,
			@FormDataParam("files") List<FormDataBodyPart> bodyParts,
			@FormDataParam("files") FormDataContentDisposition fileDispositions,
			@FormDataParam("id") String id) throws IOException {

		if(saveFileThreads.size()>10) {
			 return Response.status(Status.SERVICE_UNAVAILABLE).entity("Service is over capacity").build();
		}
		this.id = id;
		StringBuffer fileDetails = new StringBuffer("");

		/* Save multiple files */
		MetadataBuilder builder = new MetadataBuilder();
		java.nio.file.Path path = FileSystems.getDefault().getPath(TorrentSessionStateService.getDownloadPath());
		
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
			java.nio.file.Path tmp = Files.createDirectories(path.resolve(id)).resolve(fileName);
			saveFile(bodyPartEntity.getInputStream(), tmp);
			fileDetails.append(" File saved at "+TorrentSessionStateService.getDownloadPath() + fileName);
			builder.addFile(tmp.toFile());
		}
		builder.doPublic();
		builder.setCreationTime(new Date().getTime());
		builder.setCreatedBy("https://www.hyperborian.org");
		builder.addTrackers(PUBLIC_TRACKERS);
		byte[] torrentBinary = builder.buildBinary();
		TorrentParser parser = new TorrentParser();
		TorrentMetadata metadata = parser.parse(torrentBinary);
		FileUploadResource object = saveFileThreads.get(metadata.getHexInfoHash());
		if(object!=null) {
			return Response.status(Response.Status.CREATED).header("Location", TorrentSessionStateService.getBaseUrl()+"/"+object.getId()+"/"+object.getId()+".torrent").header("Access-Control-Expose-Headers", "Location").build();
		}
		String torrentFilePath = path.resolve(id)+"/"+id+".torrent";
		try (FileOutputStream fos = new FileOutputStream(torrentFilePath)) {
			   fos.write(torrentBinary);
		}
		Storage storage = new FileSystemStorage(path.resolve(id));
		client = TorrentSessionStateService.builder.
				storage(storage).
				torrent(new File(torrentFilePath).toURI().toURL()).
				afterTorrentFetched(torrent -> System.out.println("**********************\n"+torrent.getTorrentId()+"\n**********************")).
				build();
		client.startAsync();
		
		saveFileThreads.put(metadata.getHexInfoHash(), this);
		return Response.status(Response.Status.CREATED).header("Location", TorrentSessionStateService.getBaseUrl()+"/"+id+"/"+id+".torrent").header("Access-Control-Expose-Headers", "Location").build();
	}
	
	public static String getMagnetLink(String infoHash) throws UnsupportedEncodingException {
		return "magnet:?xt=urn:btih:"+infoHash;
	}
	
	private BtClient client;
	
	public BtClient getClient() {
		return client;
	}

	private void saveFile(InputStream inputStream, java.nio.file.Path path) throws IOException {
	    OutputStream outStream = new FileOutputStream(path.toFile());
	    byte[] buffer = new byte[8 * 1024];
	    int bytesRead;
	    while ((bytesRead = inputStream.read(buffer)) != -1) {
	        outStream.write(buffer, 0, bytesRead);
	    }
	    IOUtils.closeQuietly(inputStream);
	    IOUtils.closeQuietly(outStream);
	}
	
}