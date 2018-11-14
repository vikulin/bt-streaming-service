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

import bt.data.StorageUnit;
import bt.data.Storage;
import bt.metainfo.Torrent;
import bt.metainfo.TorrentFile;

import java.io.File;
import java.nio.file.Path;

/**
 * Provides file-system based storage for torrent files.
 *
 * <p>Information about the collection of files in a torrent is stored as a list of strings in the torrent metainfo.
 * This makes it possible for malicious parties to create torrents with malformed pathnames,
 * e.g. by using relative paths (. and ..), empty and invalid directory and file names, etc.
 * In the worst case this can lead to loss and corruption of user data and execution of arbitrary code.
 *
 * <p>This storage implementation performs normalization of file paths, ensuring that:
 * <ul>
 *     <li>all torrent files are stored inside the root directory of this storage (see {@link #FileSystemStorage(File)})</li>
 *     <li>all individual paths are checked for potential issues and fixed in a consistent way (see below)</li>
 * </ul>
 *
 * <p><b>Algorithm for resolving paths:</b><br>
 * 1) The following transformations are applied to each individual path element:
 * <ul>
 *     <li>trimming whitespaces,</li>
 *     <li>truncating trailing dots and whitespaces recursively,</li>
 *     <li>substituting empty names with an underscore character
 *         (this also includes names that became empty after the truncation of whitespaces and dots),</li>
 *     <li>in case there is a leading or trailing file separator,
 *         it is assumed that the path starts or ends with an empty name, respectively.</li>
 * </ul><br>
 * 2) Normalized path elements are concatenated together using {@link File#separator} as the delimiter.
 *
 * <p><b>Examples:</b><br>
 * {@code "a/b/c"     => "a/b/c"}<br>
 * {@code " a/  b /c" => "a/b/c"}<br>
 * {@code ".a/.b"     => ".a/.b"}<br>
 * {@code "a./.b."    => "a/.b"}<br>
 * {@code ""          => "_"}<br>
 * {@code "a//b"      => "a/_/b"}<br>
 * {@code "."         => "_"}<br>
 * {@code ".."        => "_"}<br>
 * {@code "/"         => "_/_"}<br>
 * {@code "/a/b/c"    => "_/a/b/c"}<br>
 *
 * @since 1.0
 */
public class StreamingFileSystemStorage implements Storage {

	private final Path rootDirectory;
	private org.hyperborian.bt.stream.PathNormalizer pathNormalizer;
	private StreamingFileSystemStorageUnit fss;

    public StreamingFileSystemStorage(Path rootDirectory) {
    	this.rootDirectory = rootDirectory;
        this.pathNormalizer = new PathNormalizer(rootDirectory.getFileSystem());
    }

    @Override
    public StorageUnit getUnit(Torrent torrent, TorrentFile torrentFile) {

    	String normalizedPath = pathNormalizer.normalize(torrentFile.getPathElements());
    	if(fss==null) {
    		fss = new StreamingFileSystemStorageUnit(rootDirectory, normalizedPath, torrentFile.getSize());
    	}
    	
        return fss;
    }

	public StreamingFileSystemStorageUnit getTorrentStorageUnit() {
		return fss;
	}
}
