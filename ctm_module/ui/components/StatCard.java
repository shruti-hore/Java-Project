package ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class StatCard extends VBox {
    public StatCard(String label, String value, String color) {
        setSpacing(10);
        setPadding(new Insets(15));
        getStyleClass().add("stat-card");
        setStyle(getStyle() + "-fx-border-color: " + color + "; -fx-border-width: 0 0 0 4;");

        Label valLbl = new Label(value);
        valLbl.getStyleClass().add("stat-value");
        
        Label tagLbl = new Label(label.toUpperCase());
        tagLbl.getStyleClass().add("stat-label");

        getChildren().addAll(tagLbl, valLbl);
        setPrefWidth(200);
    }
}
