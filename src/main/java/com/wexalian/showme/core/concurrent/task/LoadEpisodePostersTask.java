package com.wexalian.showme.core.concurrent.task;

import com.wexalian.showme.core.Episode;
import com.wexalian.showme.gui.component.pane.ShowPane;
import com.wexalian.showme.gui.component.poster.EpisodePoster;
import javafx.concurrent.Task;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class LoadEpisodePostersTask extends Task<List<EpisodePoster>> {
    private final ShowPane pane;
    private final Collection<Episode> episodes;
    
    public LoadEpisodePostersTask(ShowPane pane, Collection<Episode> episodes) {
        this.pane = pane;
        this.episodes = episodes;
    }
    
    @Override
    protected List<EpisodePoster> call() throws Exception {
        return episodes.stream().map(e -> new EpisodePoster(pane, e)).collect(Collectors.toList());
    }
}
