package com.wexalian.showme.gui.concurrent.task.data;

import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.image.ImageDownloader;
import com.wexalian.showme.core.show.IShowProvider;
import javafx.concurrent.Task;
import javafx.scene.image.Image;

public class LoadShowImageTask extends Task<Image> {
    
    private final Show show;
    private final IShowProvider provider;
    
    public LoadShowImageTask(Show show, IShowProvider provider) {
        this.show = show;
        this.provider = provider;
    }
    
    @Override
    protected Image call() {
        Image image = provider.loadImage(show);
        if (image == null) {
            image = ImageDownloader.downloadImage(show);
            if (image != null) {
                provider.saveImage(show, image);
            }
        }
        return image;
    }
}
