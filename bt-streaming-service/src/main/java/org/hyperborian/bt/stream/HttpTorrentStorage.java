package org.hyperborian.bt.stream;

import java.nio.ByteBuffer;
import java.util.Map;

import org.hyperborian.bt.map.MaxSizeSortedMap;

import bt.data.Storage;
import bt.data.StorageUnit;
import bt.metainfo.Torrent;
import bt.metainfo.TorrentFile;

public class HttpTorrentStorage implements Storage{
	
	private Map<Long, ByteBuffer> bufferMap = new MaxSizeSortedMap<Long, ByteBuffer>(10000);
	
	private int bufferSize;
	
	private HttpTorrentStorageUnit torrentStorageUnit;


	public HttpTorrentStorage(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	
	public HttpTorrentStorageUnit getTorrentStorageUnit() {
		return torrentStorageUnit;
	}
	
	@Override
	public StorageUnit getUnit(Torrent torrent, TorrentFile torrentFile) {
		if(torrentStorageUnit==null) {
			torrentStorageUnit = new HttpTorrentStorageUnit(torrent.getName(), torrent.getSize(), bufferMap);
		}
		return torrentStorageUnit;
	}

}
