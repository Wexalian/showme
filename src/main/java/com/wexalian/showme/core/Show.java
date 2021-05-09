package com.wexalian.showme.core;

import com.wexalian.jtrakt.endpoint.shows.TraktShow;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Show {
    private final ObjectProperty<Ids> ids = new SimpleObjectProperty<>();
    private final StringProperty title = new SimpleStringProperty();
    private final ObjectProperty<TraktShow.Status> status = new SimpleObjectProperty<>();
    private final ObjectProperty<OffsetDateTime> lastActivity = new SimpleObjectProperty<>();
    private final StringProperty description = new SimpleStringProperty();
    private final MapProperty<Integer, Season> seasons = new SimpleMapProperty<>(FXCollections.observableHashMap());
    
    private final List<Consumer<Episode>> episodeAddedConsumers = new ArrayList<>();
    private final List<Consumer<Episode>> episodeWatchedChangedConsumers = new ArrayList<>();
    
    public Show(String title, Ids ids) {
        this.title.set(title);
        this.ids.set(ids);
    }
    
    public Show(String title, Ids ids, TraktShow.Status status, OffsetDateTime lastActivity, String description) {
        this.title.set(title);
        this.ids.set(ids);
        this.status.set(status);
        this.lastActivity.set(lastActivity);
        this.description.set(description);
    }
    
    public Season getSeason(int number) {
        return seasons.get(number);
    }
    
    public void addSeason(Season season) {
        seasons.put(season.getNumber(), season);
    }
    
    public ObservableValue<String> titleValue() {
        return title;
    }
    
    public ObservableValue<TraktShow.Status> statusValue() {
        return status;
    }
    
    public ObservableValue<OffsetDateTime> lastActivityValue() {
        return lastActivity;
    }
    
    public ObservableValue<String> descriptionValue() {
        return description;
    }
    
    public boolean areEpisodeAvailable() {
        for (Season season : seasons.values()) {
            if (season.areEpisodesAvailable()) {
                return true;
            }
        }
        return false;
    }
    
    public void runOnEpisodeAdded(Episode episode) {
        episodeAddedConsumers.forEach(c -> c.accept(episode));
    }
    
    public void addOnEpisodeAdded(Consumer<Episode> consumer) {
        episodeAddedConsumers.add(consumer);
    }
    
    public void runEpisodeWatchedChanged(Episode episode) {
        episodeWatchedChangedConsumers.forEach(c -> c.accept(episode));
    }
    
    public void addOnEpisodeWatchedChanged(Consumer<Episode> consumer) {
        episodeWatchedChangedConsumers.add(consumer);
    }
    
    public boolean areEpisodesWatched() {
        for (Season season : seasons.values()) {
            if (season.areEpisodesWatched()) {
                return true;
            }
        }
        return false;
    }
    
    public Stream<Season> stream() {
        return seasons.values().stream();
    }
    
    public ObservableValue<Ids> getIdsValue() {
        return ids;
    }
    
    public String getTitle() {
        return title.get();
    }
    
    public void setTitle(String title) {
        this.title.set(title);
    }
    
    public Ids getIds() {
        return ids.get();
    }
    
    public TraktShow.Status getStatus() {
        return status.get();
    }
    
    public void setStatus(TraktShow.Status status) {
        this.status.set(status);
    }
    
    public OffsetDateTime getLastActivity() {
        OffsetDateTime lastActivity = this.lastActivity.get();
        return lastActivity == null ? OffsetDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.of("UTC")) : lastActivity;
    }
    
    public void setLastActivity(OffsetDateTime lastActivity) {
        this.lastActivity.set(lastActivity);
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String description) {
        this.description.set(description);
    }
    
    public ObservableMap<Integer, Season> getSeasons() {
        return seasons;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(ids);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Show show = (Show) o;
        return Objects.equals(ids, show.ids);
    }
    
    public static class Ids {
        private final String imdbId;
        private int tmdbId;
        
        public Ids(String imdbId) {
            this(imdbId, -1);
        }
        
        public Ids(String imdbId, int tmdbId) {
            this.imdbId = imdbId;
            this.tmdbId = tmdbId;
        }
        
        public String getImdbId() {
            return imdbId;
        }
        
        public int getTmdbId() {
            return tmdbId;
        }
        
        public void setTmdbId(int tmdbId) {
            this.tmdbId = tmdbId;
        }
        
        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(imdbId);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Ids ids = (Ids) o;
            return com.google.common.base.Objects.equal(imdbId, ids.imdbId);
        }
        
        @Override
        public String toString() {
            return "Ids{" + "imdbId='" + imdbId + '\'' + ", tmdbId=" + tmdbId + '}';
        }
    }
}
