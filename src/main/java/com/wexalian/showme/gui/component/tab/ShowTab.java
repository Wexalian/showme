package com.wexalian.showme.gui.component.tab;

import com.wexalian.showme.core.Show;
import com.wexalian.showme.gui.component.pane.ShowPane;
import com.wexalian.showme.gui.controller.MainController;
import javafx.scene.control.Tab;

public class ShowTab extends Tab {
    private final Show show;
    private final ShowPane pane;
    
    public ShowTab(Show show, MainController mainController) {
        super(show.getTitle());
        this.show = show;
        pane = new ShowPane(show, mainController);
        setContent(pane);
        
        // ShowManager.SHOWS.forEach(ShowManager::saveShow);
    }
    
    public Show getSeason() {
        return show;
    }
}