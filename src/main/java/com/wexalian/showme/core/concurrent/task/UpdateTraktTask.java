package com.wexalian.showme.core.concurrent.task;

import com.wexalian.jtrakt.JTraktV2;
import com.wexalian.jtrakt.endpoint.TraktIds;
import com.wexalian.jtrakt.endpoint.TraktItemsType;
import com.wexalian.jtrakt.endpoint.TraktWatchedItem;
import com.wexalian.jtrakt.endpoint.shows.TraktShow;
import com.wexalian.jtrakt.endpoint.sync.history.TraktHistoryData;
import com.wexalian.jtrakt.endpoint.sync.watchlist.TraktWatchlistItem;
import com.wexalian.jtrakt.http.query.Extended;
import com.wexalian.showme.ShowMe;
import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Season;
import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.show.IShowProvider;
import com.wexalian.showme.core.show.ShowCache;
import com.wexalian.showme.core.show.ShowManager;
import com.wexalian.showme.core.show.ShowParser;
import com.wexalian.showme.util.LazyObject;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateTraktTask extends Task<Void> {
    public static Logger LOGGER = LogManager.getLogger();
    
    private final JTraktV2 trakt;
    private final List<IShowProvider> providers;
    
    public UpdateTraktTask(JTraktV2 trakt, List<IShowProvider> providers) {
        this.trakt = trakt;
        this.providers = providers;
    }
    
    @Override
    protected Void call() {
        if (ShowMe.getSettings().TRAKT.ACCESS_TOKEN != null) {
            List<TraktWatchedItem> watchedItems = trakt.getSyncEndpoint()
                                                       .getWatchedItems(TraktItemsType.SHOWS, Extended.FULL, ShowMe.getSettings().TRAKT.ACCESS_TOKEN);
            
            List<Episode> watchedEpisodes = updateLocalWatched(watchedItems);
            updateTraktWatched(watchedEpisodes);
            
            List<TraktWatchlistItem> watchlist = trakt.getUsersEndpoint()
                                                      .getWatchlist("me", TraktItemsType.SHOWS, "rank", null, null, ShowMe.getSettings().TRAKT.ACCESS_TOKEN);
            
            updateShowsFromWatchlist(watchlist);
        }
        return null;
    }
    
    private void updateShowsFromWatchlist(List<TraktWatchlistItem> watchlist) {
        for (TraktWatchlistItem item : watchlist) {
            TraktShow traktShow = item.getShow();
            
            if (traktShow != null) {
                String imdbId = traktShow.getIds().getImdbId();
                int tmdbId = traktShow.getIds().getTmdbId();
                
                if (!ShowManager.SHOWS.containsKey(imdbId)) {
                    IShowProvider provider = null;
                    for (IShowProvider showProvider : providers) {
                        if(showProvider.canSaveNewShow()) {
                            provider = showProvider;
                            break;
                        }
                    }
                    
                    Show show = new Show(traktShow.getTitle(), new Show.Ids(imdbId, tmdbId));
                    
                    ShowCache.setupCacheForShow(show);
                    ShowParser.setShowProvider(show, provider);
                    
                    Platform.runLater(() -> ShowManager.SHOWS.put(imdbId, show));
                    
                    ShowManager.updateShow(show);
                    ShowManager.updateDownloads(show);
                }
            }
        }
    }
    
    private void updateTraktWatched(List<Episode> items) {
        var data = new LazyObject<>(TraktHistoryData::new);
        
        ShowManager.SHOWS.forEach((id, show) -> {
            Map<Integer, List<Integer>> seasonNumbers = show.stream()
                                                            .filter(season -> !getEpisodeNumbers(season, items).isEmpty())
                                                            .collect(Collectors.toMap(Season::getNumber, season -> getEpisodeNumbers(season, items)));
            
            if (!seasonNumbers.isEmpty()) {
                TraktShow traktShow = new TraktShow(show.getTitle(), 0, TraktIds.imdb(show.getIds().getImdbId()));
                TraktHistoryData.ShowData showData = data.get().addShow(traktShow);
                seasonNumbers.forEach((seasonNum, episodeNums) -> {
                    if (!episodeNums.isEmpty()) {
                        var seasonData = showData.addSeason(seasonNum, OffsetDateTime.now());
                        episodeNums.forEach(num -> seasonData.addEpisode(num, OffsetDateTime.now()));
                    }
                });
            }
        });
        if (!data.isEmpty()) {
            trakt.getSyncEndpoint().addToHistory(data.get(), ShowMe.getSettings().TRAKT.ACCESS_TOKEN);
        }
    }
    
    private List<Integer> getEpisodeNumbers(Season season, List<Episode> items) {
        return season.stream().filter(episode -> episode.isWatched() && !items.contains(episode)).map(Episode::getNumber).collect(Collectors.toList());
    }
    
    private List<Episode> updateLocalWatched(List<TraktWatchedItem> items) {
        List<Episode> watchedEpisodes = new ArrayList<>();
        items.forEach(item -> {
            Show show = ShowParser.getFromImdbId(item.getShow().getIds().getImdbId());
            if (show != null) {
                watchedEpisodes.addAll(updateLocalShow(item, show));
            }
        });
        return watchedEpisodes;
    }
    
    private List<Episode> updateLocalShow(TraktWatchedItem wShow, Show show) {
        List<Episode> watchedEpisodes = new ArrayList<>();
        wShow.getSeasons().forEach(wSeason -> {
            Season season = show.getSeason(wSeason.getNumber());
            if (season != null) {
                watchedEpisodes.addAll(updateLocalSeason(wSeason, season));
            }
        });
        return watchedEpisodes;
    }
    
    private List<Episode> updateLocalSeason(TraktWatchedItem.Season wSeason, Season season) {
        List<Episode> watchedEpisodes = new ArrayList<>();
        wSeason.getEpisodes().forEach(wEpisode -> {
            Episode episode = season.getEpisode(wEpisode.getNumber());
            if (episode != null) {
                watchedEpisodes.add(episode);
                if (!episode.isWatched()) {
                    episode.setWatched(true);
                }
            }
        });
        return watchedEpisodes;
    }
}
