package com.wexalian.showme.gui.component.poster;

import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.show.ShowManager;
import com.wexalian.showme.gui.component.pane.ShowPane;
import com.wexalian.showme.util.FXMLUtils;
import javafx.fxml.FXML;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public class EpisodePoster extends AnchorPane {
    private static final ColorAdjust GRAYSCALE_EFFECT = new ColorAdjust(0, -1, -0.75, 0);
    
    private final ShowPane showPane;
    private final Episode episode;
    
    @FXML
    private ImageView imageView;
    
    @FXML
    private Text title;
    
    public EpisodePoster(ShowPane showPane, Episode episode) {
        this.showPane = showPane;
        this.episode = episode;
        FXMLUtils.loadAsController(this, "/fxml/poster/episode.fxml");
    }
    
    @FXML
    public void initialize() {
        ShowManager.loadImage(episode, imageView::setImage);
        
        episode.titleValue().addListener((obs, oldV, newV) -> updateTitle(newV));
        updateTitle(episode.getTitle());
        
        episode.watchedValue().addListener((obs, oldV, newV) -> updateWatched(newV));
        updateWatched(episode.isWatched());
        
        this.setOnMouseClicked(e -> showPane.onEpisodeSelected(episode));
    }
    
    private void updateTitle(String title) {
        this.title.setText("Episode " + episode.getNumber() + ": " + title);
    }
    
    private void updateWatched(boolean watched) {
        imageView.setEffect(watched ? GRAYSCALE_EFFECT : null);
    }
    
    public void updateSize(double posterWidth) {
        this.setWidth(posterWidth);
        this.setHeight(posterWidth / (16F / 9F));
        imageView.setFitWidth(posterWidth);
        imageView.setFitHeight(posterWidth / (16F / 9F));
    }
    
    public Episode getEpisode() {
        return episode;
    }
    
}
