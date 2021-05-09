package com.wexalian.showme.core.download;

import bt.metainfo.Torrent;
import bt.torrent.TorrentSessionState;
import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.torrent.Magnet;
import javafx.application.Platform;

import java.util.function.Consumer;

public class Download {
    private final Episode episode;
    private final Magnet magnet;
    private boolean canceled = false;
    
    private Runnable torrentQueued = () -> {};
    private Runnable torrentStart = () -> {};
    private Consumer<Torrent> torrentMetadataFetched = s -> {};
    private Consumer<TorrentSessionState> torrentSessionState = s -> {};
    private Runnable torrentFinished = () -> {};
    private Runnable torrentCanceled = () -> {};
    
    public Download(Episode episode, Magnet magnet) {
        this.episode = episode;
        this.magnet = magnet;
    }
    
    //runs on javafx thread
    public void onTorrentQueued(Runnable runnable) {
        torrentQueued = andThen(torrentQueued, runnable);
    }
    
    //runs on javafx thread
    public void onTorrentStart(Runnable runnable) {
        torrentStart = andThen(torrentStart, runnable);
    }
    
    private Runnable andThen(Runnable run1, Runnable run2) {
        return () -> {
            run1.run();
            run2.run();
        };
    }
    
    //runs on javafx thread
    public void onUpdateDownloadState(Consumer<TorrentSessionState> consumer) {
        torrentSessionState = torrentSessionState.andThen(consumer);
    }
    
    //runs on javafx thread
    public void onMetadataFetched(Consumer<Torrent> consumer) {
        torrentMetadataFetched = torrentMetadataFetched.andThen(consumer);
    }
    
    //runs on javafx thread
    public void onTorrentFinished(Runnable runnable) {
        torrentFinished = andThen(torrentFinished, runnable);
    }
    
    //runs on javafx thread
    public void onTorrentCanceled(Runnable runnable) {
        torrentCanceled = andThen(torrentCanceled, runnable);
    }
    
    //run on any thread
    public void updateTorrentQueued() {
        Platform.runLater(torrentQueued);
    }
    
    //run on any thread
    public void updateTorrentStart() {
        Platform.runLater(torrentStart);
    }
    
    //run on any thread
    public void updateDownloadState(TorrentSessionState state) {
        Platform.runLater(() -> torrentSessionState.accept(state));
    }
    
    //run on any thread
    public void updateTorrentFetched(Torrent state) {
        Platform.runLater(() -> torrentMetadataFetched.accept(state));
    }
    
    //run on any thread
    public void updateTorrentFinished() {
        Platform.runLater(torrentFinished);
    }
    
    //run on any thread
    public void updateTorrentCanceled() {
        this.canceled = true;
        Platform.runLater(torrentCanceled);
    }
    
    public Episode getEpisode() {
        return episode;
    }
    
    public Magnet getMagnet() {
        return magnet;
    }
    
    public boolean isCanceled() {
        return canceled;
    }
}
