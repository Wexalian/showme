package com.wexalian.showme.gui.concurrent.task.javafx;

import com.wexalian.showme.core.Season;
import com.wexalian.showme.gui.component.pane.ShowPane;
import com.wexalian.showme.gui.component.tab.SeasonTab;
import javafx.concurrent.Task;

public class LoadSeasonTabTask extends Task<SeasonTab> {
    
    private final ShowPane showPane;
    private final Season season;
    
    public LoadSeasonTabTask(ShowPane showPane, Season season) {
        
        this.showPane = showPane;
        this.season = season;
    }
    
    @Override
    protected SeasonTab call() {
        return new SeasonTab(showPane, season);
    }
}
