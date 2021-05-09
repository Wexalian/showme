package com.wexalian.showme.gui.component.tab;

import com.wexalian.showme.gui.component.pane.DownloadsPane;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.Tab;

public class DownloadTab extends Tab {
    public final FontAwesomeIconView DOWNLOAD = new FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD, "15px");
    
    public DownloadTab() {
        setGraphic(DOWNLOAD);
        setContent(new DownloadsPane());
    }
}
