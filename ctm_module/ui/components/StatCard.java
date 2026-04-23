package ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class StatCard extends VBox {
    public StatCard(String label, String value, String color) {
        setSpacing(8);
        setPadding(new Insets(20));
        getStyleClass().add("stat-card");
        
        // Soft icon placeholder
        Region icon = new Region();
        icon.setPrefSize(40, 40);
        icon.setStyle("-fx-background-color: " + color + "; -fx-opacity: 0.1; -fx-background-radius: 10;");

        Label valLbl = new Label(value);
        valLbl.getStyleClass().add("stat-value");
        
        Label tagLbl = new Label(label.toUpperCase());
        tagLbl.getStyleClass().add("stat-label");

        getChildren().addAll(icon, valLbl, tagLbl);
        setPrefWidth(220);
    }
}
