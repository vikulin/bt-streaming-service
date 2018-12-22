package org.hyperborian.bt.service.api.seed;

import java.util.List;

public class Files {
	
	private String completedLength;
	
	private String index;
	
	private String length;
	
	private String path;
	
	private String selected;
	
	private List<String> uris;

	public String getCompletedLength() {
		return completedLength;
	}

	public void setCompletedLength(String completedLength) {
		this.completedLength = completedLength;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSelected() {
		return selected;
	}

	public void setSelected(String selected) {
		this.selected = selected;
	}

	public List<String> getUris() {
		return uris;
	}

	public void setUris(List<String> uris) {
		this.uris = uris;
	}

}
