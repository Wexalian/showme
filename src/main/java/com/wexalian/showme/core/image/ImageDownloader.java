package com.wexalian.showme.core.image;

import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.entities.TvEpisode;
import com.uwetrottmann.tmdb2.entities.TvSeason;
import com.uwetrottmann.tmdb2.entities.TvShow;
import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Season;
import com.wexalian.showme.core.Show;
import javafx.scene.image.Image;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import retrofit2.Response;

import java.io.IOException;
import java.net.URL;

public class ImageDownloader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String IMAGE_URL = "https://image.tmdb.org/t/p/w780";
    private static final String TMDB_API_KEY = "83fed95ccc330d5b194e5039d40387d6";
    private static final Tmdb TMDB = new Tmdb(TMDB_API_KEY);
    
    public static Image downloadImage(Show show) {
        int tmdbId = show.getIds().getTmdbId();
        if (tmdbId >= 0) {
            try {
                Response<TvShow> response = TMDB.tvService().tv(tmdbId, "en-US").execute();
                if (response.isSuccessful() && response.body() != null) {
                    String path = response.body().poster_path;
                    if (!Strings.isBlank(path)) {
                        URL url = new URL(IMAGE_URL + path);
                        return new Image(url.openStream());
                    }
                }
            }
            catch (IOException e) {
                LOGGER.error("Error downloading image for '{}'", show.getTitle());
                LOGGER.catching(Level.ERROR, e);
            }
        }
        return null;
    }
    
    public static Image downloadImage(Season season) {
        Show show = season.getShow();
        int tmdbId = show.getIds().getTmdbId();
        if (tmdbId >= 0) {
            try {
                Response<TvSeason> response = TMDB.tvSeasonsService()
                                                  .season(tmdbId, season.getNumber(), "en-US")
                                                  .execute();
                if (response.isSuccessful() && response.body() != null) {
                    String path = response.body().poster_path;
                    if (!Strings.isBlank(path)) {
                        URL url = new URL(IMAGE_URL + path);
                        return new Image(url.openStream());
                    }
                }
            }
            catch (IOException e) {
                LOGGER.error("Error downloading image for '{}' season {}", show.getTitle(), season.getNumber());
                LOGGER.catching(Level.ERROR, e);
            }
        }
        return null;
    }
    
    public static Image downloadImage(Episode episode) {
        Season season = episode.getSeason();
        Show show = season.getShow();
        
        int tmdbId = show.getIds().getTmdbId();
        if (tmdbId >= 0) {
            try {
                Response<TvEpisode> response = TMDB.tvEpisodesService()
                                                   .episode(tmdbId, season.getNumber(), episode.getNumber(), "en-US")
                                                   .execute();
                if (response.isSuccessful() && response.body() != null) {
                    String path = response.body().still_path;
                    if (!Strings.isBlank(path)) {
                        URL url = new URL(IMAGE_URL + path);
                        return new Image(url.openStream());
                    }
                }
            }
            catch (IOException e) {
                LOGGER.error("Error downloading image for '{}' season {} episode {}", show.getTitle(), season.getNumber(), episode
                    .getNumber());
                LOGGER.catching(Level.ERROR, e);
            }
        }
        return null;
    }
}
