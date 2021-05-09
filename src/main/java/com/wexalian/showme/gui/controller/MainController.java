package com.wexalian.showme.gui.controller;

import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTabPane;
import com.wexalian.jtrakt.endpoint.shows.TraktShow;
import com.wexalian.showme.ShowMe;
import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.show.ShowManager;
import com.wexalian.showme.gui.GuiLoader;
import com.wexalian.showme.gui.component.pane.DownloadsPane;
import com.wexalian.showme.gui.component.poster.ShowPoster;
import com.wexalian.showme.gui.component.tab.ShowTab;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.layout.TilePane;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MainController {
    
    private final Map<Show, ShowTab> showTabs = new HashMap<>();
    
    @FXML
    public JFXRadioButton hideCompletedButton;
    @FXML
    public JFXRadioButton hideNotCompletedButton;
    @FXML
    public JFXRadioButton hideEndedCanceledButton;
    @FXML
    public JFXRadioButton hideCurrentlyAiringButton;
    @FXML
    public JFXRadioButton hideCurrentlyWatching;
    @FXML
    public JFXRadioButton hideNotCurrentlyWatching;
    
    @FXML
    private JFXTabPane showTabPane;
    
    @FXML
    private TilePane showPane;
    
    @FXML
    private DownloadsPane downloadsPane;
    
    @FXML
    public void initialize() {
        ShowManager.SHOWS.addListener(this::onShowChange);
        GuiLoader.STAGE.widthProperty().addListener((obs, oldV, newV) -> {
            double width = newV.doubleValue();
            double posters = width / 150;
            double remain = width % 150;
            double posterWidth = 147 + remain / posters;
            
            showPane.setPrefTileWidth(posterWidth);
            showPane.setPrefTileHeight(posterWidth * 1.5);
            
            showPane.getChildren().stream().map(ShowPoster.class::cast).forEach(p -> p.updateSize(posterWidth));
        });
        
        hideCompletedButton.setSelected(ShowMe.getSettings().FILTERS.HIDE_COMPLETED);
        hideNotCompletedButton.setSelected(ShowMe.getSettings().FILTERS.HIDE_NOT_COMPLETED);
        hideEndedCanceledButton.setSelected(ShowMe.getSettings().FILTERS.HIDE_ENDED_CANCELED);
        hideCurrentlyAiringButton.setSelected(ShowMe.getSettings().FILTERS.HIDE_CURRENTLY_AIRING);
        hideCurrentlyWatching.setSelected(ShowMe.getSettings().FILTERS.HIDE_CURRENTLY_WATCHING);
        hideNotCurrentlyWatching.setSelected(ShowMe.getSettings().FILTERS.HIDE_NOT_CURRENTLY_WATCHING);
        
        addFilterListener(hideCompletedButton, hideNotCompletedButton, b -> ShowMe.getSettings().FILTERS.HIDE_COMPLETED = b);
        addFilterListener(hideNotCompletedButton, hideCompletedButton, b -> ShowMe.getSettings().FILTERS.HIDE_NOT_COMPLETED = b);
        addFilterListener(hideEndedCanceledButton, hideCurrentlyAiringButton, b -> ShowMe.getSettings().FILTERS.HIDE_ENDED_CANCELED = b);
        addFilterListener(hideCurrentlyAiringButton, hideEndedCanceledButton, b -> ShowMe.getSettings().FILTERS.HIDE_CURRENTLY_AIRING = b);
        addFilterListener(hideCurrentlyWatching, hideNotCurrentlyWatching, b -> ShowMe.getSettings().FILTERS.HIDE_CURRENTLY_WATCHING = b);
        addFilterListener(hideNotCurrentlyWatching, hideCurrentlyWatching, b -> ShowMe.getSettings().FILTERS.HIDE_NOT_CURRENTLY_WATCHING = b);
    }
    
    private void addFilterListener(JFXRadioButton clicked, JFXRadioButton other, Consumer<Boolean> stateConsumer) {
        clicked.selectedProperty().addListener((obs, old, val) -> {
            stateConsumer.accept(clicked.isSelected());
            if (clicked.isSelected() && other.isSelected()) {
                other.setSelected(false);
            }
            showPane.getChildren().stream().map(ShowPoster.class::cast).forEach(this::updateShowVisibility);
        });
    }
    
    private void onShowChange(MapChangeListener.Change<? extends String, ? extends Show> change) {
        // while (change.next()) {
        //     if (change.wasRemoved()) {
        //         change.getRemoved().forEach(this::removeShow);
        //     }
        //     if (change.wasAdded()) {
        //         change.getAddedSubList().forEach(this::addShow);
        //     }
        // }
        
        if (change.wasRemoved()) {
            removeShow(change.getValueRemoved());
        }
        if (change.wasAdded()) {
            addShow(change.getValueAdded());
        }
        
        FXCollections.sort(showPane.getChildren(), Comparator.comparing(n -> ((ShowPoster) n).getShow().getTitle()));
    }
    
    private void removeShow(Show show) {
        showTabs.remove(show);
        showPane.getChildren().removeIf(n -> n instanceof ShowPoster && ((ShowPoster) n).getShow() == show);
    }
    
    private void addShow(Show show) {
        ShowPoster poster = new ShowPoster(this, show);
        showPane.getChildren().add(poster);
        updateShowVisibility(poster);
    }
    
    public void openShow(Show show) {
        ShowTab showTab = showTabs.computeIfAbsent(show, s -> {
            ShowTab tab = new ShowTab(s, this);
            showTabPane.getTabs().add(tab);
            return tab;
        });
        showTabPane.getSelectionModel().select(showTab);
    }
    
    public void updateShowVisibility(ShowPoster poster) {
        Show show = poster.getShow();
        boolean visible = true;
        
        if (hideCompletedButton.isSelected() && !show.areEpisodeAvailable()) {
            visible = false;
        }
        else if (hideNotCompletedButton.isSelected() && show.areEpisodeAvailable()) {
            visible = false;
        }
        else if (hideEndedCanceledButton.isSelected() && (show.getStatus() == TraktShow.Status.ENDED || show.getStatus() == TraktShow.Status.CANCELED)) {
            visible = false;
        }
        else if (hideCurrentlyAiringButton.isSelected() && show.getStatus() == TraktShow.Status.RETURNING) {
            visible = false;
        }
        else if (hideCurrentlyWatching.isSelected() && show.areEpisodesWatched()) {
            visible = false;
        }
        else if (hideNotCurrentlyWatching.isSelected() && !show.areEpisodesWatched()) {
            visible = false;
        }
        
        poster.setManaged(visible);
        poster.setVisible(visible);
    }
    
    public DownloadsPane getDownloadsPane() {
        return downloadsPane;
    }
}
