package org.hyperborian.bt.service;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
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

/**
 * 
 * @author geekmj Single File and Multiple Files upload example
 */
@Path("/upload")
public class FileUploadResource {
	
	public static Map saveFileThreads  = new HashMap();

	@Path("/files")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA+";charset=utf-8")
	@Produces({MediaType.TEXT_HTML})
	public Response uploadFiles2(@DefaultValue("") @FormDataParam("tags") String tags,
			@FormDataParam("files") List<FormDataBodyPart> bodyParts,
			@FormDataParam("files") FormDataContentDisposition fileDispositions,
			@FormDataParam("id") String id) throws IOException {

		if(saveFileThreads.size()>10) {
			 return Response.status(Status.SERVICE_UNAVAILABLE).entity("Service is over capacity").build();
		}
		StringBuffer fileDetails = new StringBuffer("");

		/* Save multiple files */

		for (int i = 0; i < bodyParts.size(); i++) {
			/*
			 * Casting FormDataBodyPart to BodyPartEntity, which can give us
			 * InputStream for uploaded file
			 */
			BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyParts.get(i).getEntity();
			String fileName = new String(bodyParts.get(i).getContentDisposition().getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
			
			java.nio.file.Path path = FileSystems.getDefault().getPath(TorrentSessionStateService.getDownloadPath());
			
			if(!Files.exists(path)) {
				Files.createDirectories(path);
			}
			java.nio.file.Path tmp = Files.createDirectories(path.resolve(id)).resolve(fileName);
			//SaveFileThread saveFileThread = new SaveFileThread(bodyPartEntity.getInputStream(), tmp);
			//Thread thread = new Thread(saveFileThread);
			//thread.start();
			saveFile(bodyPartEntity.getInputStream(), tmp);
			
			fileDetails.append(" File saved at "+TorrentSessionStateService.getDownloadPath() + fileName);
		}

		return Response.ok("File uploaded").build();
	}

	private void saveFile(InputStream file, String name) {
		try {
			/* Change directory path */
			java.nio.file.Path path = FileSystems.getDefault().getPath(TorrentSessionStateService.getDownloadPath()+"/" + name);
			/* Save InputStream as file */
			Files.copy(file, path);
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	
	public void saveFile(InputStream inputStream, java.nio.file.Path path) throws IOException {
	    OutputStream outStream = new FileOutputStream(path.toFile());
	    byte[] buffer = new byte[8 * 1024];
	    int bytesRead;
	    while ((bytesRead = inputStream.read(buffer)) != -1) {
	        outStream.write(buffer, 0, bytesRead);
	    }
	    IOUtils.closeQuietly(inputStream);
	    IOUtils.closeQuietly(outStream);
	}
	
	class SaveFileThread implements Runnable{
		
		private InputStream inputStream;
		private java.nio.file.Path path;

		public SaveFileThread(InputStream inputStream, java.nio.file.Path path) {
			this.inputStream = inputStream;
			this.path = path;
		}
		
		public void saveFile() throws IOException {
		    OutputStream outStream = new FileOutputStream(path.toFile());
		    byte[] buffer = new byte[8 * 1024];
		    int bytesRead;
		    while ((bytesRead = inputStream.read(buffer)) != -1) {
		        outStream.write(buffer, 0, bytesRead);
		    }
		    IOUtils.closeQuietly(inputStream);
		    IOUtils.closeQuietly(outStream);
		}

		@Override
		public void run() {
			try {
				saveFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}