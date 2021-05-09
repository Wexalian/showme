package com.wexalian.showme.core.concurrent.task;

import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.show.IShowProvider;
import javafx.concurrent.Task;

public class DeleteEpisodeFileTask extends Task<Boolean> {
    
    private final Episode episode;
    private final IShowProvider provider;
    
    public DeleteEpisodeFileTask(Episode episode, IShowProvider provider) {
        this.episode = episode;
        this.provider = provider;
    }
    
    @Override
    protected Boolean call() {
        return provider.deleteVideoFile(episode);
    }
}
