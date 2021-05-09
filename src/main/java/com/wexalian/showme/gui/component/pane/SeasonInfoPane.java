package com.wexalian.showme.gui.component.pane;

import com.wexalian.showme.util.FXMLUtils;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public class SeasonInfoPane extends AnchorPane {
    
    @FXML
    private ImageView image;
    @FXML
    private Text title;
    @FXML
    private Text description;
    
    public SeasonInfoPane() {
        FXMLUtils.loadAsController(this, "/fxml/pane/season_info.fxml");
    }
    
    public void setImage(Image image) {
        this.image.setImage(image);
    }
    
    public void bindTitleProperty(ObservableValue<String> titleProperty) {
        title.textProperty().bind(titleProperty);
    }
    
    public void setTitle(String text) {
        title.setText(text);
    }
    
    public void bindDescriptionProperty(ObservableValue<String> descriptionProperty) {
        description.textProperty().bind(descriptionProperty);
    }
    
    public void updateWrappingWidth(double width) {
        title.setWrappingWidth(width);
        description.setWrappingWidth(width);
    }
}
