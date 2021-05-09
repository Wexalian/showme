package com.wexalian.showme.core.settings;

import com.wexalian.jtrakt.endpoint.auth.TraktAccessToken;

public class Settings {
    public Trakt TRAKT = new Trakt();
    public ShowFilters FILTERS = new ShowFilters();
    
    public static class Trakt {
        public TraktAccessToken ACCESS_TOKEN = null;
    }
    
    public static class ShowFilters {
        public boolean HIDE_COMPLETED = false;
        public boolean HIDE_NOT_COMPLETED = false;
        public boolean HIDE_ENDED_CANCELED = false;
        public boolean HIDE_CURRENTLY_AIRING = false;
        public boolean HIDE_CURRENTLY_WATCHING = false;
        public boolean HIDE_NOT_CURRENTLY_WATCHING = false;
    }
}
