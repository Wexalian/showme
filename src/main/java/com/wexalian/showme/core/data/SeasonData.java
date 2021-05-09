package com.wexalian.showme.core.data;

import java.util.ArrayList;
import java.util.List;

public class SeasonData {
    private final List<EpisodeData> episodes = new ArrayList<>();
    private int number = 0;
    private String title = "";
    private String description = "";
    
    public void addEpisodeData(EpisodeData episodeData) {
        episodes.add(episodeData);
    }
    
    public int getNumber() {
        return number;
    }
    
    public void setNumber(int number) {
        this.number = number;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<EpisodeData> getEpisodeData() {
        return episodes;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Override
    public String toString() {
        return "SeasonData{" + "number=" + number + ", title='" + title + '\'' + ", description='" + description + '\'' + ", episodes=" + episodes + '}';
    }
}
