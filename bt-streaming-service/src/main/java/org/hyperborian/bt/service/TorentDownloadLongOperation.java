package org.hyperborian.bt.service;


import java.util.function.Consumer;

import bt.torrent.TorrentSessionState;

public abstract class TorentDownloadLongOperation implements Consumer<TorrentSessionState> {
	
	private int piecesComplete;

	@Override
	public void accept(TorrentSessionState t) {
		if(t.getPiecesRemaining()==0) {
			stop();
		} else {
			piecesComplete = t.getPiecesComplete();
			System.err.println("**********************************************");
			System.err.println("************************************** Pieces:"+t.getPiecesComplete());
			System.err.println("**********************************************");
			
			
		}
	}
	
	public abstract void stop();
	
	public int getPiecesComplete() {
		return piecesComplete;
	}

}
