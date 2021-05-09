package com.wexalian.showme.core.data;

import java.util.ArrayList;
import java.util.List;

public class ShowData {
    private Ids ids;
    private String title = "";
    private String status = "";
    private String last_activity = "";
    private String description = "";
    private List<SeasonData> seasons = new ArrayList<>();
    private transient boolean dirty = false;
    
    public void addSeasonData(SeasonData data) {
        seasons.add(data);
    }
    
    public void setDirty() {
        this.dirty = true;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getLastActivity() {
        return last_activity;
    }
    
    public void setLastActivity(String last_activity) {
        this.last_activity = last_activity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<SeasonData> getSeasonData() {
        return seasons;
    }
    
    public boolean isDirty() {
        return dirty;
    }
    
    public Ids getIds() {
        return ids;
    }
    
    public void setIds(Ids ids) {
        this.ids = ids;
    }
    
    @Override
    public String toString() {
        return "ShowData{" + "title='" + title + '\'' + ", ids='" + ids + '\'' + ", status='" + status + '\'' + ", last_activity=" + last_activity + ", description='" + description + '\'' + ", seasons=" + seasons + '}';
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
    }
}
