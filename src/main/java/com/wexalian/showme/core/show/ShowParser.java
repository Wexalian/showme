package com.wexalian.showme.core.show;

import com.wexalian.jtrakt.endpoint.shows.TraktShow;
import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Season;
import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.data.EpisodeData;
import com.wexalian.showme.core.data.SeasonData;
import com.wexalian.showme.core.data.ShowData;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class ShowParser {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final Map<Show.Ids, Show> showIdMap = new HashMap<>();
    private static final Map<Show, IShowProvider> showProviders = new HashMap<>();
    
    public static Show parse(IShowProvider provider, ShowData showData) {
        if (showData != null) {
            Show show = parseShowData(showData);
            showIdMap.put(show.getIds(), show);
            setShowProvider(show, provider);
            for (SeasonData seasonData : showData.getSeasonData()) {
                Season season = parseSeasonData(show, seasonData);
                show.addSeason(season);
                for (EpisodeData episodeData : seasonData.getEpisodeData()) {
                    Episode episode = parseEpisodeData(season, episodeData);
                    season.addEpisode(episode);
                }
            }
            ShowCache.setupCacheForShow(show);
            if (showData.isDirty()) {
                ShowCache.markDirty(show);
            }
            return show;
        }
        return null;
    }
    
    public static void setShowProvider(Show show, IShowProvider provider) {
        showProviders.put(show, provider);
    }
    
    public static ShowData unparse(Show show) {
        ShowData showData = unparseShow(show);
        show.getSeasons().forEach((n, season) -> {
            SeasonData seasonData = unparseSeason(season);
            season.getEpisodeMap().forEach((n1, episode) -> {
                EpisodeData episodeData = unparseEpisode(episode);
                seasonData.addEpisodeData(episodeData);
            });
            showData.addSeasonData(seasonData);
        });
        return showData;
    }
    
    public static IShowProvider getProvider(Show show) {
        return showProviders.get(show);
    }
    
    public static Show getFromImdbId(String imdbId) {
        for (Map.Entry<Show.Ids, Show> idShowEntry : showIdMap.entrySet()) {
            if (idShowEntry.getKey().getImdbId().equals(imdbId)) {
                return idShowEntry.getValue();
            }
        }
        return null;
    }
    
    private static Show parseShowData(ShowData data) {
        String title = data.getTitle();
        
        ShowData.Ids dataIds = data.getIds();
        String imdbId = dataIds.getImdbId();
        int tmdbId = dataIds.getTmdbId();
        Show.Ids ids = new Show.Ids(imdbId, tmdbId);
        
        TraktShow.Status status = TraktShow.Status.fromValue(data.getStatus());
        OffsetDateTime lastActivity = parseDateTime(data.getLastActivity());
        String description = data.getDescription();
        
        return new Show(title, ids, status, lastActivity, description);
    }
    
    private static Season parseSeasonData(Show show, SeasonData data) {
        int number = data.getNumber();
        String title = data.getTitle();
        String description = data.getDescription();
        
        return new Season(show, number, title, description);
    }
    
    private static Episode parseEpisodeData(Season season, EpisodeData data) {
        int number = data.getNumber();
        String title = data.getTitle();
        boolean watched = data.isWatched();
        OffsetDateTime releaseDate = parseDateTime(data.getReleaseDate());
        OffsetDateTime lastActivity = parseDateTime(data.getLastActivity());
        String fileName = data.getFileName();
        String description = data.getDescription();
        
        return new Episode(season, number, title, watched, releaseDate, lastActivity, fileName, description);
    }
    
    private static OffsetDateTime parseDateTime(String dateTime) {
        if (Strings.isBlank(dateTime)) {
            return null;
        }
        try {
            return OffsetDateTime.ofInstant(Instant.parse(dateTime), ZoneId.of("UTC"));
        }
        catch (DateTimeParseException e) {
            LOGGER.debug("Error parsing datetime '{}'", dateTime);
            LOGGER.catching(Level.ERROR, e);
        }
        return null;
    }
    
    private static ShowData unparseShow(Show show) {
        ShowData showData = new ShowData();
        
        Show.Ids showIds = show.getIds();
        String imdbId = showIds.getImdbId();
        int tmdbId = showIds.getTmdbId();
        ShowData.Ids ids = new ShowData.Ids(imdbId, tmdbId);
        
        showData.setIds(ids);
        showData.setTitle(show.getTitle());
        showData.setDescription(show.getDescription());
        showData.setLastActivity(show.getLastActivity().format(DateTimeFormatter.ISO_INSTANT));
        showData.setStatus(show.getStatus().toString());
        
        return showData;
    }
    
    private static SeasonData unparseSeason(Season season) {
        SeasonData seasonData = new SeasonData();
        
        seasonData.setNumber(season.getNumber());
        seasonData.setDescription(season.getDescription());
        seasonData.setTitle(season.getTitle());
        
        return seasonData;
    }
    
    private static EpisodeData unparseEpisode(Episode episode) {
        EpisodeData episodeData = new EpisodeData();
        
        episodeData.setNumber(episode.getNumber());
        episodeData.setTitle(episode.getTitle());
        episodeData.setDescription(episode.getDescription());
        episodeData.setReleaseDate(episode.getReleaseDate() == null ? "" : episode.getReleaseDate()
                                                                                  .format(DateTimeFormatter.ISO_INSTANT));
        episodeData.setLastActivity(episode.getLastActivity() == null ? "" : episode.getLastActivity()
                                                                                    .format(DateTimeFormatter.ISO_INSTANT));
        episodeData.setFileName(episode.getFileName());
        episodeData.setWatched(episode.isWatched());
        
        return episodeData;
    }
}