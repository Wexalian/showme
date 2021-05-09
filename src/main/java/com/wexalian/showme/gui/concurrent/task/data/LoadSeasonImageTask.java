package com.wexalian.showme.gui.concurrent.task.data;

import com.wexalian.showme.core.Season;
import com.wexalian.showme.core.image.ImageDownloader;
import com.wexalian.showme.core.show.IShowProvider;
import javafx.concurrent.Task;
import javafx.scene.image.Image;

public class LoadSeasonImageTask extends Task<Image> {
    
    private final Season season;
    private final IShowProvider provider;
    
    public LoadSeasonImageTask(Season season, IShowProvider provider) {
        this.season = season;
        this.provider = provider;
    }
    
    @Override
    protected Image call() {
        Image image = provider.loadImage(season);
        if (image == null) {
            image = ImageDownloader.downloadImage(season);
            if (image != null) {
                provider.saveImage(season, image);
            }
        }
        return image;
    }
}
