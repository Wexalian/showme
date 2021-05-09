package com.wexalian.showme.core.torrent;

import com.wexalian.showme.core.Episode;

import java.util.List;

public interface ITorrentProvider {
    
    List<Magnet> getTorrents(Episode episode);
    
    boolean hasTorrents(Episode episode);
}
