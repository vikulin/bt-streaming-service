package org.hyperborian.bt.service.api.seed;

import java.util.List;

public class ReponseResult {
	
	private String bitfield;
	
	private Bittorrent bittorrent;
	
	private String completedLength;
	
	private String connections;
	
	private String downloadSpeed;
	
	private List<Files> files;
	
	private String gid;
	
	private String infoHash;
	
	private String numPieces;
	
	private String numSeeders;
	
	private String pieceLength;
	
	private String seeder;
	/**
	 * possible values: complete
	 */
	private String status;
	
	private String totalLength;
	
	private String uploadLength;
	
	private String uploadSpeed;

	public String getBitfield() {
		return bitfield;
	}

	public void setBitfield(String bitfield) {
		this.bitfield = bitfield;
	}

	public Bittorrent getBittorrent() {
		return bittorrent;
	}

	public void setBittorrent(Bittorrent bittorrent) {
		this.bittorrent = bittorrent;
	}

	public String getCompletedLength() {
		return completedLength;
	}

	public void setCompletedLength(String completedLength) {
		this.completedLength = completedLength;
	}

	public String getConnections() {
		return connections;
	}

	public void setConnections(String connections) {
		this.connections = connections;
	}

	public String getDownloadSpeed() {
		return downloadSpeed;
	}

	public void setDownloadSpeed(String downloadSpeed) {
		this.downloadSpeed = downloadSpeed;
	}

	public List<Files> getFiles() {
		return files;
	}

	public void setFiles(List<Files> files) {
		this.files = files;
	}

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getInfoHash() {
		return infoHash;
	}

	public void setInfoHash(String infoHash) {
		this.infoHash = infoHash;
	}

	public String getNumPieces() {
		return numPieces;
	}

	public void setNumPieces(String numPieces) {
		this.numPieces = numPieces;
	}

	public String getNumSeeders() {
		return numSeeders;
	}

	public void setNumSeeders(String numSeeders) {
		this.numSeeders = numSeeders;
	}

	public String getPieceLength() {
		return pieceLength;
	}

	public void setPieceLength(String pieceLength) {
		this.pieceLength = pieceLength;
	}

	public String getSeeder() {
		return seeder;
	}

	public void setSeeder(String seeder) {
		this.seeder = seeder;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTotalLength() {
		return totalLength;
	}

	public void setTotalLength(String totalLength) {
		this.totalLength = totalLength;
	}

	public String getUploadLength() {
		return uploadLength;
	}

	public void setUploadLength(String uploadLength) {
		this.uploadLength = uploadLength;
	}

	public String getUploadSpeed() {
		return uploadSpeed;
	}

	public void setUploadSpeed(String uploadSpeed) {
		this.uploadSpeed = uploadSpeed;
	}

}
