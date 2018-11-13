package org.hyperborian.bt.client;

import java.io.File;

import com.google.inject.Module;

import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.runtime.BtClient;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
    	
    	String magnetUri = "magnet:?xt=urn:btih:a9b09d61aaa9090a5fa77f7da02bcd78b80f6f85&dn=example.torrent";
    	Storage storage = new FileSystemStorage(new File("~/Downloads").toPath());
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
    	BtClient client = Bt.client().magnet(magnetUri).storage(storage).autoLoadModules().module(module).build();
    	
    	client.startAsync(state->{
    		if(state.getPiecesRemaining()==0) {
    			client.stop();
    		}
    	}, 1000).join();
    }

}
