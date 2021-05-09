package com.wexalian.showme.core;

import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.stream.Stream;

public class Season {
    private final Show show;
    private final int number;
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObservableMap<Integer, Episode> episodes = new SimpleMapProperty<>(FXCollections.observableHashMap());
    
    public Season(Show show, int number, String title, String description) {
        this.show = show;
        this.number = number;
        this.title.set(title);
        this.description.set(description);
    }
    
    public boolean areEpisodesAvailable() {
        for (Episode episode : episodes.values()) {
            if (!episode.isWatched()) {
                if (episode.getReleaseDate() != null && episode.getReleaseDate().isBefore(OffsetDateTime.now())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void addEpisode(Episode episode) {
        episodes.put(episode.getNumber(), episode);
        show.runOnEpisodeAdded(episode);
    }
    
    public Episode getEpisode(int number) {
        return episodes.get(number);
    }
    
    public ObservableValue<String> descriptionValue() {
        return description;
    }
    
    public ObservableValue<String> titleValue() {
        return title;
    }
    
    public boolean areEpisodesWatched() {
        for (Episode episode : episodes.values()) {
            if (episode.isWatched()) {
                return true;
            }
        }
        return false;
    }
    
    public Show getShow() {
        return show;
    }
    
    public int getNumber() {
        return number;
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String description) {
        this.description.set(description);
    }
    
    public String getTitle() {
        return title.get();
    }
    
    public void setTitle(String title) {
        this.title.set(title);
    }
    
    public ObservableMap<Integer, Episode> getEpisodeMap() {
        return episodes;
    }
    
    public Stream<Episode> stream() {
        return episodes.values().stream();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(show, number);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Season season = (Season) o;
        return number == season.number && Objects.equals(show, season.show);
    }
    
}
