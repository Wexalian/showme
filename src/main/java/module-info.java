module com.wexalian.showme {
    requires jsr305;
    requires javafx.controls;
    requires javafx.fxml;
    requires com.jfoenix;
    requires com.google.gson;
    requires com.google.common;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires org.fusesource.jansi;
    requires com.wexalian.jtrakt;
    requires tmdb.java;
    requires de.jensd.fx.fontawesomefx.commons;
    requires de.jensd.fx.fontawesomefx.fontawesome;
    requires java.desktop;
    requires retrofit2;
    requires org.apache.commons.lang3;
    requires bt.core;
    requires bt.dht;
    requires bt.bencoding;
    requires bt.http.tracker.client;
    requires bt.upnp;
    requires java.net.http;
    
    opens com.wexalian.showme.core to com.google.gson;
    opens com.wexalian.showme.core.data to com.google.gson;
    opens com.wexalian.showme.core.settings to com.google.gson;
    
    opens com.wexalian.showme.gui to javafx.fxml, javafx.graphics;
    opens com.wexalian.showme.gui.controller to javafx.fxml, javafx.graphics;
    
    // opens com.wexalian.showme.gui.component to javafx.fxml;
    opens com.wexalian.showme.gui.component.pane to javafx.fxml;
    opens com.wexalian.showme.gui.component.poster to javafx.fxml;
    opens com.wexalian.showme.gui.component.tab to javafx.fxml;
    
    exports com.wexalian.showme.core;
    exports com.wexalian.showme.core.data;
    exports com.wexalian.showme.core.show;
    exports com.wexalian.showme.util;
    exports com.wexalian.showme.util.throwing;
}