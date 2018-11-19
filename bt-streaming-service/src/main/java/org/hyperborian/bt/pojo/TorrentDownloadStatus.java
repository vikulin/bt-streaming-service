package org.hyperborian.bt.pojo;

public class TorrentDownloadStatus {
	
	private boolean isComplete = false;
	
	private int chunkComplete = 0;

	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	public int getChunkComplete() {
		return chunkComplete;
	}

	public void setChunkComplete(int chunkComplete) {
		this.chunkComplete = chunkComplete;
	}

}
