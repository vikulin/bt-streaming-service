package org.hyperborian.bt.service;

import java.util.function.Consumer;

import javax.ws.rs.core.Response.ResponseBuilder;

import bt.metainfo.Torrent;

abstract class TorrentConsumer implements Consumer<Torrent>{
	
	public abstract ResponseBuilder getResponseBuilder();

}
