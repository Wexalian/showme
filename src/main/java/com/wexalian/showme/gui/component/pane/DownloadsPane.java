package com.wexalian.showme.gui.component.pane;

import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.download.Download;
import com.wexalian.showme.core.download.DownloadManager;
import com.wexalian.showme.core.show.ShowManager;
import com.wexalian.showme.gui.GuiLoader;
import com.wexalian.showme.gui.component.poster.DownloadAvailablePoster;
import com.wexalian.showme.gui.component.poster.DownloadPoster;
import com.wexalian.showme.util.FXMLUtils;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class DownloadsPane extends AnchorPane {
    
    @FXML
    private ScrollPane downloadQueue;
    
    @FXML
    private ScrollPane availableDownloads;
    
    @FXML
    private AnchorPane currentDownload;
    
    public DownloadsPane() {
        FXMLUtils.loadAsController(this, "/fxml/pane/downloads.fxml");
    }
    
    @FXML
    public void initialize() {
        GuiLoader.STAGE.widthProperty().addListener((obs, oldV, newV) -> updateWidth(newV.doubleValue()));
        
        synchronized (DownloadManager.AVAILABLE_DOWNLOADS) {
            DownloadManager.AVAILABLE_DOWNLOADS.addListener((MapChangeListener.Change<? extends Show, ? extends ObservableMap<Episode, Download>> c) -> {
                if (c.wasAdded()) {
                    ObservableMap<Episode, Download> downloads = c.getValueAdded();
                    
                    downloads.addListener((MapChangeListener.Change<? extends Episode, ? extends Download> c2) -> {
                        if (c2.wasAdded()) {
                            Platform.runLater(() -> {
                                Download download = c2.getValueAdded();
                                addAvailableDownload(download);
                            });
                        }
                    });
                }
            });
        }
    }
    
    private void updateWidth(double width) {
        double oneThirds = width / 3;
        
        downloadQueue.setPrefWidth(oneThirds);
        availableDownloads.setPrefWidth(oneThirds * 2);
    }
    
    //run on javafx thread
    public void addAvailableDownload(Download download) {
        DownloadAvailablePoster poster = new DownloadAvailablePoster(download, this::addDownloadToQueue);
        addToAvailableDownloads(poster);
        download.onTorrentQueued(() -> removeFromAvailableDownloads(poster));
    }
    
    public void addDownloadToQueue(Download download) {
        DownloadPoster poster = new DownloadPoster(download);
        addToQueue(poster);
        download.onTorrentFinished(() -> removeFromQueue(poster));
        download.onTorrentCanceled(() -> removeFromQueue(poster));
        download.onTorrentCanceled(() -> ShowManager.deleteEpisodeFile(download.getEpisode()));
    }
    
    //run on javafx thread
    private void addToQueue(DownloadPoster poster) {
        ((VBox) downloadQueue.getContent()).getChildren().add(poster);
    }
    
    //run on javafx thread
    private void removeFromQueue(DownloadPoster poster) {
        ((VBox) downloadQueue.getContent()).getChildren().remove(poster);
    }
    
    //run on javafx thread
    private void addToAvailableDownloads(DownloadAvailablePoster poster) {
        ((VBox) availableDownloads.getContent()).getChildren().add(poster);
    }
    
    //run on javafx thread
    private void removeFromAvailableDownloads(DownloadAvailablePoster poster) {
        ((VBox) availableDownloads.getContent()).getChildren().remove(poster);
    }
}
