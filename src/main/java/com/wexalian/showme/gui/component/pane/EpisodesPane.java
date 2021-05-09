package com.wexalian.showme.gui.component.pane;

import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Season;
import com.wexalian.showme.core.concurrent.Threading;
import com.wexalian.showme.core.concurrent.task.LoadEpisodePostersTask;
import com.wexalian.showme.gui.component.poster.EpisodePoster;
import com.wexalian.showme.util.FXMLUtils;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.TilePane;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class EpisodesPane extends ScrollPane {
    
    private final ShowPane showPane;
    private final Season season;
    @FXML
    private TilePane episodePane;
    
    public EpisodesPane(ShowPane showPane, Season season) {
        this.showPane = showPane;
        this.season = season;
        FXMLUtils.loadAsController(this, "/fxml/pane/episodes.fxml");
    }
    
    @FXML
    public void initialize() {
        addEpisodes(season.getEpisodeMap().values());
        season.getEpisodeMap().addListener(this::onEpisodeChange);
        
        widthProperty().addListener((obs, oldV, newV) -> updateSize(newV.doubleValue()));
    }
    
    public void selectPoster() {
        int size = season.getEpisodeMap().size();
        if (season.areEpisodesAvailable()) {
            for (int i = 0; i < size; i++) {
                Episode episode = season.getEpisode(i + 1);
                if (!episode.isWatched()) {
                    episodePane.getChildren()
                               .stream()
                               .map(EpisodePoster.class::cast)
                               .filter(p -> p.getEpisode() == episode)
                               .findFirst()
                               .map(EpisodePoster::getEpisode)
                               .ifPresent(showPane::onEpisodeSelected);
                    break;
                }
            }
        }
        else if (size > 0) {
            episodePane.getChildren()
                       .stream()
                       .map(EpisodePoster.class::cast)
                       .filter(p -> p.getEpisode().getNumber() == 1)
                       .findFirst()
                       .map(EpisodePoster::getEpisode)
                       .ifPresent(showPane::onEpisodeSelected);
        }
    }
    
    private void addEpisodes(Collection<Episode> episodes) {
        LoadEpisodePostersTask task = new LoadEpisodePostersTask(showPane, episodes);
        task.setOnSucceeded(e -> {
            List<EpisodePoster> posters = task.getValue();
            posters.forEach(episodePane.getChildren()::add);
            
            Function<Node, Integer> numberSupplier = p -> ((EpisodePoster) p).getEpisode().getNumber();
            FXCollections.sort(episodePane.getChildren(), Comparator.comparing(numberSupplier));
            
            if (showPane.getSelectedTab().getSeason().getNumber() == season.getNumber()) {
                selectPoster();
            }
        });
        Threading.gui("load_episode_posters", task);
    }
    
    public void onEpisodeChange(MapChangeListener.Change<? extends Integer, ? extends Episode> change) {
        if (change.wasRemoved()) {
            removeEpisode(change.getValueRemoved());
        }
        if (change.wasAdded()) {
            addEpisodes(Set.of(change.getValueAdded()));
        }
    }
    
    public void updateSize(double width) {
        double posters = width / 200;
        double remain = width % 200;
        double posterWidth = 198 + remain / posters;
        
        episodePane.setPrefTileWidth(posterWidth);
        episodePane.setPrefTileHeight(posterWidth / (16F / 9F));
        episodePane.getChildren().stream().map(EpisodePoster.class::cast).forEach(p -> p.updateSize(posterWidth));
    }
    
    private void removeEpisode(Episode episode) {
        episodePane.getChildren().removeIf(n -> n instanceof EpisodePoster && ((EpisodePoster) n).getEpisode() == episode);
    }
}