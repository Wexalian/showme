package com.wexalian.showme.gui.concurrent.task.data;

import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.show.IShowProvider;
import javafx.concurrent.Task;

import java.nio.file.Path;

public class LoadEpisodeFileTask extends Task<Path> {
    
    private final Episode episode;
    private final IShowProvider provider;
    
    public LoadEpisodeFileTask(Episode episode, IShowProvider provider) {
        this.episode = episode;
        this.provider = provider;
    }
    
    @Override
    protected Path call() {
        return provider.getVideoPath(episode);
    }
}
