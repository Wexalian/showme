package com.wexalian.showme.gui.concurrent.task.data;

import com.wexalian.showme.core.Show;
import com.wexalian.showme.core.data.ShowData;
import com.wexalian.showme.core.show.IShowProvider;
import com.wexalian.showme.core.show.ShowParser;
import javafx.concurrent.Task;

import java.util.List;
import java.util.stream.Collectors;

public class LoadShowsTask extends Task<List<Show>> {
    private final IShowProvider provider;
    
    public LoadShowsTask(IShowProvider provider) {
        this.provider = provider;
    }
    
    @Override
    protected List<Show> call() {
        List<ShowData> showData = provider.loadShowData(this::isCancelled);
        
        return showData.parallelStream()//
                       .map(data -> ShowParser.parse(provider, data))//
                       .collect(Collectors.toList());
    }
}
