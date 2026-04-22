package ui.views;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.geometry.Side;
import model.Task;
import ui.components.StatCard;

public class DashboardView extends VBox {
    private ObservableList<Task> taskList;

    public DashboardView(ObservableList<Task> tasks) {
        this.taskList = tasks;
        setSpacing(30);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #0d1117;");

        // --- HEADER ---
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("DASHBOARD");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        topBar.getChildren().add(title);

        // --- WELCOME CARD ---
        VBox welcomeCard = new VBox(15);
        welcomeCard.setStyle("-fx-background-color: linear-gradient(to right, #8b5cf6, #7c3aed); -fx-padding: 30; -fx-background-radius: 15;");
        Label welcomeTitle = new Label("Welcome to your Task Management Area");
        welcomeTitle.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        Label welcomeSub = new Label("You have " + getPendingCount() + " tasks pending for today. Keep up the great work!");
        welcomeSub.setStyle("-fx-text-fill: #e9d5ff; -fx-font-size: 14px;");
        Button learnMore = new Button("Learn More");
        learnMore.setStyle("-fx-background-color: white; -fx-text-fill: #7c3aed; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 20;");
        welcomeCard.getChildren().addAll(welcomeTitle, welcomeSub, learnMore);

        // --- STATS ROW ---
        HBox statsRow = new HBox(20);
        statsRow.getChildren().addAll(
            new StatCard("Total Tasks", String.valueOf(taskList.size()), "#3498db"),
            new StatCard("In Progress", String.valueOf(getCount("IN_PROGRESS")), "#f59e0b"),
            new StatCard("Pending", String.valueOf(getCount("DEADLINE")), "#8b5cf6"),
            new StatCard("Completed", String.valueOf(getCount("DONE")), "#10b981")
        );

        // --- CHARTS ROW ---
        HBox chartsRow = new HBox(30);
        chartsRow.setPrefHeight(300);

        // Donut Chart placeholder (using PieChart)
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Task Distribution");
        pieChart.getData().add(new PieChart.Data("To Do", getCount("DEADLINE")));
        pieChart.getData().add(new PieChart.Data("In Progress", getCount("IN_PROGRESS")));
        pieChart.getData().add(new PieChart.Data("Done", getCount("DONE")));
        pieChart.setLabelsVisible(false);
        pieChart.setLegendSide(Side.BOTTOM);
        pieChart.setStyle("-fx-background-color: #161b22; -fx-background-radius: 15;");
        HBox.setHgrow(pieChart, Priority.ALWAYS);

        // Line Chart placeholder
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Work Progress");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tasks Completed");
        series.getData().add(new XYChart.Data<>("Mon", 2));
        series.getData().add(new XYChart.Data<>("Tue", 5));
        series.getData().add(new XYChart.Data<>("Wed", 3));
        series.getData().add(new XYChart.Data<>("Thu", 8));
        series.getData().add(new XYChart.Data<>("Fri", 10));
        lineChart.getData().add(series);
        lineChart.setStyle("-fx-background-color: #161b22; -fx-background-radius: 15;");
        HBox.setHgrow(lineChart, Priority.ALWAYS);

        chartsRow.getChildren().addAll(pieChart, lineChart);

        getChildren().addAll(topBar, welcomeCard, statsRow, chartsRow);
    }

    private long getCount(String status) {
        return taskList.stream().filter(t -> status.equals(t.getStatus())).count();
    }

    private long getPendingCount() {
        return getCount("DEADLINE") + getCount("IN_PROGRESS");
    }
}
