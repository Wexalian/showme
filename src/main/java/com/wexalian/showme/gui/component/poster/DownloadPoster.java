package com.wexalian.showme.gui.component.poster;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadPoster extends AnchorPane {
    private final Download download;
    @FXML
    private ImageView imageView;
    @FXML
    private Text textEpisode;
    @FXML
    private Text textTitle;
    @FXML
    private JFXProgressBar progressBar;
    @FXML
    private Text textProgress;
    @FXML
    private Text textPeerSeeds;
    @FXML
    private JFXButton cancelButton;
    
    public DownloadPoster(Download download) {
        this.download = download;
        FXMLUtils.loadAsController(this, "/fxml/poster/download.fxml");
    }
    
    @FXML
    public void initialize() {
        Episode episode = download.getEpisode();
        Season season = episode.getSeason();
        Show show = season.getShow();
        
        download.onTorrentStart(() -> textProgress.setText("Loading metadata..."));
        
        download.onMetadataFetched(torrent -> {
            progressBar.setProgress(0);
            textProgress.setText("0.00%");
        });
        
        AtomicInteger pieces = new AtomicInteger();
        AtomicInteger peers = new AtomicInteger();
        download.onUpdateDownloadState(state -> {
            if (pieces.get() != state.getPiecesComplete()) {
                pieces.set(state.getPiecesComplete());
                
                double progress = (double) state.getPiecesComplete() / state.getPiecesTotal();
                progressBar.setProgress(progress);
                textProgress.setText(String.format("%.02f", progress * 100D) + "%");
            }
            if (peers.get() != state.getConnectedPeers().size()) {
                peers.set(state.getConnectedPeers().size());
                
                textPeerSeeds.setText(state.getConnectedPeers().size() + " peers");
            }
        });
        Object[] params = new Object[]{show.getTitle(), season.getNumber(), episode.getNumber()};
        download.onTorrentFinished(() -> DownloadManager.LOGGER.debug("Finished downloading '{}' {}x{}", params));
        download.onTorrentCanceled(() -> DownloadManager.LOGGER.debug("Canceled downloading '{}' {}x{}", params));
        
        ShowManager.loadImage(episode, this::setImage);
        
        textEpisode.setText(show.getTitle() + " " + season.getNumber() + "x" + String.format("%02d", episode.getNumber()));
        
        textTitle.setText("Title: " + episode.getTitle());
        
        cancelButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TIMES, "20px"));
        cancelButton.setOnMouseClicked(e -> DownloadManager.removeFromQueue(this));
        
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
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
