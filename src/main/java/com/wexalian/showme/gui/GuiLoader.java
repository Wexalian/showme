package com.wexalian.showme.gui;

import com.wexalian.showme.core.concurrent.Threading;
import com.wexalian.showme.core.concurrent.task.DownloadTorrentTask;
import com.wexalian.showme.core.show.ShowCache;
import com.wexalian.showme.core.show.ShowManager;
import com.wexalian.showme.util.FXMLUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class GuiLoader extends Application {
    public static Stage STAGE = null;
    
    @Override
    public void start(Stage stage) {
        STAGE = stage;
        
        Parent root = FXMLUtils.load("/fxml/main.fxml");
        Scene scene = new Scene(Objects.requireNonNull(root));
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark_theme.css")).toString());
        
        stage.setScene(scene);
        stage.setTitle("showme");
        stage.show();
        
        stage.setOnCloseRequest(e -> {
            DownloadTorrentTask.shutdown();
            ShowCache.shutdown();
            Threading.shutdown();
            ShowManager.shutdown();
        });
        
        ShowManager.init();
    }
    
    public static void start(String[] args) {
        launch(args);
    }
}