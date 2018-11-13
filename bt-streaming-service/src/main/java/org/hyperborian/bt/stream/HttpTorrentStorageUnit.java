/*
 * Copyright (c) 2016—2017 Andrei Tomashpolskiy and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hyperborian.bt.stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bt.BtException;
import bt.data.StorageUnit;

public class HttpTorrentStorageUnit implements StorageUnit {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpTorrentStorageUnit.class);
    
    private volatile long currentOffset = 0;

	public long getCurrentOffset() {
		return currentOffset;
	}

	private long size;

	private String name;
	
	private List<Object> readOffset = new ArrayList<Object>();
	
	private List<Object> writeOffset = new ArrayList<Object>();
	
	public List<Object> getReadOffset() {
		return readOffset;
	}
	
	public List<Object> getWriteOffset() {
		return writeOffset;
	}

	private volatile boolean closed = false;

	private Map<Long, ByteBuffer> bufferMap;
	
	public int getBufferSize(){
		return bufferMap.size();
	}

    HttpTorrentStorageUnit(String name, long size, Map<Long, ByteBuffer> bufferMap) {
    	this.name= name;
    	this.size = size;
    	this.bufferMap = bufferMap;
    }
    
    public String getName() {
    	return name;
    }
    
    public synchronized ByteBuffer readNextBlock() {
    	Iterator<Long> iterator = bufferMap.keySet().iterator();
    	if(iterator.hasNext()) {
			Long offset = iterator.next();
    		ByteBuffer buffer = bufferMap.get(offset);
    		return buffer;
    	} else {
    		return ByteBuffer.allocate(0);
    	}
    }
    
    @Override
    public synchronized void readBlock(ByteBuffer buffer, long offset) {
    	readOffset.add(offset+"/b"+":"+currentOffset);
    	buffer = bufferMap.get(offset);
    }

    @Override
    public synchronized byte[] readBlock(long offset, int length) {
    	readOffset.add(offset+"/"+":"+length);
    	if(offset>=currentOffset) {
    		return new byte[length];
    	}
    	//readOffset.add(offset);
    	if (closed) {
    		return new byte[length];
        }
        if (offset < 0) {
            throw new BtException("Illegal arguments: offset (" + offset + ")");
        }
        ByteBuffer buffer = bufferMap.get(offset);
        byte[] dst = new byte[length];
        if(buffer!=null) {
        	buffer.get(dst);
        	
        }
        return dst;
    	//byte[] dst = new byte[length];
        //return dst;
    }

    @Override
    public synchronized void writeBlock(ByteBuffer buffer, long offset) {
    	int buferSize = buffer.remaining();
    	//writeOffset.add(offset+"/b"+buferSize+":"+currentOffset);
    	bufferMap.put(offset, buffer);
    	if(offset == currentOffset) {
    		joinBuffer(currentOffset);
    		//currentOffset += buferSize;
    	}
    }

    private void joinBuffer(long currentOffset2) {
    	long firstOffset = 0;
    	if(currentOffset>0) {
	    	ByteBuffer lastBuffer = bufferMap.get(currentOffset);
	    	ByteBuffer firstBuffer = bufferMap.get(firstOffset);
	    	ByteBuffer joinedBuffer = ByteBuffer.allocate(firstBuffer.limit() + lastBuffer.limit());
	    	joinedBuffer.put(firstBuffer);
	    	joinedBuffer.put(lastBuffer);
	    	joinedBuffer.rewind();
	    	bufferMap.remove(firstOffset);
	    	currentOffset=joinedBuffer.remaining();
	    	bufferMap.put(currentOffset, joinedBuffer);
    	}
	}

	@Override
    public synchronized void writeBlock(byte[] block, long offset) {
    	int buferSize = block.length;
    	//writeOffset.add(offset+"/"+buferSize);
    	ByteBuffer buffer = ByteBuffer.wrap(block);
    	bufferMap.put(offset, buffer);
    	if(offset == currentOffset) {
    		currentOffset += buferSize;
    	}
    }

    @Override
    public long capacity() {
        return size;
    }



    @Override
    public void close() throws IOException {
    	for(Entry<Long, ByteBuffer> entry:bufferMap.entrySet()) {
    		entry.getValue().clear();
    	}
        closed  = true;
    }

	@Override
	public long size() {
		return size;
	}
}