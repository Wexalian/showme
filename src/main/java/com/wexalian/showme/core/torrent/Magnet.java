package com.wexalian.showme.core.torrent;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Magnet {
    private final String title;
    private final String magnetUrl;
    private final int season;
    private final int episode;
    private final int seeds;
    private final int peers;
    
    private final Object data;
    
    public Magnet(String title, String magnetUrl, int season, int episode, int seeds, int peers) {
        this(title, magnetUrl, season, episode, seeds, peers, null);
    }
    
    public Magnet(String title, String magnetUrl, int season, int episode, int seeds, int peers, Object data) {
        this.title = title;
        this.magnetUrl = magnetUrl;
        this.season = season;
        this.episode = episode;
        this.seeds = seeds;
        this.peers = peers;
        this.data = data;
    }
    
    public Object getData() {
        return data;
    }
    
    public int getEpisode() {
        return episode;
    }
    
    public String getMagnetUrl() {
        return magnetUrl;
    }
    
    public int getSeason() {
        return season;
    }
    
    public int getSeedsAndPeers() {
        return getSeeds() + getPeers();
    }
    
    public int getSeeds() {
        return seeds;
    }
    
    public int getPeers() {
        return peers;
    }
    
    public String getTitle() {
        return title;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("title", title)
                                        .append("season", season)
                                        .append("episode", episode)
                                        .toString();
    }
}
