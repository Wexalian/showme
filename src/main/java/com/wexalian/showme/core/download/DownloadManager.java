package com.wexalian.showme.core.download;

import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Season;
import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.concurrent.Threading;
import com.wexalian.showme.core.concurrent.task.DownloadTorrentTask;
import com.wexalian.showme.core.show.IShowProvider;
import com.wexalian.showme.core.show.ShowParser;
import com.wexalian.showme.core.torrent.Magnet;
import com.wexalian.showme.core.torrent.TorrentCollector;
import com.wexalian.showme.gui.component.poster.DownloadAvailablePoster;
import com.wexalian.showme.gui.component.poster.DownloadPoster;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DownloadManager {
    public static final Logger LOGGER = LogManager.getLogger();
    
    public static final int MAX_CONCURRENT_DOWNLOADS = 1;
    public static final ObservableMap<Show, ObservableMap<Episode, Download>> AVAILABLE_DOWNLOADS = FXCollections.observableHashMap();
    private static final List<Download> DOWNLOAD_QUEUE = new ArrayList<>();
    private static final AtomicInteger CURRENTLY_DOWNLOADING = new AtomicInteger(0);
    
    public static void removeFromQueue(DownloadPoster poster) {
        Download download = poster.getDownload();
        download.updateTorrentCanceled();
        DOWNLOAD_QUEUE.remove(download);
    }
    
    public static void removeFromAvailableDownloads(DownloadAvailablePoster poster) {
        Episode episode = poster.getDownload().getEpisode();
        AVAILABLE_DOWNLOADS.getOrDefault(episode.getSeason().getShow(), FXCollections.emptyObservableMap()).remove(episode);
    }
    
    public static void queueEpisodeDownload(Episode episode, Magnet magnet, Consumer<Download> consumer) {
        Threading.download("queue_magnet", () -> {
            Download download = new Download(episode, magnet);
            Platform.runLater(() -> consumer.accept(download));
            addToDownloadQueue(download);
        });
    }
    
    public static void queueDownload(Download download) {
        Threading.download("queue_download", () -> addToDownloadQueue(download));
    }
    
    public static void queueEpisodeDownload(Episode episode, Consumer<Download> consumer) {
        Threading.download("queue_episode", () -> {
            Magnet magnet = getTorrent(episode);
            if (magnet != null) {
                Download download = new Download(episode, magnet);
                Platform.runLater(() -> consumer.accept(download));
                addToDownloadQueue(download);
            }
        });
    }
    
    public static void addToAvailableDownloads(Show show, List<Download> showDownloads) {
        Threading.download("available_download", () -> {
            synchronized (AVAILABLE_DOWNLOADS) {
                Map<Episode, Download> episodeDownloadMap = AVAILABLE_DOWNLOADS.computeIfAbsent(show, s -> FXCollections.observableHashMap());
                showDownloads.forEach(d -> episodeDownloadMap.put(d.getEpisode(), d));
            }
        });
    }
    
    //run on download thread
    private static Magnet getTorrent(Episode episode) {
        synchronized (AVAILABLE_DOWNLOADS) {
            return AVAILABLE_DOWNLOADS.computeIfAbsent(episode.getSeason().getShow(), s -> FXCollections.observableHashMap())
                                      .computeIfAbsent(episode, e -> new Download(e, TorrentCollector.getMagnet(e)))
                                      .getMagnet();
        }
    }
    
    //run on download thread
    private static void addToDownloadQueue(Download download) {
        Episode episode = download.getEpisode();
        Season season = episode.getSeason();
        Show show = season.getShow();
        synchronized (DOWNLOAD_QUEUE) {
            DOWNLOAD_QUEUE.add(download);
        }
        download.updateTorrentQueued();
        LOGGER.debug("Added '{}' {}x{} to download queue", show.getTitle(), season.getNumber(), episode.getNumber());
        attemptDownloadFromQueue();
    }
    
    //run on download thread
    private static void attemptDownloadFromQueue() {
        if (CURRENTLY_DOWNLOADING.get() < MAX_CONCURRENT_DOWNLOADS) {
            if (!DOWNLOAD_QUEUE.isEmpty()) {
                Download download = DOWNLOAD_QUEUE.remove(0);
                CURRENTLY_DOWNLOADING.incrementAndGet();
                
                Episode episode = download.getEpisode();
                Season season = episode.getSeason();
                Show show = season.getShow();
                
                String title = show.getTitle();
                int seasonNum = season.getNumber();
                int episodeNum = episode.getNumber();
                LOGGER.debug("Loading download of '{}' {}x{}", title, seasonNum, episodeNum);
                
                IShowProvider provider = ShowParser.getProvider(show);
                if (provider != null) {
                    Path path = provider.getFolderPath(episode);
                    download.onTorrentFinished(Threading.runOnDownload("next_download", () -> {
                        CURRENTLY_DOWNLOADING.decrementAndGet();
                        attemptDownloadFromQueue();
                    }));
                    download.onTorrentCanceled(Threading.runOnDownload("next_download", () -> {
                        CURRENTLY_DOWNLOADING.decrementAndGet();
                        attemptDownloadFromQueue();
                    }));
                    downloadTorrent(download, path);
                }
            }
        }
    }
    
    //run on download thread
    private static void downloadTorrent(Download download, Path path) {
        Task<Void> downloadTask = new DownloadTorrentTask(download, path);
        downloadTask.setOnSucceeded(e -> {
            if (!download.isCanceled()) {
                download.updateTorrentFinished();
            }
        });
        Threading.download("download_torrent", downloadTask);
    }
}
