package org.hyperborian.bt.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Security;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimerTask;
import java.util.function.Consumer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.hyperborian.bt.pojo.TorrentDownloadStatus;
import org.hyperborian.bt.pojo.TorrentMetainfo;
import org.hyperborian.bt.stream.MediaStreamer;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Module;

import bt.Bt;
import bt.StandaloneClientBuilder;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.metainfo.Torrent;
import bt.protocol.crypto.EncryptionPolicy;
import bt.runtime.BtClient;
import bt.runtime.Config;
import bt.torrent.TorrentSessionState;
import bt.torrent.selector.PieceSelector;
import bt.torrent.selector.RarestFirstSelector;
import bt.torrent.selector.SequentialSelector;

@Path(value = "/")
public class TorrentSessionStateService implements Consumer<TorrentSessionState>{
	
	private static Properties properties = new Properties();
	
	static {
		configureSecurity();
		try {
			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("server.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public TorrentSessionStateService() {
		
		if(builder==null) {
		
	   	 	config = new Config() {
	   	 		
	   	     public InetAddress getAcceptorAddress() {
	   	        try {
					return getBindIpAddress();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				return null;
	   	     }
	 		
	   	 		@Override
		   		public int getAcceptorPort() {
		   			return getAcceptorTcpPort();
		   		}
	           @Override
	           public int getNumOfHashingThreads() {
	               return Runtime.getRuntime().availableProcessors();
	           }
	           
	           @Override
	           public EncryptionPolicy getEncryptionPolicy() {
	               return EncryptionPolicy.PREFER_PLAINTEXT; 
	           }
	   	 	};
	   	 	Module module = new DHTModule (new DHTConfig() {
	   	 		
	   	 		/*
	   	 		@Override
	   	 		public Collection<InetPeerAddress> getBootstrapNodes() {
	   	 			List<InetPeerAddress> nodes = new ArrayList<InetPeerAddress>(super.getBootstrapNodes());
	   	 			try {
						nodes.add(new InetPeerAddress(TorrentSessionStateService.getBindIpAddress().toString(), getAcceptorTcpPort()));
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
	   	 			return nodes;
	   	 		}*/
	   		
		   		@Override
		   		public int getListeningPort() {
		   			return getDhtUdpPath();
		   		}
		   		@Override
		   		public boolean shouldUseRouterBootstrap() {
		   			return true;
		   		}
		   		
		   		@Override
		   		public boolean shouldUseIPv6() {
		   			return getShouldUseIpv6();
		   		}
		   		
				@Override
				public boolean getShutdownAfterClientStop() {
					return false;
				}
				
				@Override
				public Collection<InetAddress> getBindIpAddress() {
					try {
						return Arrays.asList(TorrentSessionStateService.getBindIpAddress());
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
					return null;
				}

		   		
	   	 	});
	   	 	builder = Bt.client().config(config).disableAutomaticShutdown().autoLoadModules().module(module).selector(RarestFirstSelector.randomizedRarest());
		}
		
		gson = new Gson();
	}
	
	class TineoutTimerTask extends TimerTask {

		@Override
		public void run() {
			/**
			 * After timeout occurs stop corresponding client and delete it from map
			 */
			client.stop();
		}
		
	}
	
	private Gson gson;
	
	private static void configureSecurity() {
	    // Starting with JDK 8u152 this is a way 
	    //   to programmatically allow unlimited encryption
	    // See http://www.oracle.com/technetwork/java/javase/8u152-relnotes-3850503.html
	    String key = "crypto.policy";
	    String value = "unlimited";
	    try {
	        Security.setProperty(key, value);
	    } catch (Exception e) {
	        System.err.println(String.format(
	              "Failed to set security property '%s' to '%s'", key, value));
	    }
	}
	
	public static Map<String , TorrentSessionStateService> torrentSessionState = new HashMap<String ,TorrentSessionStateService>();
	
	private BtClient client;
	
	public static StandaloneClientBuilder builder;
	
	public BtClient getClient() {
		return client;
	}

	public void setClient(BtClient client) {
		this.client = client;
	}
	
	private Config config;
	
	public Config getRuntimeConfig() {
		return config;
	}

	public static String getDownloadPath(){
		return properties.getProperty("download_path");
	}
	
	public static String getBaseUrl(){
		return properties.getProperty("base_url");
	}
	
	static int getDhtUdpPath(){
		return Integer.parseInt(properties.getProperty("dht_udp_port"));
	}
	
	static InetAddress getBindIpAddress() throws UnknownHostException{
		return InetAddress.getByName(properties.getProperty("bind_ip_address"));
	}
	
	static int getAcceptorTcpPort(){
		return Integer.parseInt(properties.getProperty("acceptor_tcp_port"));
	}
	
	static boolean getShouldUseIpv6(){
		return Boolean.parseBoolean(properties.getProperty("should_use_ipv6"));
	}
	
	static String getSeedingApiUrl(){
		return properties.getProperty("seeding_api_url");
	}
	
	private FileSystemStorage storage;
	
	private java.nio.file.Path pathToFile;
	
	private TorrentMetainfo tm;
	
	private PathTorrentFileSelector fileSelector;

	public TorrentMetainfo getTorrentMetainfo() {
		return tm;
	}

	private TorrentDownloadStatus status;

	public TorrentDownloadStatus getStatus() {
		return status;
	}

	private Object getPath(String pathSet) {
		if(pathSet==null || pathSet.trim().length()==0) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		Set<String> path = null;
		try {
			path = gson.fromJson(pathSet, Set.class);
			if(path==null || path.size()==0) {
				return Response.status(Status.BAD_REQUEST).build();
			}
			if(path.size()>1) {
				return Response.status(Status.NOT_IMPLEMENTED).build();
			}
		} catch (JsonSyntaxException ex) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		return path;
	}

	@POST
	@Path("/torrent/{infoHash}")
	@Produces("application/json")
	public Response getTorrent(@PathParam("infoHash") @NotNull @Size(min = 40, max = 40) @Pattern(regexp = "^[a-fA-F0-9]+$") String infoHash, String pathSet)  throws Exception  {
		
		Set<String> path = null;
		Object result = getPath(pathSet);
		if(result instanceof Response) {
			return (Response) result;
		}
		if(result instanceof Set) {
			path = (Set<String>) result;
		}
		String torrentFilePath = path.iterator().next();
    	String pathHash = Hashing.sha256().hashString(torrentFilePath, Charsets.UTF_8 ).toString();
    	String sessionKey = infoHash+"|"+pathHash;
    	TorrentSessionStateService tss = torrentSessionState.get(sessionKey);
    	if(tss!=null) {
    		if(tss.getClient().isStarted() || tss.getStatus().isComplete()) {
	    	    ResponseBuilder response = Response.ok();
	    	    response.status(Response.Status.NOT_MODIFIED);
	            return response.build();
    		}
    		/**
    		 * else torrent client is going to be initialize egain
    		 */
    	}
    	status = new TorrentDownloadStatus();
		tm = new TorrentMetainfo();
		tm.setTorrentFilePath(torrentFilePath);
		fileSelector = new PathTorrentFileSelector(torrentFilePath);
		//String magnetUri = "magnet:?xt=urn:btih:a9b09d61aaa9090a5fa77f7da02bcd78b80f6f85&dn=example.torrent";

		String magnetUri = "magnet:?xt=urn:btih:"+infoHash;
		pathToFile = Paths.get(getDownloadPath(),infoHash);
		if(!pathToFile.toFile().exists()) {
			Files.createDirectories(pathToFile);
		}
		storage = new FileSystemStorage(pathToFile);
		PieceSelector pieceSelector = null;
		switch (tm.getContentType()) {
		case audio_mpeg:
			pieceSelector = RarestFirstSelector.randomizedRarest();
			break;
		case video_mp4:
			pieceSelector = SequentialSelector.sequential();
			break;
		default:
			pieceSelector = RarestFirstSelector.randomizedRarest();
			break;
		}
    	
    	
    	Consumer<Torrent> torrentConsumer = new Consumer<Torrent>() {

			@Override
			public void accept(Torrent t) {
				torrentSessionState.entrySet().stream().filter(entry->entry.getKey().contains(t.getTorrentId().toString())).forEach(entry->{
					TorrentMetainfo tm = entry.getValue().getTorrentMetainfo();
					tm.setChunkSize(t.getChunkSize());
					tm.setName(t.getName());
					tm.setTotalChunks((int) Math.ceil(t.getSize() / t.getChunkSize()));
					tm.setTorrentFilesNumber(t.getFiles().size());
				});
			}
    	};
		
    	client = builder.storage(storage).afterTorrentFetched(torrentConsumer).fileSelector(fileSelector).magnet(magnetUri).build();

		client.startAsync(this, 1000);    	
    	torrentSessionState.put(sessionKey, this);

	    ResponseBuilder response = Response.ok();
	    response.status(Response.Status.CREATED);
        return response.build();
	}
	
	int timer=0;
	/**
	 * it could be more accurate if chunk size is very big
	 */
	int masInactivityThreshold = 300;//5 min
	
	@Override
	public void accept(TorrentSessionState t) {
		
		tm.setSize(fileSelector.getSize());
		if(t.getPiecesRemaining()==0) {
			client.stop();
			status.setComplete(true);
			status.setChunkComplete(t.getPiecesComplete());
		} else {
			if(t.getPiecesComplete()==status.getChunkComplete()) {
				timer++;
			} else {
				timer=0;
			}
			if(timer>masInactivityThreshold) {
				client.stop();
				status.setError("Timeout occured: download inactivity exceeded 5 min.");
			}
			status.setChunkComplete(t.getPiecesComplete());
			System.err.println("**********************************************");
			System.err.println("************************************** Pieces:"+t.getPiecesComplete());
			System.err.println("**********************************************");
		}
	}
	
	@GET
	@Path("/status/{infoHash}/{pathHashSet}")
	@Produces("application/json")
	public Response getChunkNumber(@PathParam("infoHash") @NotNull @Size(min = 40, max = 40) @Pattern(regexp = "^[a-fA-F0-9]+$") String infoHash, @PathParam("pathHashSet") @NotNull @Size(min = 64, max = 64) @Pattern(regexp = "^[a-fA-F0-9]+$") String pathHashSet)  throws Exception  {
		
		String sessionKey = infoHash+"|"+pathHashSet;
		TorrentSessionStateService torrentState = torrentSessionState.get(sessionKey);
		if(torrentState==null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		String status = gson.toJson(torrentState.getStatus());
		ResponseBuilder response = Response.ok().entity(status);
	    response.status(Response.Status.OK);
        return response.build();
	}
	
	@GET
	@Path("/metainfo/{infoHash}/{pathHashSet}")
	@Produces("application/json")
	public Response getTorrentMetainfo(@PathParam("infoHash") @NotNull @Size(min = 40, max = 40) @Pattern(regexp = "^[a-fA-F0-9]+$") String infoHash, @PathParam("pathHashSet") @NotNull @Size(min = 64, max = 64) @Pattern(regexp = "^[a-fA-F0-9]+$") String pathHashSet)  throws Exception  {
		
		String sessionKey = infoHash+"|"+pathHashSet;
		TorrentSessionStateService torrentState = torrentSessionState.get(sessionKey);
		if(torrentState==null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		TorrentMetainfo tm = torrentState.getTorrentMetainfo();
		
		ResponseBuilder response = Response.ok().entity(gson.toJson(tm, TorrentMetainfo.class));
	    response.status(Response.Status.OK);
        return response.build();
	}
	
    // for clients to check whether the server supports range / partial content requests
    @HEAD
    @Path("/stream/{infoHash}/{pathHashSet}")
    public Response header(@HeaderParam("Range") String range, @PathParam("infoHash") @NotNull @Size(min = 40, max = 40) @Pattern(regexp = "^[a-fA-F0-9]+$") String infoHash, @PathParam("pathHashSet") @NotNull @Size(min = 64, max = 64) @Pattern(regexp = "^[a-fA-F0-9]+$") String pathHashSet) {
    	logger.info("@HEAD request received");
    	
    	String sessionKey = infoHash+"|"+pathHashSet;
    	TorrentSessionStateService torrentState = torrentSessionState.get(sessionKey);
		if(torrentState==null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		TorrentMetainfo tm = torrentState.getTorrentMetainfo();
		String torrentFilePath = tm.getTorrentFilePath();
		File torrentFile;
		if(tm.getTorrentFilesNumber()>1) {
			String name = tm.getName();
			torrentFile = new File(getDownloadPath()+"/"+infoHash+"/"+name+"/"+torrentFilePath);
		} else {
			torrentFile = new File(getDownloadPath()+"/"+infoHash+"/"+torrentFilePath);
		}
		if(!torrentFile.exists()) {
			TorrentSessionStateService session = torrentSessionState.remove(sessionKey);
			if(session!=null) {
				session.getClient().stop();
				return Response.status(Status.TEMPORARY_REDIRECT).build();
			} else {
				return Response.status(Status.NOT_FOUND).build();
			}
		}
        
        return Response.ok()
        		.status( Response.Status.PARTIAL_CONTENT )
        		.header( HttpHeaders.CONTENT_LENGTH, tm.getSize() )
        		.header( "Accept-Ranges", "bytes" )
        		.header( HttpHeaders.CONTENT_TYPE, tm.getContentType().toString() )
        		.build();
    }
    
    // stop bt client gracefully
    @DELETE
    @Path("/stream/{infoHash}/{pathHashSet}")
    public Response deleteTorrent(@PathParam("infoHash") @NotNull @Size(min = 40, max = 40) @Pattern(regexp = "^[a-fA-F0-9]+$") String infoHash, @PathParam("pathHashSet") @NotNull @Size(min = 64, max = 64) @Pattern(regexp = "^[a-fA-F0-9]+$") String pathHashSet) {
    	logger.info("@DELETE request received");
    	
    	String sessionKey = infoHash+"|"+pathHashSet;
    	TorrentSessionStateService torrentState = torrentSessionState.get(sessionKey);
		if(torrentState==null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		TorrentMetainfo tm = torrentState.getTorrentMetainfo();
		String torrentFilePath = tm.getTorrentFilePath();
		File torrentFile;
		if(tm.getTorrentFilesNumber()>1) {
			String name = tm.getName();
			torrentFile = new File(getDownloadPath()+"/"+infoHash+"/"+name+"/"+torrentFilePath);
		} else {
			torrentFile = new File(getDownloadPath()+"/"+infoHash+"/"+torrentFilePath);
		}
		if(torrentFile.exists()) {
			//TODO check that the file is not being playing for enyone
			//torrentFile.delete();
			//torrentSessionState.remove(sessionKey);
		} else {
			TorrentSessionStateService session = torrentSessionState.remove(sessionKey);
			if(session!=null) {
				session.getClient().stop();
				return Response.status(Status.TEMPORARY_REDIRECT).build();
			} else {
				return Response.status(Status.NOT_FOUND).build();
			}
		}
		torrentState.getClient().stop();
        return Response.ok().status(Response.Status.ACCEPTED).build();
    }

    
    @GET
    @Path("/stream/{infoHash}/{pathHashSet}")
    public Response stream( @HeaderParam("Range") String range, @PathParam("infoHash") @NotNull @Size(min = 40, max = 40) @Pattern(regexp = "^[a-fA-F0-9]+$") String infoHash, @PathParam("pathHashSet") @NotNull @Size(min = 64, max = 64) @Pattern(regexp = "^[a-fA-F0-9]+$") String pathHashSet) throws Exception {
    	
    	String sessionKey = infoHash+"|"+pathHashSet;
    	TorrentSessionStateService torrentState = torrentSessionState.get(sessionKey);
		if(torrentState==null) {
			return Response.status(Status.NOT_FOUND).build();
		}
        TorrentMetainfo tm = torrentState.getTorrentMetainfo();
		String torrentFilePath = tm.getTorrentFilePath();
		File torrentFile;
		if(tm.getTorrentFilesNumber()>1) {
			String name = tm.getName();
			torrentFile = new File(getDownloadPath()+"/"+infoHash+"/"+name+"/"+torrentFilePath);
		} else {
			torrentFile = new File(getDownloadPath()+"/"+infoHash+"/"+torrentFilePath);
		}
		if(!torrentFile.exists()) {
			TorrentSessionStateService session = torrentSessionState.remove(sessionKey);
			if(session!=null) {
				session.getClient().stop();
				return Response.status(Status.TEMPORARY_REDIRECT).build();
			} else {
				return Response.status(Status.NOT_FOUND).build();
			}
		}
        return buildStream(tm, torrentFile, range );
    }
    
    
    /**
     * @param asset Media file
     * @param range range header
     * @return Streaming output
     * @throws Exception IOException if an error occurs in streaming.
     */
    private Response buildStream(TorrentMetainfo tm, final File asset, final String range ) throws Exception {
        // range not requested: firefox does not send range headers
        if ( range == null ) {
        	logger.info("Request does not contain a range parameter!");
        	
            StreamingOutput streamer = output -> {
                try ( FileChannel inputChannel = new FileInputStream( asset ).getChannel(); 
                	  WritableByteChannel outputChannel = Channels.newChannel( output ) ) {
                	
                    inputChannel.transferTo( 0, inputChannel.size(), outputChannel );
                }
                catch( IOException io ) {
                	logger.info( io.getMessage() );
                }
            };
            
            return Response.ok( streamer )
            		.status( Response.Status.OK )
            		.header( HttpHeaders.CONTENT_LENGTH, asset.length() )
            		.header( HttpHeaders.CONTENT_TYPE, tm.getContentType().toString() )
            		.build();
        }

        logger.info( "Requested Range: " + range );
        
        String[] ranges = range.split( "=" )[1].split( "-" );
        
        int from = Integer.parseInt( ranges[0] );
        
        // Chunk media if the range upper bound is unspecified
        int to = chunkSize + from;
        
        if ( to >= asset.length() ) {
            to = (int) ( asset.length() - 1 );
        }
        
        // uncomment to let the client decide the upper bound
        // we want to send 2 MB chunks all the time
        if ( ranges.length == 2 ) {
        	int tmp = Integer.parseInt( ranges[1] );
        	if(tmp-from<=5*chunkSize) {
        		to=tmp;
        	}
        }
        
        final String responseRange = String.format( "bytes %d-%d/%d", from, to, asset.length() );
        
        logger.info( "Response Content-Range: " + responseRange + "\n");
        
        //final RandomAccessFile raf = new RandomAccessFile( asset, "r" );
        final SeekableByteChannel sbc = Files.newByteChannel(asset.toPath(), StandardOpenOption.READ);
        sbc.position(from);
        //raf.seek( from );

        final int len = to - from + 1;
        final MediaStreamer mediaStreamer = new MediaStreamer( len, sbc );

        return Response.ok( mediaStreamer )
                .status( Response.Status.PARTIAL_CONTENT )
                .header( "Accept-Ranges", "bytes" )
                .header( "Content-Range", responseRange )
                .header( HttpHeaders.CONTENT_LENGTH, mediaStreamer. getLenth() )
                .header( HttpHeaders.LAST_MODIFIED, new Date( asset.lastModified() ) )
                .header( HttpHeaders.CONTENT_TYPE, tm.getContentType().toString() )
                .build();
    }
    
    private final int chunkSize = 1024 * 1024 * 2; // 2 MB chunks
    
    final static Logger logger = Logger.getLogger( TorrentSessionStateService.class );
    
}
