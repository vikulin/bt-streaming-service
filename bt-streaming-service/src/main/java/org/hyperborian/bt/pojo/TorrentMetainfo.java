package org.hyperborian.bt.pojo;

public class TorrentMetainfo {
	
	private int totalChunks;
	private long chunkSize;
	private String name;
	private long size;
	private int torrentFilesNumber;
	
	private String torrentFilePath;

	public String getTorrentFilePath() {
		return torrentFilePath;
	}
	
	public void setTorrentFilePath(String torrentFilePath) {
		this.torrentFilePath = torrentFilePath;
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

}
