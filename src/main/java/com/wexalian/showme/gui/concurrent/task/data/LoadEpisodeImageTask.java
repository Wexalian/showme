package com.wexalian.showme.gui.concurrent.task.data;

import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.image.ImageDownloader;
import com.wexalian.showme.core.show.IShowProvider;
import javafx.concurrent.Task;
import javafx.scene.image.Image;

public class LoadEpisodeImageTask extends Task<Image> {
    
    private final Episode episode;
    private final IShowProvider provider;
    
    public LoadEpisodeImageTask(Episode episode, IShowProvider provider) {
        this.episode = episode;
        this.provider = provider;
    }
    
    @Override
    protected Image call() {
        Image image = provider.loadImage(episode);
        if (image == null) {
            image = ImageDownloader.downloadImage(episode);
            if (image != null) {
                provider.saveImage(episode, image);
            }
        }
        return image;
    }
}
