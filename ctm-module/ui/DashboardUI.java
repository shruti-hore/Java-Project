// compile : javac --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml -d . ui/*.java model/*.java service/*.java
// run : java --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml ui.DashboardUI

package ui;
import model.Task;
import service.MockDataService;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;

import java.util.List;

public class DashboardUI extends Application {

    @Override
    public void start(Stage stage) {

        // top
        Label title = new Label("Secure Task Manager Dashboard");

        VBox topBox = new VBox(title);
        topBox.setAlignment(Pos.CENTER);

        // side bar
        VBox sidebar = new VBox(10);
        sidebar.getChildren().addAll(
            new Label("All Tasks"),
            new Label("Completed"),
            new Label("Pending")
        );

        // center (task list)
        VBox taskContainer = new VBox(10);

        List<Task> tasks = MockDataService.getSampleTasks();

        for (Task t : tasks) {
            Label taskLabel = new Label(t.getDetails());
            taskContainer.getChildren().add(taskLabel);
        }

        ScrollPane scrollPane = new ScrollPane(taskContainer);
        scrollPane.setFitToWidth(true);

        // root
        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setLeft(sidebar);
        root.setCenter(scrollPane);

        root.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("CTM Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}