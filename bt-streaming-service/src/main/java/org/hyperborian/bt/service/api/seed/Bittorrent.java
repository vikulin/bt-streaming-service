package org.hyperborian.bt.service.api.seed;

import java.util.List;

public class Bittorrent {
	
	private List<String> announceList;
	
	private Long creationDate;
	
	private BittorentInfo info;
	
	private String mode;

	public List<String> getAnnounceList() {
		return announceList;
	}

	public void setAnnounceList(List<String> announceList) {
		this.announceList = announceList;
	}

	public Long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Long creationDate) {
		this.creationDate = creationDate;
	}

	public BittorentInfo getInfo() {
		return info;
	}

	public void setInfo(BittorentInfo info) {
		this.info = info;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
