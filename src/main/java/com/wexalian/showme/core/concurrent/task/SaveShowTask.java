package com.wexalian.showme.core.concurrent.task;

import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.data.ShowData;
import com.wexalian.showme.core.show.IShowProvider;
import com.wexalian.showme.core.show.ShowParser;
import javafx.concurrent.Task;

public class SaveShowTask extends Task<Void> {
    private final Show show;
    private final IShowProvider provider;
    
    public SaveShowTask(Show show, IShowProvider provider) {
        this.show = show;
        this.provider = provider;
    }
    
    @Override
    protected Void call() {
        ShowData data = ShowParser.unparse(show);
        provider.saveShowData(show, data);
        
        return null;
    }
}
