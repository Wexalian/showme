package com.wexalian.showme.core.show;

import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Season;
import com.wexalian.showme.core.Show;
import javafx.collections.MapChangeListener;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ShowCache {
    
    private static final Set<Show> currentlyDirtyShows = new HashSet<>();
    
    public static void setupCacheForShow(Show show) {
        show.getIdsValue().addListener((obs, oldV, newV) -> markDirty(oldV, newV, show));
        show.titleValue().addListener((obs, oldV, newV) -> markDirty(oldV, newV, show));
        show.descriptionValue().addListener((obs, oldV, newV) -> markDirty(oldV, newV, show));
        show.lastActivityValue().addListener((obs, oldV, newV) -> markDirty(oldV, newV, show));
        show.statusValue().addListener((obs, oldV, newV) -> markDirty(oldV, newV, show));
        show.getSeasons().addListener((MapChangeListener<Integer, Season>) (c) -> {
            if (c.wasAdded()) {
                setupCacheForSeason(c.getValueAdded());
            }
        });
        
        show.getSeasons().forEach((n, season) -> setupCacheForSeason(season));
    }
    
    public static void markDirty(Show show) {
        synchronized (currentlyDirtyShows) {
            currentlyDirtyShows.add(show);
        }
    }
    
    public static void shutdown() {
        synchronized (currentlyDirtyShows) {
            currentlyDirtyShows.forEach(ShowManager::saveShow);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <T> void markDirty(T oldV, T newV, Show show) {
        if (oldV instanceof String && newV == null) {
            newV = (T) "";
        }
        if (!Objects.equals(oldV, newV)) {
            synchronized (currentlyDirtyShows) {
                currentlyDirtyShows.add(show);
            }
        }
    }
    
    private static void setupCacheForSeason(Season season) {
        season.titleValue().addListener((obs, oldV, newV) -> markDirty(oldV, newV, season.getShow()));
        season.descriptionValue().addListener((obs, oldV, newV) -> markDirty(oldV, newV, season.getShow()));
        season.getEpisodeMap().addListener((MapChangeListener<Integer, Episode>) (c) -> {
            if (c.wasAdded()) {
                setupCacheForEpisode(c.getValueAdded());
            }
        });
        
        season.getEpisodeMap().forEach((n1, episode) -> setupCacheForEpisode(episode));
    }
    
    private static void setupCacheForEpisode(Episode episode) {
        episode.titleValue().addListener((obs, oldV, newV) -> markDirty(oldV, newV, episode.getSeason().getShow()));
        episode.fileNameValue().addListener((obs, oldV, newV) -> markDirty(oldV, newV, episode.getSeason().getShow()));
        episode.watchedValue().addListener((obs, oldV, newV) -> markDirty(oldV, newV, episode.getSeason().getShow()));
        episode.descriptionValue().addListener((obs, oldV, newV) -> markDirty(oldV, newV, episode.getSeason().getShow()));
        episode.releaseDateValue().addListener((obs, oldV, newV) -> markDirty(oldV, newV, episode.getSeason().getShow()));
    }
}
