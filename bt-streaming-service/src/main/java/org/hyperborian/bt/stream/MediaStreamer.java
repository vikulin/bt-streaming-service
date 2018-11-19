package org.hyperborian.bt.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

/**
 * Media streaming utility
 *
 * @author Arul Dhesiaseelan (arul@httpmine.org)
 */
public class MediaStreamer implements StreamingOutput {

    private int length;
    private SeekableByteChannel sbc;
    final byte[] buf = new byte[8192];

    public MediaStreamer(int length, SeekableByteChannel sbc) {
        this.length = length;
        this.sbc = sbc;
    }

    @Override
    public void write( OutputStream outputStream ) throws IOException, WebApplicationException {
        try {
        	
        	ByteBuffer buf = ByteBuffer.allocate(length);
            int read = 1;
            while(buf.hasRemaining() && read > 0) {
              read = sbc.read(buf);
            }
            try {
            	outputStream.write(buf.array());
            	outputStream.flush();
            } catch(java.net.SocketTimeoutException ex) {
            	ex.printStackTrace();
            } catch(java.io.IOException ex) {
            	ex.printStackTrace();
            }
        } 
        finally {
        	outputStream.close();
            sbc.close();
        }
    }

    public int getLenth() {
        return length;
    }
}