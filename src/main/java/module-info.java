module showme {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.jfoenix;
    
    opens com.wexalian.showme.javafx.controller to javafx.fxml;
    opens com.wexalian.showme.javafx.tab to javafx.fxml;
    
    exports com.wexalian.showme;
}