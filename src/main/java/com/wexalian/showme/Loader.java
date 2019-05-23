package com.wexalian.showme;

import javafx.application.Application;
import javafx.application.Preloader;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Loader extends Application
{
    BooleanProperty ready = new SimpleBooleanProperty(false);
    
    private void longStart()
    {
        //simulate long init in background
        Task task = new Task<Void>()
        {
            @Override
            protected Void call() throws Exception
            {
                int max = 10;
                for (int i = 1; i <= max; i++)
                {
                    Thread.sleep(200);
                    // Send progress to preloader
                    notifyPreloader(new Preloader.ProgressNotification(((double) i) / max));
                }
                // After init is ready, the app is ready to be shown
                // Do this before hiding the preloader stage to prevent the
                // app from exiting prematurely
                ready.setValue(Boolean.TRUE);
                
                notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
                return null;
            }
        };
        new Thread(task).start();
    }
    
    @Override
    public void start(Stage stage) throws Exception
    {
        // longStart();
        
        Parent parent = FXMLLoader.load(getClass().getResource("/fxml/scene.fxml"));
        Scene scene = new Scene(parent);
        scene.getStylesheets().add(getClass().getResource("/css/dark_theme.css").toString());
        stage.setScene(scene);
        stage.setTitle("ShowMe!");
        stage.show();
        
        // ready.addListener((ov, t, t1) -> {
        //     if (Boolean.TRUE.equals(t1))
        //     {
        //         Platform.runLater(stage::show);
        //     }
        // });
    }
    
    public static void main(String[] args)
    {
        launch(args);
        // LauncherImpl.launchApplication(Loader.class, PreLoader.class, args);
    }
}