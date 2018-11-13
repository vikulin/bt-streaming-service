package org.hyperborian.bt.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.hyperborian.bt.stream.HttpTorrentStorage;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.inject.Module;

import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.metainfo.Torrent;
import bt.runtime.BtClient;
import bt.torrent.TorrentSessionState;
import bt.torrent.selector.PieceSelector;
import bt.torrent.selector.SequentialSelector;

@Path(value = "/")
public class HttpStreamingTorrentService implements Consumer<TorrentSessionState>{
	
	
	protected long chunkSize;
	private BtClient client;
	private int piecesAvailable;
	protected String name;
	protected long size;
	
	private HttpTorrentStorage storage;

	@GET
	public Response exportExcel()  throws Exception  {
		
		String magnetUri = "magnet:?xt=urn:btih:a9b09d61aaa9090a5fa77f7da02bcd78b80f6f85&dn=example.torrent";

    	/*FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
    	java.nio.file.Path foo = fs.getPath("/foo");
    	Files.createDirectory(foo);

    	java.nio.file.Path hello = foo.resolve("inmemory.torrent");
    	java.nio.file.Path file = Files.createFile(hello);
    	Storage storage = new FileSystemStorage(file);*/
		
    	//Storage storage = new FileSystemStorage(new File("target/Downloads").toPath());
		storage = new HttpTorrentStorage(1024*1024*1024);
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
				
			}
    		
    	};
		client = Bt.client().storage(storage).autoLoadModules().module(module).selector(selector).magnet(magnetUri).afterTorrentFetched(torrentConsumer).initEagerly().build();

    	/**
    	 * TODO add pre-load listener to return total size of certain torrent file
    	 * As result return Reponse.ok object within the listener
    	 */
    	/*client.startAsync(state->{
    		
    		if(state.getPiecesRemaining()==0) {
    			client.stop();
    		} else {
    			System.err.println("**********************************************");
    			System.err.println("************************************** Pieces:"+state.getPiecesComplete());
    			System.err.println("**********************************************");
    		}
    		
    	}, 1000);*/
		
		client.startAsync(this, 1000);
    	
	    
	    while(size==0) {
	    	Thread.sleep(200);
	    }
	    
	    TimerTask timerTask = new MyTimerTask();
        //running timer task as daemon thread
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
        System.out.println("TimerTask started");
	    
	    StreamingOutput stream = new StreamingOutput() {
	    	
			private long sent;

			public void write(OutputStream output) throws IOException {
		        /*
				try {
		        	byte[] buffer = new byte[8192];
		            int bytes;
		            while ((bytes = input.read()) != -1 || sent<size) {
		            	if(bytes>-1) {
			                output.write(bytes);
			                sent +=1;
		            	}
		            }
		        } catch (Exception e) {
		            throw new WebApplicationException(e);
		        } finally {
		            if (output != null) output.close();
		        }*/
		        
	        }
	    };
	    
	    ResponseBuilder response = Response.ok((Object) stream);
        response.header("Content-Disposition", "attachment; filename=\""+name+"\"");
        response.header("Content-Length", size);

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
	
	public class MyTimerTask extends TimerTask {

	    @Override
	    public void run() {
	        System.out.println("Timer task started at:"+new Date());
	        completeTask();
	        System.out.println("Timer task finished at:"+new Date());
	    }

	    private void completeTask() {
	    	System.err.println("");
			System.err.println("ReadList:"+Arrays.deepToString(storage.getTorrentStorageUnit().getReadOffset().toArray()));
			System.err.println("WriteList:"+Arrays.deepToString(storage.getTorrentStorageUnit().getWriteOffset().toArray()));
			System.err.println("");
			System.err.println("");
			System.err.println("Current offset:"+storage.getTorrentStorageUnit().getCurrentOffset());
			System.err.println("WriteList:"+Arrays.deepToString(storage.getTorrentStorageUnit().getWriteOffset().toArray()));
			System.err.println("");
	    }
	    
	}

}
