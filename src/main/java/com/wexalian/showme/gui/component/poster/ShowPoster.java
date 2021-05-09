package com.wexalian.showme.gui.component.poster;

import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.show.ShowManager;
import com.wexalian.showme.gui.controller.MainController;
import com.wexalian.showme.util.FXMLUtils;
import javafx.fxml.FXML;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShowPoster extends AnchorPane {
    public static final Logger LOGGER = LogManager.getLogger();
    
    private static final ColorAdjust GRAYSCALE_EFFECT = new ColorAdjust(0, -1, -0.75, 0);
    
    private final MainController controller;
    private final Show show;
    // private final
    
    @FXML
    private ImageView imageView;
    
    @FXML
    private Text title;
    
    public ShowPoster(MainController controller, Show show) {
        this.controller = controller;
        this.show = show;
        
        FXMLUtils.loadAsController(this, "/fxml/poster/show.fxml");
    }
    
    @FXML
    public void initialize() {
        ShowManager.loadImage(show, imageView::setImage);
        title.textProperty().bind(show.titleValue());
        setOnMouseClicked(e -> controller.openShow(show));
        
        show.addOnEpisodeAdded(e -> updateWatched());
        show.addOnEpisodeWatchedChanged(e -> updateWatched());
        updateWatched();
        // show.getSeasons().addListener((MapChangeListener<Integer, Season>) sc -> {
        //     if (sc.wasAdded()) {
        //         sc.getValueAdded().getEpisodes().addListener((MapChangeListener<Integer, Episode>) ec -> {
        //             if (ec.wasAdded()) {
        //                 ec.getValueAdded().watchedValue().addListener((obs, oldV, newV) -> this.updateWatched());
        //             }
        //             updateWatched();
        //         });
        //     }
        //     updateWatched();
        // });
    }
    
    private void updateWatched() {
        if (!show.areEpisodeAvailable()) {
            imageView.setEffect(GRAYSCALE_EFFECT);
        }
        else {
            imageView.setEffect(null);
        }
        controller.updateShowVisibility(this);
    }
    
    public void updateSize(double posterWidth) {
        this.setWidth(posterWidth);
        this.setHeight(posterWidth * 1.5);
        imageView.setFitWidth(posterWidth);
        imageView.setFitHeight(posterWidth * 1.5);
    }
    
    public Show getShow() {
        return show;
    }
}
