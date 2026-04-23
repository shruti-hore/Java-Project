package ui.views;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.geometry.Side;
import model.Task;
import ui.components.StatCard;
import java.time.LocalDate;

public class DashboardView extends BorderPane {
    private ObservableList<Task> taskList;

    public DashboardView(ObservableList<Task> tasks) {
        this.taskList = tasks;
        getStyleClass().add("root");

        // --- TOP BAR ---
        setTop(createTopBar());

        // --- CENTER CONTENT (Scrollable) ---
        VBox centerContent = new VBox(30);
        centerContent.setPadding(new Insets(30));
        
        centerContent.getChildren().addAll(
            createWelcomeCard(),
            createStatsRow(),
            createChartsRow(),
            createWorkProgressSection()
        );

        ScrollPane scrollPane = new ScrollPane(centerContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setCenter(scrollPane);

        // --- RIGHT PANEL ---
        setRight(createRightPanel());
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(20, 30, 20, 30));
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #e1e4e8; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Dashboard");
        title.getStyleClass().add("text-title");

        TextField searchField = new TextField();
        searchField.setPromptText("Search anything...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: #f5f6fa; -fx-background-radius: 10; -fx-padding: 10 15; -fx-border-color: transparent;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label("March 2021");
        dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        Button dateSelector = new Button("📅");
        dateSelector.setStyle("-fx-background-color: #f5f6fa; -fx-background-radius: 8; -fx-padding: 8;");

        topBar.getChildren().addAll(title, searchField, spacer, dateLabel, dateSelector);
        return topBar;
    }

    private VBox createWelcomeCard() {
        VBox welcome = new VBox(15);
        welcome.getStyleClass().add("welcome-card");
        
        Label title = new Label("Your Task Management Area");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        long pending = getCount("DEADLINE") + getCount("IN_PROGRESS");
        Label sub = new Label("You have " + pending + " tasks remaining. Keep pushing forward!");
        sub.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 14px;");
        
        Button learnMore = new Button("View Tasks");
        learnMore.setStyle("-fx-background-color: white; -fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 25;");
        
        welcome.getChildren().addAll(title, sub, learnMore);
        return welcome;
    }

    private HBox createStatsRow() {
        HBox row = new HBox(20);
        row.getChildren().addAll(
            new StatCard("Total Tasks", String.valueOf(taskList.size()), "#4f46e5"),
            new StatCard("In Progress", String.valueOf(getCount("IN_PROGRESS")), "#f59e0b"),
            new StatCard("Pending", String.valueOf(getCount("DEADLINE")), "#8b5cf6"),
            new StatCard("Completed", String.valueOf(getCount("DONE")), "#10b981")
        );
        return row;
    }

    private HBox createChartsRow() {
        HBox row = new HBox(25);
        row.setPrefHeight(350);

        // Line Chart Container
        VBox lineChartBox = new VBox(15);
        lineChartBox.getStyleClass().add("chart-container");
        HBox.setHgrow(lineChartBox, Priority.ALWAYS);
        
        Label chartTitle = new Label("Work Progress");
        chartTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLegendVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        // Mock data for trends (since we don't have historical data in Mongo yet)
        series.getData().add(new XYChart.Data<>("Mon", 2));
        series.getData().add(new XYChart.Data<>("Tue", 5));
        series.getData().add(new XYChart.Data<>("Wed", 3));
        series.getData().add(new XYChart.Data<>("Thu", 8));
        series.getData().add(new XYChart.Data<>("Fri", 10));
        lineChart.getData().add(series);
        
        lineChartBox.getChildren().addAll(chartTitle, lineChart);

        // Donut Chart Container
        VBox pieChartBox = new VBox(15);
        pieChartBox.getStyleClass().add("chart-container");
        pieChartBox.setPrefWidth(300);
        
        Label pieTitle = new Label("Task Distribution");
        pieTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        PieChart pieChart = new PieChart();
        pieChart.getData().add(new PieChart.Data("To Do", getCount("DEADLINE")));
        pieChart.getData().add(new PieChart.Data("In Progress", getCount("IN_PROGRESS")));
        pieChart.getData().add(new PieChart.Data("Done", getCount("DONE")));
        pieChart.setLabelsVisible(false);
        pieChart.setLegendSide(Side.BOTTOM);
        
        pieChartBox.getChildren().addAll(pieTitle, pieChart);

        row.getChildren().addAll(lineChartBox, pieChartBox);
        return row;
    }

    private VBox createWorkProgressSection() {
        VBox section = new VBox(20);
        Label title = new Label("Recent Tasks");
        title.getStyleClass().add("text-title");
        
        HBox row = new HBox(20);
        
        int count = 0;
        for (Task t : taskList) {
            if (count >= 3) break;
            double progress = "DONE".equals(t.getStatus()) ? 1.0 : ("IN_PROGRESS".equals(t.getStatus()) ? 0.5 : 0.0);
            row.getChildren().add(createProgressCard(t.getTitle(), progress, "Start", t.getDeadline()));
            count++;
        }
        
        if (count == 0) {
            row.getChildren().add(new Label("No tasks found. Add a new task to get started!"));
        }
        
        section.getChildren().addAll(title, row);
        return section;
    }

    private VBox createProgressCard(String name, double progress, String start, String end) {
        VBox card = new VBox(12);
        card.getStyleClass().add("dashboard-card");
        card.setPrefWidth(280);
        
        Label nLabel = new Label(name);
        nLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        ProgressBar bar = new ProgressBar(progress);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(8);
        bar.setStyle("-fx-accent: #4f46e5;");
        
        HBox dates = new HBox();
        Label sLabel = new Label(start);
        Label eLabel = new Label(end);
        sLabel.getStyleClass().add("text-subtitle");
        eLabel.getStyleClass().add("text-subtitle");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        dates.getChildren().addAll(sLabel, spacer, eLabel);
        
        card.getChildren().addAll(nLabel, bar, dates);
        return card;
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(30);
        panel.setPrefWidth(320);
        panel.setPadding(new Insets(30));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #e1e4e8; -fx-border-width: 0 0 0 1;");

        // Simple Calendar Placeholder
        VBox calendar = new VBox(15);
        Label calTitle = new Label("Calendar");
        calTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        for (int i = 1; i <= 31; i++) {
            Label day = new Label(String.valueOf(i));
            day.setAlignment(Pos.CENTER);
            day.setPrefSize(30, 30);
            day.setStyle("-fx-background-color: #f5f6fa; -fx-background-radius: 5; -fx-font-size: 11px;");
            grid.add(day, (i-1)%7, (i-1)/7);
        }
        calendar.getChildren().addAll(calTitle, grid);

        // Upcoming Schedule
        VBox upcoming = new VBox(15);
        Label upTitle = new Label("Upcoming Schedule");
        upTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        VBox list = new VBox(10);
        int scheduled = 0;
        for (Task t : taskList) {
            if (scheduled >= 5) break;
            if ("DONE".equals(t.getStatus())) continue;
            
            String color = "#4f46e5"; // Default
            if (t.getDeadline().equals(LocalDate.now().toString())) color = "#ef4444";
            else if ("IN_PROGRESS".equals(t.getStatus())) color = "#f59e0b";

            list.getChildren().add(createScheduleItem(t.getTitle(), t.getDeadline(), color));
            scheduled++;
        }
        
        if (scheduled == 0) {
            list.getChildren().add(new Label("No upcoming tasks."));
        }

        upcoming.getChildren().addAll(upTitle, list);
        panel.getChildren().addAll(calendar, upcoming);
        return panel;
    }

    private HBox createScheduleItem(String title, String time, String color) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10));
        item.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12;");
        
        Region dot = new Region();
        dot.setPrefSize(8, 8);
        dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");
        
        VBox text = new VBox(2);
        Label tLbl = new Label(title);
        tLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1f2937;");
        Label timLbl = new Label(time);
        timLbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
        
        text.getChildren().addAll(tLbl, timLbl);
        item.getChildren().addAll(dot, text);
        return item;
    }

    private long getCount(String status) {
        return taskList.stream().filter(t -> status.equals(t.getStatus())).count();
    }
}
