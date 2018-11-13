package org.hyperborian.bt.stream;

import bt.metainfo.TorrentFile;

public interface StorageUnitListener {
	
	void received(TorrentFile torrentFile);

}
