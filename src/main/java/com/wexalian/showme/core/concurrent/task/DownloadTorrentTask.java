package com.wexalian.showme.core.concurrent.task;

import bt.Bt;
import bt.data.Storage;
import bt.data.StorageUnit;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.metainfo.TorrentFile;
import bt.net.buffer.ByteBufferView;
import bt.runtime.BtClient;
import bt.runtime.BtRuntime;
import bt.runtime.Config;
import bt.torrent.fileselector.SelectionResult;
import bt.torrent.fileselector.TorrentFileSelector;
import com.wexalian.showme.core.download.Download;
import com.wexalian.showme.core.download.DownloadManager;
import com.wexalian.showme.util.FileUtils;
import com.wexalian.showme.util.Utils;
import com.wexalian.showme.util.throwing.Throwing;
import javafx.concurrent.Task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DownloadTorrentTask extends Task<Void> {
    public static final TorrentFileSelector BT_FILE_SELECTOR = Utils.make(() -> new TorrentFileSelector() {
        @Override
        protected SelectionResult select(TorrentFile file) {
            List<String> path = file.getPathElements();
            if (path.size() == 1) {
                String fileName = path.get(0);
                
                if (fileName.endsWith(".mkv")) {
                    return SelectionResult.select().build();
                }
            }
            return SelectionResult.skip();
        }
    });
    private static final Config BT_CONFIG = Utils.make(() -> {
        Config config = new Config();
        config.setNumOfHashingThreads(Runtime.getRuntime().availableProcessors() * 2);
        return config;
    });
    private static final DHTModule BT_DHT_MODULE = Utils.make(() -> {
        DHTConfig config = new DHTConfig();
        config.setShouldUseRouterBootstrap(true);
        return new DHTModule(config);
    });
    
    private static BtRuntime BT_RUNTIME = null;
    
    private final Download download;
    private final Path path;
    
    public DownloadTorrentTask(Download download, Path path) {
        this.download = download;
        this.path = path;
    }
    
    @Override
    public Void call() {
        String magnet = download.getMagnet().getMagnetUrl();
        String fileName = FileUtils.getFileName(download.getEpisode());
        Storage storage = (torrent, file) -> new CustomFileSystemStorageUnit(path, fileName, file.getSize());
        
        BtClient client = Bt.client(getBtRuntime())
                            .fileSelector(BT_FILE_SELECTOR)
                            .afterTorrentFetched(download::updateTorrentFetched)
                            .stopWhenDownloaded()
                            .magnet(magnet)
                            .storage(storage)
                            .build();
        
        download.updateTorrentStart();
        download.onTorrentCanceled(client::stop);
        download.onTorrentFinished(() -> download.getEpisode().setFileName(fileName));
        
        CompletableFuture<?> future = client.startAsync(download::updateDownloadState, 1000L);
        Throwing.run(future::join, e -> DownloadManager.LOGGER.debug("Exception during torrent download", e));
        
        return null;
    }
    
    private static BtRuntime getBtRuntime() {
        if (BT_RUNTIME == null) {
            BT_RUNTIME = BtRuntime.builder(BT_CONFIG).module(BT_DHT_MODULE).autoLoadModules().disableAutomaticShutdown().build();
        }
        return BT_RUNTIME;
    }
    
    public static void shutdown() {
        if (BT_RUNTIME != null) {
            BT_RUNTIME.shutdown();
        }
    }
    
    public static class CustomFileSystemStorageUnit implements StorageUnit {
        private final Path parent;
        private final Path file;
        private final long capacity;
        private SeekableByteChannel sbc;
        private volatile boolean closed;
        
        CustomFileSystemStorageUnit(Path root, String fileName, long capacity) {
            this.file = root.resolve(fileName);
            this.parent = file.getParent();
            this.capacity = capacity;
            this.closed = true;
        }
        
        // notnowTODO: this is temporary fix for verification upon app start
        // should be re-done (probably need additional API to know if storage unit is "empty")
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private Boolean init(boolean create) {
            return Throwing.get(() -> {
                if (closed) {
                    if (!Files.exists(file)) {
                        if (create) {
                            if (!Files.exists(parent)) {
                                Files.createDirectories(parent);
                            }
                            Files.createFile(file);
                        }
                        else {
                            return false;
                        }
                    }
                    sbc = Files.newByteChannel(file, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
                    closed = false;
                }
                return true;
            }, e -> DownloadManager.LOGGER.error("Failed to create file storage -- can't create (some of the) directories", e));
        }
        
        @Override
        public synchronized int readBlock(ByteBuffer buffer, long offset) {
            if (closed) {
                if (!init(false)) {
                    return -1;
                }
            }
            
            if (offset < 0) {
                throw new IllegalArgumentException("Negative offset: " + offset);
            }
            else if (offset > capacity - buffer.remaining()) {
                throw new IllegalArgumentException("Received a request to read past the end of file (offset: " + offset + ", requested block length: " + buffer.remaining() + ", file capacity: " + capacity);
            }
            
            try {
                sbc.position(offset);
                return sbc.read(buffer);
            }
            catch (IOException e) {
                throw new UncheckedIOException("Failed to read bytes (offset: " + offset + ", requested block length: " + buffer.remaining() + ", file capacity: " + capacity + ")",
                                               e);
            }
        }
        
        @Override
        public synchronized void readBlockFully(ByteBuffer buffer, long offset) {
            int read = 0, total = 0;
            do {
                total += read;
                read = readBlock(buffer, offset + total);
            }
            while (read >= 0 && buffer.hasRemaining());
        }
        
        @Override
        public synchronized int writeBlock(ByteBuffer buffer, long offset) {
            if (closed) {
                if (!init(true)) {
                    return -1;
                }
            }
            
            if (offset < 0) {
                throw new IllegalArgumentException("Negative offset: " + offset);
            }
            else if (offset > capacity - buffer.remaining()) {
                throw new IllegalArgumentException("Received a request to write past the end of file (offset: " + offset + ", block length: " + buffer.remaining() + ", file capacity: " + capacity);
            }
            
            try {
                sbc.position(offset);
                return sbc.write(buffer);
            }
            catch (IOException e) {
                throw new UncheckedIOException("Failed to write bytes (offset: " + offset + ", block length: " + buffer.remaining() + ", file capacity: " + capacity + ")",
                                               e);
            }
        }
        
        @Override
        public synchronized void writeBlockFully(ByteBuffer buffer, long offset) {
            int written = 0, total = 0;
            do {
                total += written;
                written = writeBlock(buffer, offset + total);
            }
            while (written >= 0 && buffer.hasRemaining());
        }
        
        @Override
        public synchronized int writeBlock(ByteBufferView buffer, long offset) {
            if (closed) {
                if (!init(true)) {
                    return -1;
                }
            }
            
            if (offset < 0) {
                throw new IllegalArgumentException("Negative offset: " + offset);
            }
            else if (offset > capacity - buffer.remaining()) {
                throw new IllegalArgumentException("Received a request to write past the end of file (offset: " + offset + ", block length: " + buffer.remaining() + ", file capacity: " + capacity);
            }
            
            try {
                sbc.position(offset);
                return buffer.transferTo(sbc);
            }
            catch (IOException e) {
                throw new UncheckedIOException("Failed to write bytes (offset: " + offset + ", block length: " + buffer.remaining() + ", file capacity: " + capacity + ")",
                                               e);
            }
        }
        
        @Override
        public synchronized void writeBlockFully(ByteBufferView buffer, long offset) {
            int written = 0, total = 0;
            do {
                total += written;
                written = writeBlock(buffer, offset + total);
            }
            while (written >= 0 && buffer.hasRemaining());
        }
        
        @Override
        public long capacity() {
            return capacity;
        }
        
        @Override
        public long size() {
            try {
                return Files.exists(file) ? Files.size(file) : 0;
            }
            catch (IOException e) {
                throw new UncheckedIOException("Unexpected I/O error", e);
            }
        }
        
        @Override
        public void close() {
            if (!closed) {
                try {
                    sbc.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    closed = true;
                }
            }
        }
        
        @Override
        public String toString() {
            return "(" + capacity + " B) " + file;
        }
    }
}
