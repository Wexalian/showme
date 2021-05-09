package com.wexalian.showme.core.torrent.eztv;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.gson.reflect.TypeToken;
import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.torrent.ITorrentProvider;
import com.wexalian.showme.core.torrent.Magnet;
import com.wexalian.showme.util.JsonUtils;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EZTVApi implements ITorrentProvider {
    public static final ITorrentProvider INSTANCE = new EZTVApi();
    
    private static final Pattern TORRENT_PATTERN = Pattern.compile("s(\\d{1,2})e(\\d{1,2})");
    private static final TypeToken<List<Magnet>> SHOW_TORRENTS_TYPE_TOKEN = new TypeToken<>() {};
    private static final Map<Show, Table<Integer, Integer, List<Magnet>>> TORRENT_TABLES = new HashMap<>();
    
    private final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).followRedirects(HttpClient.Redirect.NORMAL).build();
    
    @Override
    public List<Magnet> getTorrents(Episode episode) {
        Show show = episode.getSeason().getShow();
        if (TORRENT_TABLES.containsKey(show)) {
            var torrentTable = TORRENT_TABLES.get(show);
            return torrentTable.get(episode.getSeason().getNumber(), episode.getNumber());
        }
        else return requestTorrents(episode);
    }
    
    @Override
    public boolean hasTorrents(Episode episode) {
        Show show = episode.getSeason().getShow();
        if (TORRENT_TABLES.containsKey(show)) {
            var torrentTable = TORRENT_TABLES.get(show);
            return torrentTable.contains(episode.getSeason().getNumber(), episode.getNumber());
        }
        else return !requestTorrents(episode).isEmpty();
        
    }
    
    @Nonnull
    public List<Magnet> requestTorrents(Episode episode) {
        Show show = episode.getSeason().getShow();
        String url = "https://eztv.re/api/get-torrents?imdb_id=" + show.getIds().getImdbId().replace("tt", "");
        List<Magnet> showMagnets = getAndParse(url, SHOW_TORRENTS_TYPE_TOKEN);
        
        var torrentTable = TORRENT_TABLES.computeIfAbsent(show, k -> TreeBasedTable.create());
        
        List<Magnet> episodeMagnets = new ArrayList<>();
        for (Magnet magnet : showMagnets) {
            int seasonNum = magnet.getSeason();
            int episodeNum = magnet.getEpisode();
            
            if (seasonNum == 0 || episodeNum == 0) {
                Matcher matcher = TORRENT_PATTERN.matcher(magnet.getTitle().toLowerCase());
                if (matcher.matches()) {
                    seasonNum = Integer.parseInt(matcher.group(1));
                    episodeNum = Integer.parseInt(matcher.group(2));
                }
            }
            
            List<Magnet> magnets = torrentTable.get(seasonNum, episodeNum);
            if (magnets == null) {
                magnets = new ArrayList<>();
                torrentTable.put(seasonNum, episodeNum, magnets);
            }
            magnets.add(magnet);
            
            if (seasonNum == episode.getSeason().getNumber()) {
                if (episodeNum == episode.getNumber()) {
                    episodeMagnets.add(magnet);
                }
            }
        }
        return episodeMagnets;
    }
    
    public <T> T getAndParse(String url, TypeToken<T> typeToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().GET().uri(URI.create(url));
        
        return client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                     .thenApply(HttpResponse::body)
                     .thenApply(s -> JsonUtils.fromJson(s, typeToken))
                     .join();
    }
}
