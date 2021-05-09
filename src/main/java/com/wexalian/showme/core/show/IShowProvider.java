package com.wexalian.showme.core.show;

import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Season;
import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.data.ShowData;
import javafx.scene.image.Image;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public interface IShowProvider {
    List<ShowData> loadShowData(Supplier<Boolean> isCancelledSupplier);
    
    void saveShowData(Show show, ShowData data);
    
    Path getFolderPath(Episode episode);
    
    @Nullable
    Image loadImage(Show show);
    
    @Nullable Image loadImage(Season season);
    
    @Nullable Image loadImage(Episode episode);
    
    void saveImage(Show show, Image image);
    
    void saveImage(Season season, Image image);
    
    void saveImage(Episode episode, Image image);
    
    Path getVideoPath(Episode episode);
    
    boolean deleteVideoFile(Episode episode);
    
    boolean canSaveNewShow();
    
    @Nonnull
    String getName();
}
