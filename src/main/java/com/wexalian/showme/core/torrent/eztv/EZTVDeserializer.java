package com.wexalian.showme.core.torrent.eztv;

import com.google.gson.*;
import com.wexalian.showme.core.torrent.Magnet;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EZTVDeserializer implements JsonDeserializer<List<Magnet>> {
    @Override
    public List<Magnet> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<Magnet> magnets = new ArrayList<>();
        if (json.getAsJsonObject().has("torrents")) {
            JsonArray torrentsArray = json.getAsJsonObject().get("torrents").getAsJsonArray();
            
            for (JsonElement e : torrentsArray) {
                JsonObject torrentObj = e.getAsJsonObject();
                
                String title = torrentObj.get("title").getAsString();
                String magnet_url = torrentObj.get("magnet_url").getAsString();
                int season = torrentObj.get("season").getAsInt();
                int episode = torrentObj.get("episode").getAsInt();
                int seeds = torrentObj.get("seeds").getAsInt();
                int peers = torrentObj.get("peers").getAsInt();
                long size_bytes = torrentObj.get("size_bytes").getAsLong();
                
                magnets.add(new Magnet(title, magnet_url, season, episode, seeds, peers));
            }
        }
        return magnets;
    }
}
