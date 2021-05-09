package com.wexalian.showme.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

public final class FXMLUtils {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static <T extends Parent> void loadAsController(T controller, String fileName) {
        FXMLLoader loader = new FXMLLoader();
        loader.setRoot(controller);
        loader.setController(controller);
        
        try {
            loader.load(controller.getClass().getResourceAsStream(fileName));
        }
        catch (Exception e) {
            LOGGER.error("Error loading FXML file {} for controller {}", fileName, controller.getClass().getName());
            LOGGER.catching(Level.ERROR, e);
        }
    }
    
    public static <T extends Parent> T load(String fileName) {
        URL resourceUrl = FXMLUtils.class.getResource(fileName);
        if (resourceUrl != null) {
            try {
                return FXMLLoader.load(resourceUrl);
            }
            catch (Exception e) {
                LOGGER.error("Error loading FXML file {}", fileName);
                LOGGER.catching(Level.ERROR, e);
            }
        }
        return null;
    }
}
