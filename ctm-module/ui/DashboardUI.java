package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

// to compile and run:
// java --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml ui.DashboardUI

public class DashboardUI extends Application {

    @Override
    public void start(Stage stage) {

        // Title
        Label title = new Label("Secure Task Manager Dashboard");

        VBox topBox = new VBox(title);
        topBox.setAlignment(Pos.CENTER);

        // Sidebar
        VBox sidebar = new VBox(10);
        sidebar.getChildren().addAll(
            new Label("All Tasks"),
            new Label("Completed"),
            new Label("Pending")
        );

        // Center content
        Label taskArea = new Label("Tasks will appear here...");

        // Layout
        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setLeft(sidebar);
        root.setCenter(taskArea);

        root.setStyle("-fx-padding: 20;");

        // Scene
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("CTM Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}