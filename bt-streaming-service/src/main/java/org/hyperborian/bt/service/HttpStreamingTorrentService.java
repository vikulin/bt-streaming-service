package org.hyperborian.bt.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.hyperborian.bt.stream.StreamingFileSystemStorage;

import com.google.inject.Module;

import bt.Bt;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.metainfo.Torrent;
import bt.runtime.BtClient;
import bt.torrent.TorrentSessionState;
import bt.torrent.selector.PieceSelector;
import bt.torrent.selector.SequentialSelector;

@Path(value = "/")
public class HttpStreamingTorrentService implements Consumer<TorrentSessionState>{
	
	private final int chunk_size = 1024 * 1024 * 2; // 2 MB chunks
	
	
	protected long chunkSize;
	public static BtClient client;
	private int piecesAvailable;
	protected String name;
	protected long size;
	
	private StreamingFileSystemStorage storage;
	private java.nio.file.Path pathToFile;
	protected int totalChunks;

	@GET
	@Path("/torrent/{infoHash}")
	@Produces("video/mp4")
	public Response getTorrent(@HeaderParam("Range") String range, @PathParam("infoHash") @NotNull @Size(min = 40, max = 40) @Pattern(regexp = "^[a-fA-F0-9]+$") String infoHash)  throws Exception  {
		
		//String magnetUri = "magnet:?xt=urn:btih:a9b09d61aaa9090a5fa77f7da02bcd78b80f6f85&dn=example.torrent";
		
    	
    	if(client==null) {
    		String magnetUri = "magnet:?xt=urn:btih:"+infoHash;
    		pathToFile = Paths.get("Download");
    		storage = new StreamingFileSystemStorage(pathToFile);
        	PieceSelector selector = SequentialSelector.sequential();
        	Module module = new DHTModule (new DHTConfig() {
        		@Override
        		public boolean shouldUseRouterBootstrap() {
        			return true;
        		}
        		
        		@Override
        		public boolean shouldUseIPv6() {
        			return false;
        		}
        	});
        	Consumer<Torrent> torrentConsumer = new Consumer<Torrent>() {

    			@Override
    			public void accept(Torrent t) {
    				HttpStreamingTorrentService.this.chunkSize = t.getChunkSize();
    				HttpStreamingTorrentService.this.name = t.getName();
    				HttpStreamingTorrentService.this.size = t.getSize();
    				HttpStreamingTorrentService.this.totalChunks = (int) Math.ceil(size / chunkSize);
    			}
        		
        	};
    		client = Bt.client().storage(storage).autoLoadModules().module(module).selector(selector).magnet(magnetUri).afterTorrentFetched(torrentConsumer).build();
    		client.startAsync(this, 1000);
    	}
		
    
	    while(size==0) {
	    	Thread.sleep(200);
	    }
	    
	    while(piecesAvailable==0) {
	    	Thread.sleep(1000);
	    }
	    
	    StreamingOutput stream = new StreamingOutput() {
	    	
			private long piecesSent = 0;
			
			
			
			java.nio.file.Path path = Paths.get(pathToFile.toString(), name);
			SeekableByteChannel sbc = Files.newByteChannel(path, StandardOpenOption.READ);
			public void write(OutputStream output) throws IOException {
	            try {
	            	while(true) {
			            while(piecesAvailable==piecesSent) {
			            	try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
			            }
			            sbc.position(piecesSent*chunkSize);
			            ByteBuffer buf = ByteBuffer.allocate((int) chunkSize);
			            int read = 1;
			            int gotBytes = 0;
			            while(buf.hasRemaining() && read > 0) {
			              read = sbc.read(buf);
			              gotBytes+=read;
			            }
			            output.write(buf.array(), 0, gotBytes);
			            output.flush();
			            piecesSent = piecesAvailable;
			            if(piecesSent==totalChunks) {
			            	output.close();
			            	break;
			            }
	            	}
	            } finally {
	            	sbc.close();
	            	Files.delete(path);
	            }
	        }
	    };
	    
	    ResponseBuilder response = Response.ok((Object) stream);
	    response.header("Content-Disposition", "attachment; filename=\""+name+"\"");
	    response.header("Content-Type", "video/mp4");
        response.header(HttpHeaders.CONTENT_LENGTH, size);

	    return response.build();

	}

	@Override
	public void accept(TorrentSessionState t) {
		if(t.getPiecesRemaining()==0) {
			client.stop();
		} else {
			piecesAvailable = t.getPiecesComplete();
			System.err.println("**********************************************");
			System.err.println("************************************** Pieces:"+t.getPiecesComplete());
			System.err.println("**********************************************");
			
			
		}
	}

}
