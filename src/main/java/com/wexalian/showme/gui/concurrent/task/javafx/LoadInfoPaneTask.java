package com.wexalian.showme.gui.concurrent.task.javafx;

import com.wexalian.showme.gui.component.pane.ShowInfoPane;
import javafx.concurrent.Task;

public class LoadInfoPaneTask extends Task<ShowInfoPane> {
    @Override
    protected ShowInfoPane call() {
        return new ShowInfoPane();
    }
}
