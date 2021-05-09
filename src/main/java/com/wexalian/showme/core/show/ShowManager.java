package com.wexalian.showme.core.show;

import com.wexalian.jtrakt.JTraktV2;
import com.wexalian.showme.ShowMe;
import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Season;
import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.concurrent.Threading;
import com.wexalian.showme.core.concurrent.task.*;
import com.wexalian.showme.core.download.Download;
import com.wexalian.showme.core.download.DownloadManager;
import com.wexalian.showme.core.torrent.Magnet;
import com.wexalian.showme.gui.concurrent.task.data.*;
import com.wexalian.showme.util.throwing.Throwing;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class ShowManager {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final ObservableMap<String, Show> SHOWS = FXCollections.observableHashMap();
    private static final JTraktV2 TRAKT = new JTraktV2("3281b4c31326c24994ba55d28577fae5288c2cbbc1260ae2af7268da9bc413b5",
                                                       "069524dcc74072a78db8b6f74460c5eb0c3f29e33d6d135250658978f07e68eb");
    private static final Image DEFAULT_IMAGE = getImageFromJar("/image/default.png");
    private static final Image DEFAULT_IMAGE_WIDE = getImageFromJar("/image/default_wide.png");
    private static final List<IShowProvider> providers = new ArrayList<>();
    
    static {
        providers.add(new DirectoryShowProvider(Paths.get("F:")));
        providers.add(new DirectoryShowProvider(Paths.get("H:")));
        
        connectToTrakt(TRAKT);
    }
    
    public static void connectToTrakt(JTraktV2 trakt) {
        if (ShowMe.getSettings().TRAKT.ACCESS_TOKEN == null) {
            ShowMe.getSettings().TRAKT.ACCESS_TOKEN = trakt.getAuthenticationEndpoint().setupDeviceOAuth((code, url) -> {
                LOGGER.debug("===========================================================================");
                LOGGER.debug("Trakt.tv code = " + code);
                LOGGER.debug("===========================================================================");
                Throwing.accept(URI.create(url), Desktop.getDesktop()::browse);
            });
        }
    }
    
    public static void init() {
        loadShowsTemp();
    }
    
    public static Image getImageFromJar(String path) {
        URL url = Objects.requireNonNull(ShowManager.class.getResource(path));
        return new Image(url.toExternalForm());
    }
    
    public static void loadShowsTemp() {
        loadShowsFromProviders(providers);
    }
    
    public static void loadShowsFromProviders(List<IShowProvider> providers) {
        AtomicInteger providersFinished = new AtomicInteger();
        for (IShowProvider provider : providers) {
            Task<List<Show>> task = new LoadShowsTask(provider);
            task.setOnSucceeded(e -> {
                List<Show> shows = task.getValue();
                if (shows.size() > 0) {
                    
                    LOGGER.info("Loaded {} shows from provider '{}'", shows.size(), provider.getName());
                    shows.forEach(show -> SHOWS.put(show.getIds().getImdbId(), show));
                    
                    LOGGER.info("Checking shows from provider '{}' for updates", provider.getName());
                    shows.forEach(ShowManager::updateShow);
                    
                    LOGGER.info("Checking shows from provider '{}' for downloads", provider.getName());
                    shows.forEach(ShowManager::updateDownloads);
                    
                }
                if (providersFinished.incrementAndGet() == providers.size()) {
                    ShowManager.updateTrakt();
                }
            });
            Threading.core("load_providers", task);
        }
    }
    
    public static void updateShow(Show show) {
        IShowProvider provider = ShowParser.getProvider(show);
        if (provider != null) {
            Task<Void> updateShowTask = new UpdateTraktShowTask(show, provider, TRAKT);
            Threading.show("update_shows", updateShowTask);
        }
    }
    
    public static void updateDownloads(Show show) {
        Task<List<Download>> updateDownloadTask = new UpdateShowDownloadTask(show);
        updateDownloadTask.setOnSucceeded(event -> {
            List<Download> showDownloads = updateDownloadTask.getValue();
            if (showDownloads.size() > 0) {
                showDownloads.forEach(download -> {
                    Magnet magnet = download.getMagnet();
                    Episode episode = download.getEpisode();
                    Season season = episode.getSeason();
                    
                    Object[] params = new Object[]{show.getTitle(), season.getNumber(), episode.getNumber(), magnet.getSeeds(), magnet.getPeers()};
                    LOGGER.debug("Download found for '{}' {}x{} (seeds: {}, peers: {})", params);
                });
                DownloadManager.addToAvailableDownloads(show, showDownloads);
            }
        });
        Threading.show("update_downloads", updateDownloadTask);
    }
    
    public static void updateTrakt() {
        Task<Void> updateTraktTask = new UpdateTraktTask(TRAKT, providers);
        Threading.core("update_trakt", updateTraktTask);
    }
    
    public static void updateTraktWatched(Episode episode, boolean watched, Consumer<Boolean> success) {
        Task<Boolean> updateWatchedTask = new UpdateTraktEpisodeWatchedTask(episode, watched, TRAKT);
        updateWatchedTask.setOnSucceeded(e -> success.accept(updateWatchedTask.getValue()));
        Threading.show("update_trakt_watched", updateWatchedTask);
    }
    
    public static void loadImage(Show show, Consumer<Image> imageConsumer) {
        IShowProvider provider = ShowParser.getProvider(show);
        if (provider != null) {
            Task<Image> loadShowImageTask = new LoadShowImageTask(show, provider);
            loadShowImageTask.setOnSucceeded(event -> {
                Image image = loadShowImageTask.getValue();
                if (image != null) {
                    imageConsumer.accept(image);
                }
            });
            Threading.show("load_show_image", loadShowImageTask);
        }
        else imageConsumer.accept(DEFAULT_IMAGE);
    }
    
    public static void loadImage(Season season, Consumer<Image> imageConsumer) {
        Show show = season.getShow();
        IShowProvider provider = ShowParser.getProvider(show);
        if (provider != null) {
            Task<Image> loadSeasonImageTask = new LoadSeasonImageTask(season, provider);
            loadSeasonImageTask.setOnSucceeded(event -> {
                Image image = loadSeasonImageTask.getValue();
                if (image != null) {
                    imageConsumer.accept(image);
                }
            });
            Threading.show("load_season_image", loadSeasonImageTask);
        }
        imageConsumer.accept(DEFAULT_IMAGE);
    }
    
    public static void loadImage(Episode episode, Consumer<Image> imageConsumer) {
        Show show = episode.getSeason().getShow();
        IShowProvider provider = ShowParser.getProvider(show);
        if (provider != null) {
            Task<Image> loadEpisodeImageTask = new LoadEpisodeImageTask(episode, provider);
            loadEpisodeImageTask.setOnSucceeded(event -> {
                Image image = loadEpisodeImageTask.getValue();
                if (image != null) {
                    imageConsumer.accept(image);
                }
            });
            Threading.show("load_episode_image", loadEpisodeImageTask);
        }
        imageConsumer.accept(DEFAULT_IMAGE_WIDE);
    }
    
    public static void loadEpisodePath(Episode episode, Consumer<Path> fileConsumer) {
        Show show = episode.getSeason().getShow();
        IShowProvider provider = ShowParser.getProvider(show);
        if (provider != null) {
            Task<Path> loadEpisodeFileTask = new LoadEpisodeFileTask(episode, provider);
            loadEpisodeFileTask.setOnSucceeded(event -> {
                Path path = loadEpisodeFileTask.getValue();
                if (path != null) {
                    fileConsumer.accept(path);
                }
            });
            Threading.core("load_episode_path", loadEpisodeFileTask);
        }
    }
    
    public static void saveShow(Show show) {
        IShowProvider provider = ShowParser.getProvider(show);
        if (provider != null) {
            Task<Void> saveShowTask = new SaveShowTask(show, provider);
            Threading.show("save_show", saveShowTask);
        }
    }
    
    public static void deleteEpisodeFile(Episode episode) {
        Show show = episode.getSeason().getShow();
        IShowProvider provider = ShowParser.getProvider(show);
        if (provider != null) {
            Task<Boolean> deleteEpisodeFileTask = new DeleteEpisodeFileTask(episode, provider);
            Threading.show("delete_episode_file", deleteEpisodeFileTask);
        }
    }
    
    public static void shutdown() {
        // for (Show show : SHOWS) {
        //     LOGGER.debug("--- {}", show.getTitle());
        //     for (Season season : show.getSeasons().values()) {
        //         boolean runFirst = true;
        //         for (Episode episode : season.getEpisodeMap().values()) {
        //             if (Strings.isBlank(episode.getFileName())) {
        //                 if(episode.getReleaseDate() == null) continue;
        //                 if(episode.getReleaseDate().isAfter(OffsetDateTime.now())) continue;
        //
        //                 if (runFirst) {
        //                     LOGGER.debug("--- --- Season {}", season.getNumber());
        //                     runFirst = false;
        //                 }
        //
        //                 LOGGER.debug("--- --- --- Episode {}", episode.getNumber());
        //             }
        //         }
        //     }
        // }
    }
}





















