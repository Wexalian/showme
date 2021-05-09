package com.wexalian.showme.core.torrent;

import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.torrent.eztv.EZTVApi;
import com.wexalian.showme.util.Utils;
import com.wexalian.showme.util.throwing.Throwing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TorrentCollector {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final List<ITorrentProvider> torrentProviders = new ArrayList<>();
    
    private static final Set<String> TRACKERS = Utils.make(HashSet::new, s -> Throwing.run(() -> Files.lines(Path.of("", "trackers.list")).forEach(s::add)));
    
    static {
        // torrentCollectors.add(KickassApi::request);
        torrentProviders.add(EZTVApi.INSTANCE);
        // torrentCollectors.add(ETTVApi::request);
        // torrentCollectors.add(PirateBayApi::request);
        
    }
    
    public static Magnet getMagnet(Episode episode) {
        List<Magnet> magnets = new ArrayList<>();
        for (ITorrentProvider provider : torrentProviders) {
            if (provider.hasTorrents(episode)) {
                List<Magnet> episodeMagnets = provider.getTorrents(episode);
                episodeMagnets.stream().map(TorrentCollector::parseMagnet).forEach(magnets::add);
                magnets.addAll(episodeMagnets);
            }
        }
        if (!magnets.isEmpty()) {
            
            magnets.sort(Comparator.comparing(Magnet::getSeedsAndPeers).reversed());
            return magnets.get(0);
        }
        return null;
    }
    
    private static Magnet parseMagnet(Magnet magnet) {
        HashMap<String, List<String>> magnetMap = getMagnetPartMap(magnet);
        
        updateKnownTrackers(magnetMap);
        
        String title = magnet.getTitle();
        String url = createMagnetUrl(magnetMap);
        int season = magnet.getSeason();
        int episode = magnet.getEpisode();
        int seeds = magnet.getSeeds();
        int peers = magnet.getPeers();
        Object data = magnet.getData();
        
        return new Magnet(title, url, season, episode, seeds, peers, data);
    }
    
    private static String createMagnetUrl(HashMap<String, List<String>> magnetMap) {
        StringBuilder magnetBuilder = new StringBuilder("magnet:?");
        
        addToUrl(magnetBuilder, "dn", magnetMap);
        addToUrl(magnetBuilder, "xl", magnetMap);
        addToUrl(magnetBuilder, "xt", magnetMap);
        addToUrl(magnetBuilder, "ws", magnetMap);
        addToUrl(magnetBuilder, "as", magnetMap);
        addToUrl(magnetBuilder, "xs", magnetMap);
        addToUrl(magnetBuilder, "kt", magnetMap);
        addToUrl(magnetBuilder, "mp", magnetMap);
        addToUrl(magnetBuilder, "tr", TRACKERS);
        
        int lastIndex = magnetBuilder.lastIndexOf("&");
        magnetBuilder.deleteCharAt(lastIndex);
        
        return magnetBuilder.toString();
    }
    
    private static void updateKnownTrackers(HashMap<String, List<String>> magnetMap) {
        if (magnetMap.containsKey("tr")) {
            List<String> magnetTrackers = magnetMap.remove("tr");
            magnetTrackers.forEach(t -> {
                if (!TRACKERS.contains(t)) {
                    TRACKERS.add(t);
                    LOGGER.debug("Found new tracker ({})", t);
                }
            });
        }
    }
    
    private static HashMap<String, List<String>> getMagnetPartMap(Magnet magnet) {
        String[] parts = magnet.getMagnetUrl().replace("magnet:?", "").split("&");
        
        return Arrays.stream(parts).map(p -> p.split("=")).collect(HashMap::new, TorrentCollector::addPartsToMap, HashMap::putAll);
    }
    
    private static void addToUrl(StringBuilder builder, String key, Map<String, List<String>> values) {
        if (values != null) {
            addToUrl(builder, key, values.remove(key));
        }
    }
    
    private static void addToUrl(StringBuilder builder, String key, Collection<String> values) {
        if (values != null) {
            values.forEach(part -> {
                builder.append(key);
                builder.append("=");
                builder.append(part);
                builder.append("&");
            });
        }
    }
    
    private static void addPartsToMap(HashMap<String, List<String>> map, String[] parts) {
        map.computeIfAbsent(parts[0], k -> new ArrayList<>()).add(parts[1]);
    }
}
