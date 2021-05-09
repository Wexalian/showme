package com.wexalian.showme.core.concurrent.task;

import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Season;
import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.download.Download;
import com.wexalian.showme.core.torrent.Magnet;
import com.wexalian.showme.core.torrent.TorrentCollector;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.List;

public class UpdateShowDownloadTask extends Task<List<Download>> {
    public static Logger LOGGER = LogManager.getLogger();
    
    private final Show show;
    
    public UpdateShowDownloadTask(Show show) {
        this.show = show;
    }
    
    @Override
    protected List<Download> call() {
        ArrayList<Download> downloads = new ArrayList<>();
        for (Season season : show.getSeasons().values()) {
            for (Episode episode : season.getEpisodeMap().values()) {
                if (Strings.isEmpty(episode.getFileName())) {
                    Magnet magnet = TorrentCollector.getMagnet(episode);
                    if (magnet != null) {
                        Download download = new Download(episode, magnet);
                        downloads.add(download);
                    }
                }
            }
        }
        return downloads;
    }
}
