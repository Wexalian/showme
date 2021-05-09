package com.wexalian.showme.core.data;

public class EpisodeData {
    private int number = 0;
    private String title = "";
    private String description = "";
    private boolean watched = false;
    private String release_date = "";
    private String last_activity = "";
    private String file_name = "";
    
    public String getLastActivity() {
        return last_activity;
    }
    
    public void setLastActivity(String last_activity) {
        this.last_activity = last_activity;
    }
    
    public int getNumber() {
        return number;
    }
    
    public void setNumber(int number) {
        this.number = number;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public boolean isWatched() {
        return watched;
    }
    
    public void setWatched(boolean watched) {
        this.watched = watched;
    }
    
    public String getReleaseDate() {
        return release_date;
    }
    
    public void setReleaseDate(String release_date) {
        this.release_date = release_date;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getFileName() {
        return file_name;
    }
    
    public void setFileName(String file_name) {
        this.file_name = file_name;
    }
    
    @Override
    public String toString() {
        return "EpisodeData{" + "number=" + number + ", title='" + title + '\'' + ", watched=" + watched + ", release_date=" + release_date + ", description='" + description + '\'' + '}';
    }
}
