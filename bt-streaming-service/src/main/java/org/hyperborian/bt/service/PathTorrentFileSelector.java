package org.hyperborian.bt.service;

import java.util.stream.Collectors;

import bt.torrent.fileselector.SelectionResult;
import  bt.torrent.fileselector.TorrentFileSelector;

public class PathTorrentFileSelector extends TorrentFileSelector {
	
	public PathTorrentFileSelector(String torrentFilePath) {
		this.torrentFilePath = torrentFilePath;
	}
	
	private String torrentFilePath;
	
	private long size;

	public long getSize() {
		return size;
	}

	@Override
	protected SelectionResult select(bt.metainfo.TorrentFile file) {
		
		
		if(file.getPathElements().stream().collect(Collectors.joining("/")).equals(torrentFilePath)) {
			size = file.getSize();
			return SelectionResult.select().build();
		} else {
			return SelectionResult.skip();
		}
	}

}
