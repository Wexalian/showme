package com.wexalian.showme.core;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Episode {
    private final Season season;
    private final int number;
    private final StringProperty title = new SimpleStringProperty();
    private final BooleanProperty watched = new SimpleBooleanProperty();
    private final ObjectProperty<OffsetDateTime> releaseDate = new SimpleObjectProperty<>();
    private final ObjectProperty<OffsetDateTime> lastActivity = new SimpleObjectProperty<>();
    private final StringProperty fileName = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    
    public Episode(Season season, int number, String title, boolean watched, OffsetDateTime releaseDate, OffsetDateTime lastActivity, String fileName, String description) {
        this.season = season;
        this.number = number;
        setTitle(title);
        setWatched(watched);
        setReleaseDate(releaseDate);
        setLastActivity(lastActivity);
        setFileName(fileName);
        setDescription(description);
    }
    
    public ObjectProperty<OffsetDateTime> lastActivityProperty() {
        return lastActivity;
    }
    
    public ObservableValue<String> titleValue() {
        return title;
    }
    
    public ObservableValue<Boolean> watchedValue() {
        return watched;
    }
    
    public ObservableValue<OffsetDateTime> releaseDateValue() {
        return releaseDate;
    }
    
    public ObservableValue<String> descriptionValue() {
        return description;
    }
    
    public ObservableValue<String> fileNameValue() {
        return fileName;
    }
    
    public StringProperty titleProperty() {
        return title;
    }
    
    public BooleanProperty watchedProperty() {
        return watched;
    }
    
    public ObjectProperty<OffsetDateTime> releaseDateProperty() {
        return releaseDate;
    }
    
    public StringProperty fileNameProperty() {
        return fileName;
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    public Episode getNextEpisode() {
        return season.getEpisode(number + 1);
    }
    
    public OffsetDateTime getLastActivity() {
        return lastActivity.get();
    }
    
    public void setLastActivity(OffsetDateTime lastActivity) {
        this.lastActivity.set(lastActivity);
    }
    
    public int getNumber() {
        return number;
    }
    
    public String getTitle() {
        return title.get();
    }
    
    public void setTitle(String title) {
        this.title.set(title);
    }
    
    public boolean isWatched() {
        return watched.get();
    }
    
    public void setWatched(boolean watched) {
        this.watched.set(watched);
        season.getShow().runEpisodeWatchedChanged(this);
    }
    
    public OffsetDateTime getReleaseDate() {
        return releaseDate.get();
    }
    
    public void setReleaseDate(OffsetDateTime releaseDate) {
        this.releaseDate.set(releaseDate);
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String description) {
        this.description.set(description);
    }
    
    public Season getSeason() {
        return season;
    }
    
    public String getFileName() {
        return fileName.get();
    }
    
    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(season, number);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Episode episode = (Episode) o;
        return number == episode.number && Objects.equals(season, episode.season);
    }
}
