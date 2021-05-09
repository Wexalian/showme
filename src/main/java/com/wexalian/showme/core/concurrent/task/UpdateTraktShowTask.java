package com.wexalian.showme.core.concurrent.task;

import com.wexalian.jtrakt.JTraktV2;
import com.wexalian.jtrakt.endpoint.episodes.TraktEpisode;
import com.wexalian.jtrakt.endpoint.seasons.TraktSeason;
import com.wexalian.jtrakt.endpoint.shows.TraktShow;
import com.wexalian.jtrakt.http.query.Extended;
import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Season;
import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.show.IShowProvider;
import com.wexalian.showme.core.show.ShowCache;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class UpdateTraktShowTask extends Task<Void> {
    public static Logger LOGGER = LogManager.getLogger();
    
    private final Show show;
    private final IShowProvider provider;
    private final JTraktV2 trakt;
    
    public UpdateTraktShowTask(Show show, IShowProvider provider, JTraktV2 trakt) {
        this.show = show;
        this.provider = provider;
        this.trakt = trakt;
    }
    
    @Override
    protected Void call() {
        LOGGER.debug("Checking show '{}' for updates", show.getTitle());
        
        TraktShow traktShow = trakt.getShowsEndpoint().getSummary(show.getIds().getImdbId(), Extended.FULL);
        
        if (traktShow != null) {
            int oldTmdbId = show.getIds().getTmdbId();
            int newTmdbId = traktShow.getIds().getTmdbId();
            if (oldTmdbId != newTmdbId) {
                LOGGER.debug("Updating '{}' TMDB id from {} to {}", show.getTitle(), oldTmdbId, newTmdbId);
                show.getIds().setTmdbId(newTmdbId);
                ShowCache.markDirty(show);
            }
            
            if (traktShow.getUpdatedAt().isAfter(show.getLastActivity()) || show.getSeasons().size() == 0) {
                LOGGER.info("Updating '{}' to data from '{}'", show.getTitle(), show.getLastActivity()
                                                                                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                updateShow(traktShow);
            }
        }
        
        return null;
    }
    
    private void updateShow(TraktShow traktShow) {
        Platform.runLater(() -> show.setLastActivity(traktShow.getUpdatedAt()));
        
        if (!Objects.equals(show.getTitle(), traktShow.getTitle())) {
            LOGGER.debug("Updating '{}' title to '{}'", show.getTitle(), traktShow.getTitle());
            Platform.runLater(() -> show.setTitle(traktShow.getTitle()));
        }
        if (!Objects.equals(show.getStatus(), traktShow.getStatus())) {
            LOGGER.debug("Updating '{}' status to '{}'", show.getTitle(), traktShow.getStatus());
            Platform.runLater(() -> show.setStatus(traktShow.getStatus()));
        }
        if (!Objects.equals(show.getDescription(), traktShow.getOverview())) {
            LOGGER.debug("Updating '{}' description to ", show.getTitle());
            Platform.runLater(() -> show.setDescription(traktShow.getOverview()));
        }
        
        List<TraktSeason> traktSeasons = trakt.getSeasonsEndpoint()
                                              .getSummary(show.getIds().getImdbId(), Extended.FULL_EPISODES);
        
        if (show.getSeasons().size() > traktSeasons.size()) {
            Platform.runLater(() -> {
                int diff = show.getSeasons().size() - traktSeasons.size();
                for (int i = 0; i < diff; i++) {
                    Season season = show.getSeasons().remove(show.getSeasons().size());
                    LOGGER.debug("Removing '{}' season {}", show.getTitle(), season.getNumber());
                }
            });
        }
        
        for (TraktSeason traktSeason : traktSeasons) {
            int seasonNumber = traktSeason.getNumber();
            if (seasonNumber != 0) {
                Season season = show.getSeasons().computeIfAbsent(seasonNumber, n -> {
                    LOGGER.debug("Adding '{}' season {}", show.getTitle(), seasonNumber);
                    return new Season(show, seasonNumber, traktSeason.getTitle(), traktSeason.getOverview());
                });
                
                if (!Objects.equals(season.getTitle(), traktSeason.getTitle())) {
                    LOGGER.debug("Updating '{}' season {} title to '{}'", show.getTitle(), seasonNumber, traktShow.getTitle());
                    Platform.runLater(() -> season.setTitle(traktSeason.getTitle()));
                }
                if (!Objects.equals(season.getDescription(), traktSeason.getOverview()) && !Strings.isBlank(traktSeason.getOverview())) {
                    LOGGER.debug("Updating '{}' season {} description to '{}'", show.getTitle(), seasonNumber, traktSeason
                        .getOverview());
                    Platform.runLater(() -> season.setDescription(traktSeason.getOverview()));
                }
                
                if (season.getEpisodeMap().size() > traktSeason.getEpisodeCount()) {
                    int diff = season.getEpisodeMap().size() - traktSeason.getEpisodeCount();
                    Platform.runLater(() -> {
                        for (int i = 0; i < diff; i++) {
                            Episode episode = season.getEpisodeMap().remove(season.getEpisodeMap().size());
                            LOGGER.debug("Removing '{}' season {} episode {}", show.getTitle(), season.getNumber(), episode
                                .getNumber());
                        }
                    });
                }
                for (TraktEpisode traktEpisode : traktSeason.getEpisodes()) {
                    Episode episode = season.getEpisodeMap().computeIfAbsent(traktEpisode.getNumber(), n -> {
                        LOGGER.debug("Adding '{}' season {} episode {}", show.getTitle(), seasonNumber, traktEpisode.getNumber());
                        return new Episode(season, traktEpisode.getNumber(), traktEpisode.getTitle(), false, traktEpisode
                            .getFirstAired(), traktEpisode.getUpdatedAt(), "", traktEpisode.getOverview());
                    });
                    if (traktEpisode.getUpdatedAt().isAfter(episode.getLastActivity())) {
                        Platform.runLater(() -> episode.setLastActivity(traktEpisode.getUpdatedAt()));
                        
                        if (!Objects.equals(episode.getTitle(), traktEpisode.getTitle())) {
                            LOGGER.debug("Updating '{}' season {} episode {} title to '{}'", show.getTitle(), seasonNumber, traktEpisode
                                .getNumber(), traktEpisode.getTitle());
                            Platform.runLater(() -> episode.setTitle(traktEpisode.getTitle()));
                        }
                        if (!traktEpisode.getFirstAired().isEqual(episode.getReleaseDate())) {
                            LOGGER.debug("Updating '{}' season {} episode {} release date to '{}'", show.getTitle(), seasonNumber, traktEpisode
                                .getNumber(), traktEpisode.getFirstAired());
                            Platform.runLater(() -> episode.setReleaseDate(traktEpisode.getFirstAired()));
                        }
                        if (!Objects.equals(episode.getDescription(), traktEpisode.getOverview())) {
                            LOGGER.debug("Updating '{}' season {} episode {} description to '{}'", show.getTitle(), seasonNumber, traktEpisode
                                .getNumber(), traktEpisode.getOverview());
                            Platform.runLater(() -> episode.setDescription(traktEpisode.getOverview()));
                        }
                    }
                }
            }
        }
    }
}
