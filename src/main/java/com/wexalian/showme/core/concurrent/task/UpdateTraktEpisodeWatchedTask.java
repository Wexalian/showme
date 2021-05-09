package com.wexalian.showme.core.concurrent.task;

import com.wexalian.jtrakt.JTraktV2;
import com.wexalian.jtrakt.endpoint.TraktIds;
import com.wexalian.jtrakt.endpoint.shows.TraktShow;
import com.wexalian.jtrakt.endpoint.sync.TraktSyncUpdate;
import com.wexalian.jtrakt.endpoint.sync.history.TraktHistoryData;
import com.wexalian.showme.ShowMe;
import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Show;
import com.wexalian.showme.util.Utils;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.OffsetDateTime;

public class UpdateTraktEpisodeWatchedTask extends Task<Boolean> {
    public static Logger LOGGER = LogManager.getLogger();
    
    private final Episode episode;
    private final boolean watched;
    private final JTraktV2 trakt;
    
    public UpdateTraktEpisodeWatchedTask(Episode episode, boolean watched, JTraktV2 trakt) {
        this.episode = episode;
        this.watched = watched;
        this.trakt = trakt;
    }
    
    @Override
    protected Boolean call() {
        if (ShowMe.getSettings().TRAKT.ACCESS_TOKEN != null) {
            Show show = episode.getSeason().getShow();
            TraktShow traktShow = new TraktShow(show.getTitle(), 0, TraktIds.imdb(show.getIds().getImdbId()));
            
            TraktHistoryData data = new TraktHistoryData();
            data.addShow(traktShow).addSeason(episode.getSeason().getNumber(), OffsetDateTime.now()).addEpisode(episode.getNumber(), OffsetDateTime.now());
            
            TraktSyncUpdate update = Utils.makeTernary(watched,
                                                       () -> trakt.getSyncEndpoint().addToHistory(data, ShowMe.getSettings().TRAKT.ACCESS_TOKEN),
                                                       () -> trakt.getSyncEndpoint().removeFromHistory(data, ShowMe.getSettings().TRAKT.ACCESS_TOKEN));
            
            return update.getAdded().getEpisodes() > 0;
        }
        return false;
    }
}
