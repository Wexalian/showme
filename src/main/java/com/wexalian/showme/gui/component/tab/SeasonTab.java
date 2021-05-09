package com.wexalian.showme.gui.component.tab;

import com.wexalian.showme.core.Season;
import com.wexalian.showme.gui.component.pane.EpisodesPane;
import com.wexalian.showme.gui.component.pane.ShowPane;
import javafx.scene.control.Tab;
import org.apache.logging.log4j.util.Strings;

public class SeasonTab extends Tab {
    private final Season season;
    
    public SeasonTab(ShowPane showPane, Season season) {
        super(Strings.isBlank(season.getTitle()) ? "Season " + season.getNumber() : season.getTitle());
        this.season = season;
        EpisodesPane pane = new EpisodesPane(showPane, season);
        setContent(pane);
    }
    
    public Season getSeason() {
        return season;
    }
}
