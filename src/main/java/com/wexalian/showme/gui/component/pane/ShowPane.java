package com.wexalian.showme.gui.component.pane;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTabPane;
import com.wexalian.showme.core.Episode;
import com.wexalian.showme.core.Season;
import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.concurrent.Threading;
import com.wexalian.showme.core.download.Download;
import com.wexalian.showme.core.download.DownloadManager;
import com.wexalian.showme.core.show.ShowManager;
import com.wexalian.showme.core.torrent.Magnet;
import com.wexalian.showme.core.torrent.TorrentCollector;
import com.wexalian.showme.gui.GuiLoader;
import com.wexalian.showme.gui.component.tab.SeasonTab;
import com.wexalian.showme.gui.concurrent.task.javafx.LoadSeasonTabTask;
import com.wexalian.showme.gui.controller.MainController;
import com.wexalian.showme.util.FXMLUtils;
import com.wexalian.showme.util.throwing.Throwing;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.awt.*;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.function.Consumer;

public class ShowPane extends AnchorPane {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private final FontAwesomeIconView DOWNLOAD = new FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD, "15px");
    private final FontAwesomeIconView EYE_CLOSED = new FontAwesomeIconView(FontAwesomeIcon.EYE_SLASH, "15px");
    private final FontAwesomeIconView EYE_OPENED = new FontAwesomeIconView(FontAwesomeIcon.EYE, "15px");
    private final FontAwesomeIconView PLAY = new FontAwesomeIconView(FontAwesomeIcon.PLAY, "15px");
    
    private final Show show;
    private final MainController mainController;
    
    private boolean listenerAdded = false;
    private boolean firstSelected = false;
    
    @FXML
    private ScrollPane showScrollPane;
    @FXML
    private ScrollPane seasonScrollPane;
    @FXML
    private ScrollPane episodeScrollPane;
    
    private ShowInfoPane showInfo;
    private SeasonInfoPane seasonInfo;
    
    @FXML
    private Text episodeTitle;
    @FXML
    private Text episodeDescription;
    @FXML
    private Text episodeReleaseDate;
    
    @FXML
    private SplitMenuButton actionButton;
    
    @FXML
    private JFXButton watchedButton;
    
    @FXML
    private JFXTabPane seasonTabPane;
    
    public ShowPane(Show show, MainController mainController) {
        this.show = show;
        this.mainController = mainController;
        
        FXMLUtils.loadAsController(this, "/fxml/pane/show.fxml");
    }
    
    @FXML
    public void initialize() {
        initializeShowInfo();
        initializeSeasonInfo();
        
        show.getSeasons().forEach(this::addSeason);
        show.getSeasons().addListener(this::onSeasonChange);
        
        GuiLoader.STAGE.widthProperty().addListener((obs, oldV, newV) -> updateWidth(newV.doubleValue()));
        GuiLoader.STAGE.heightProperty().addListener((obs, oldV, newV) -> updateHeight(newV.doubleValue()));
    }
    
    public void initializeShowInfo() {
        showInfo = new ShowInfoPane();
        ShowManager.loadImage(show, showInfo::setImage);
        showInfo.bindTitleProperty(show.titleValue());
        showInfo.bindDescriptionProperty(show.descriptionValue());
        showInfo.bindStatusProperty(show.statusValue());
        showScrollPane.setContent(showInfo);
    }
    
    public void initializeSeasonInfo() {
        seasonInfo = new SeasonInfoPane();
        seasonScrollPane.setContent(seasonInfo);
    }
    
    private void onSeasonChange(MapChangeListener.Change<? extends Integer, ? extends Season> change) {
        if (change.wasRemoved()) {
            removeSeason(change.getValueRemoved());
            FXCollections.sort(seasonTabPane.getTabs(), Comparator.comparingInt(tab -> ((SeasonTab) tab).getSeason().getNumber()));
        }
        if (change.wasAdded()) {
            addSeason(change.getKey(), change.getValueAdded());
        }
    }
    
    private void removeSeason(Season season) {
        seasonTabPane.getTabs().removeIf(tab -> tab instanceof SeasonTab && ((SeasonTab) tab).getSeason() == season);
    }
    
    private void addSeason(int number, Season season) {
        LoadSeasonTabTask loadSeasonTabTask = new LoadSeasonTabTask(this, season);
        loadSeasonTabTask.setOnSucceeded(e -> {
            SeasonTab tab = loadSeasonTabTask.getValue();
            seasonTabPane.getTabs().add(tab);
            FXCollections.sort(seasonTabPane.getTabs(), Comparator.comparingInt(t -> ((SeasonTab) t).getSeason().getNumber()));
            
            if (tab.getSeason().areEpisodesAvailable() || seasonTabPane.getTabs().size() == 0) {
                if (!firstSelected) {
                    firstSelected = true;
                    seasonTabPane.getSelectionModel().select(tab);
                }
                else {
                    SeasonTab currentTab = ((SeasonTab) seasonTabPane.getSelectionModel().getSelectedItem());
                    if (tab.getSeason().getNumber() < currentTab.getSeason().getNumber()) {
                        seasonTabPane.getSelectionModel().select(tab);
                    }
                }
            }
            if (!listenerAdded) {
                listenerAdded = true;
                seasonTabPane.getSelectionModel().selectedItemProperty().addListener((value, oldTab, newTab) -> onSeasonTabSelected((SeasonTab) newTab));
            }
        });
        Threading.gui("load_season",loadSeasonTabTask);
    }
    
    private void updateWidth(double width) {
        double halfWidth = width / 2;
        double quarterWidth = width / 4;
        
        showScrollPane.setPrefWidth(halfWidth);
        showInfo.updateWrappingWidth(halfWidth - 250);
        
        seasonScrollPane.setPrefWidth(halfWidth);
        seasonInfo.updateWrappingWidth(halfWidth - 250);
        
        episodeScrollPane.setPrefWidth(quarterWidth);
        episodeTitle.setWrappingWidth(quarterWidth - 50);
        episodeDescription.setWrappingWidth(quarterWidth - 50);
        
        seasonTabPane.setPrefWidth(width - quarterWidth);
    }
    
    private void updateHeight(double height) {
        double thirdHeight = height / 3;
        
        showScrollPane.setMaxHeight(thirdHeight);
        seasonScrollPane.setMaxHeight(thirdHeight);
        
        AnchorPane.setTopAnchor(episodeScrollPane, thirdHeight);
        AnchorPane.setTopAnchor(seasonTabPane, thirdHeight);
    }
    
    private void onSeasonTabSelected(SeasonTab tab) {
        if (tab != null) {
            if (seasonInfo != null) {
                ShowManager.loadImage(tab.getSeason(), seasonInfo::setImage);
                seasonInfo.setTitle(tab.getText());
                seasonInfo.bindDescriptionProperty(tab.getSeason().descriptionValue());
            }
            Node content = tab.getContent();
            if (content instanceof EpisodesPane) {
                ((EpisodesPane) content).selectPoster();
            }
        }
    }
    
    public void onEpisodeSelected(Episode episode) {
        episodeTitle.setText("Episode " + episode.getNumber() + ": " + episode.getTitle());
        episodeDescription.setText(episode.getDescription());
        
        OffsetDateTime date = episode.getReleaseDate();
        String dateText = "Unknown";
        if (date != null && date.toEpochSecond() != 0) {
            int day = date.getDayOfMonth();
            int month = date.getMonthValue();
            int year = date.getYear();
            int hour = date.getHour();
            int minute = date.getMinute();
            dateText = String.format("%02d/%02d/%04d %02d:%02d", day, month, year, hour, minute);
        }
        episodeReleaseDate.setText(String.format("Release Date: %s", dateText));
        
        updateActionButton(episode);
        updateWatchedButton(episode);
    }
    
    private void updateWatchedButton(Episode episode) {
        watchedButton.setOnAction(e -> {
            episode.setWatched(!episode.isWatched());
            watchedButton.setGraphic(episode.isWatched() ? EYE_CLOSED : EYE_OPENED);
            ShowManager.updateTraktWatched(episode, episode.isWatched(), episode::setWatched);
            
            if (episode.isWatched()) {
                Episode nextEpisode = episode.getNextEpisode();
                if (nextEpisode != null) {
                    onEpisodeSelected(nextEpisode);
                }
                else {
                    seasonTabPane.getSelectionModel().selectNext();
                }
            }
            
        });
        watchedButton.setGraphic(episode.isWatched() ? EYE_CLOSED : EYE_OPENED);
    }
    
    private void updateActionButton(Episode episode) {
        boolean downloaded = Strings.isNotBlank(episode.getFileName());
        actionButton.getItems().clear();
        if (downloaded) {
            actionButton.setGraphic(PLAY);
            actionButton.setText(" Play");
            actionButton.setOnAction(e -> playEpisode(episode));
            actionButton.setDisable(false);
        }
        else {
            actionButton.setGraphic(DOWNLOAD);
            actionButton.setText(" Download");
            
            OffsetDateTime date = episode.getReleaseDate();
            if (date != null && date.toEpochSecond() != 0 && date.isBefore(OffsetDateTime.now())) {
                Consumer<Download> downloadConsumer = mainController.getDownloadsPane()::addDownloadToQueue;
                
                MenuItem item = new MenuItem(" Download from magnet");
                item.setOnAction(e -> popup(episode, magnet -> DownloadManager.queueEpisodeDownload(episode, magnet, downloadConsumer)));
                
                actionButton.getItems().add(item);
                actionButton.setOnAction(e -> DownloadManager.queueEpisodeDownload(episode, downloadConsumer));
                actionButton.setDisable(false);
            }
            else {
                actionButton.setOnAction(e -> {});
                actionButton.setDisable(true);
            }
        }
        
        MenuItem item2 = new MenuItem("Get all magnets");
        item2.setOnAction(e -> episode.getSeason().getEpisodeMap().values().forEach(ep -> {
            Magnet magnet = TorrentCollector.getMagnet(ep);
            if (magnet != null) {
                System.out.println("Episode " + ep.getNumber() + ": " + magnet.getMagnetUrl() + "{}");
                
                // Object[] params = new Object[]{show.getTitle(), season.getNumber(), String.format("%02d", ep.getNumber()), magnet.getSeeds(), magnet.getPeers(), magnet.getMagnetUrl()};
                // LOGGER.debug("'{}' {}x{} (seeds: {}, peers: {}): {}", params);
            }
        }));
        actionButton.getItems().add(item2);
    }
    
    private void popup(Episode episode, Consumer<Magnet> consumer) {
        Popup popup = new Popup();
        
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        
        TextField text = new TextField();
        JFXButton button = new JFXButton();
        button.setOnAction(e -> {
            String input = text.getText();
            if (Strings.isNotBlank(input)) {
                Magnet magnet = new Magnet("", input, episode.getNumber(), episode.getSeason().getNumber(), 0, 0, null);
                consumer.accept(magnet);
            }
            popup.hide();
        });
        
        box.getChildren().addAll(text, button);
        popup.getContent().add(box);
        popup.show(GuiLoader.STAGE);
    }
    
    private void playEpisode(Episode episode) {
        ShowManager.loadEpisodePath(episode, Throwing.transformAndConsume(Path::toFile, Desktop.getDesktop()::open, (file, exception) -> {
            LOGGER.debug("Error playing video file at location '{}'", file.toString());
            LOGGER.catching(Level.DEBUG, exception);
        }));
    }
    
    public SeasonTab getSelectedTab() {
        return (SeasonTab) seasonTabPane.getSelectionModel().getSelectedItem();
    }
}
