package org.hyperborian.bt.pojo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TorrentMetainfo {
	
	public enum ContentType
	{
		video_mp4("video/mp4"),
		audio_mpeg("audio/mpeg"),
		text_html("text/html"),
		text_plain("text/plain"),
		application_pdf("application/pdf"),
		application_msword("application/msword"),
		image_jpeg(" image/jpeg");
		
	    private String string;
	 
	    ContentType(String string) {
	        this.string = string;
	    }
	 
	    public String toString() {
	        return string;
	    }
	}
	
	static Map<String, ContentType> classes;
	
	static {
		classes = new HashMap<String, ContentType>();
		classes.put("mp4", ContentType.video_mp4);
		classes.put("mp3", ContentType.audio_mpeg);
		classes.put("html", ContentType.text_html);
		classes.put("txt", ContentType.text_plain);
		classes.put("pdf", ContentType.application_pdf);
		classes.put("doc", ContentType.application_msword);
		classes.put("jpeg", ContentType.image_jpeg);
		classes.put("jpg", ContentType.image_jpeg);
		
		classes = Collections.unmodifiableMap(classes);
	}
	
	private int totalChunks;
	private long chunkSize;
	private String name;
	private long size;
	private int torrentFilesNumber;
	private String torrentFilePath;
	private ContentType contentType;

	public String getTorrentFilePath() {
		return torrentFilePath;
	}
	
	public void setTorrentFilePath(String torrentFilePath) {
		this.torrentFilePath = torrentFilePath;
		String ext = getExtension();
		if(ext!=null) {
			contentType = classes.get(ext);
		}
	}
	
	public String getExtension() {
		if(torrentFilePath==null) {
			return null;
		}
		int i = torrentFilePath.lastIndexOf('.');
		if (i > 0) {
		    return new String(torrentFilePath.substring(i+1)).toLowerCase();
		} else {
			return null;
		}
	}
	
	public int getTotalChunks() {
		return totalChunks;
	}
	public void setTotalChunks(int totalChunks) {
		this.totalChunks = totalChunks;
	}
	public long getChunkSize() {
		return chunkSize;
	}
	public void setChunkSize(long chunkSize) {
		this.chunkSize = chunkSize;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}

	public int getTorrentFilesNumber() {
		return torrentFilesNumber;
	}

	public void setTorrentFilesNumber(int torrentFilesNumber) {
		this.torrentFilesNumber = torrentFilesNumber;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

}
