package com.wexalian.showme.gui.component.poster;

import com.jfoenix.controls.JFXButton;
import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Season;
import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.download.Download;
import com.wexalian.showme.core.download.DownloadManager;
import com.wexalian.showme.core.show.ShowManager;
import com.wexalian.showme.util.FXMLUtils;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class DownloadAvailablePoster extends AnchorPane {
    private final Download download;
    private final Consumer<Download> queueConsumer;
    @FXML
    private ImageView imageView;
    @FXML
    private Text textEpisode;
    @FXML
    private Text textTitle;
    @FXML
    private Text textPeerSeeds;
    @FXML
    private JFXButton cancelButton;
    @FXML
    private JFXButton downloadButton;
    
    public DownloadAvailablePoster(Download download, Consumer<Download> queueConsumer) {
        this.download = download;
        this.queueConsumer = queueConsumer;
        FXMLUtils.loadAsController(this, "/fxml/poster/download_available.fxml");
    }
    
    @FXML
    public void initialize() {
        Episode episode = download.getEpisode();
        Season season = episode.getSeason();
        Show show = season.getShow();
        
        textPeerSeeds.setText(download.getMagnet().getPeers() + " peers / " + download.getMagnet().getSeeds() + " seeds");
        
        ShowManager.loadImage(episode, this::setImage);
        
        textEpisode.setText(show.getTitle() + " " + season.getNumber() + "x" + String.format("%02d", episode.getNumber()));
        
        textTitle.setText("Title: " + episode.getTitle());
        
        cancelButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TIMES, "20px"));
        cancelButton.setOnMouseClicked(e -> DownloadManager.removeFromAvailableDownloads(this));
        
        downloadButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD, "17px"));
        downloadButton.setOnMouseClicked(e -> {
            DownloadManager.removeFromAvailableDownloads(this);
            DownloadManager.queueDownload(download);
            queueConsumer.accept(download);
        });
    }
    
    private void setImage(Image image) {
        imageView.setImage(image);
        double imageHeight = 188 * (image.getHeight() / image.getWidth());
        setPrefHeight(20 + imageHeight);
    }
    
    public Download getDownload() {
        return download;
    }
}
