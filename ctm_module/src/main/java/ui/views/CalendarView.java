package ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import client.model.Task;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class CalendarView extends VBox {
    private YearMonth currentYearMonth;
    private List<Task> tasks;
    private GridPane calendarGrid;
    private Label monthLabel;

    public CalendarView(List<Task> tasks) {
        this.tasks = tasks;
        this.currentYearMonth = YearMonth.now();
        setSpacing(25);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #f3f4f6;"); // Match main background

        HBox nav = new HBox(20);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(0, 0, 10, 0));
        
        Label title = new Label("CALENDAR");
        title.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button prev = new Button("←");
        Button next = new Button("→");
        styleNavBtn(prev);
        styleNavBtn(next);
        
        monthLabel = new Label();
        monthLabel.setStyle("-fx-text-fill: #4f46e5; -fx-font-size: 20px; -fx-font-weight: bold; -fx-min-width: 200px;");
        monthLabel.setAlignment(Pos.CENTER);

        prev.setOnAction(e -> { currentYearMonth = currentYearMonth.minusMonths(1); refresh(); });
        next.setOnAction(e -> { currentYearMonth = currentYearMonth.plusMonths(1); refresh(); });

        nav.getChildren().addAll(title, spacer, prev, monthLabel, next);

        calendarGrid = new GridPane();
        calendarGrid.setHgap(15);
        calendarGrid.setVgap(15);
        VBox.setVgrow(calendarGrid, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(calendarGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(nav, scroll);
        refresh();
    }

    private void styleNavBtn(Button btn) {
        btn.setStyle("-fx-background-color: white; -fx-text-fill: #4f46e5; -fx-background-radius: 10; -fx-padding: 8 18; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2); -fx-font-weight: bold;");
    }

    public void refresh() {
        calendarGrid.getChildren().clear();
        monthLabel.setText(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase() + " " + currentYearMonth.getYear());

        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (int i = 0; i < 7; i++) {
            Label dayLbl = new Label(days[i]);
            dayLbl.setStyle("-fx-text-fill: #9ca3af; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5 0;");
            dayLbl.setMaxWidth(Double.MAX_VALUE);
            dayLbl.setAlignment(Pos.CENTER);
            calendarGrid.add(dayLbl, i, 0);
        }

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentYearMonth.lengthOfMonth();

        for (int i = 0; i < daysInMonth; i++) {
            int row = (i + dayOfWeek) / 7 + 1;
            int col = (i + dayOfWeek) % 7;
            
            LocalDate date = firstOfMonth.plusDays(i);
            VBox dayBox = createDayBox(date);
            calendarGrid.add(dayBox, col, row);
        }
    }

    private VBox createDayBox(LocalDate date) {
        VBox box = new VBox(8);
        box.setMinSize(150, 120);
        
        boolean isToday = date.equals(LocalDate.now());
        String bg = isToday ? "#eef2ff" : "white";
        String border = isToday ? "#4f46e5" : "transparent";
        
        box.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 16; -fx-padding: 15; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 5); " +
                     "-fx-border-color: " + border + "; -fx-border-width: 2; -fx-border-radius: 16; -fx-cursor: hand;");

        box.setOnMouseEntered(e -> box.setStyle(box.getStyle() + "-fx-effect: dropshadow(three-pass-box, rgba(79,70,229,0.15), 15, 0, 0, 8);"));
        box.setOnMouseExited(e -> box.setStyle(box.getStyle().replace("-fx-effect: dropshadow(three-pass-box, rgba(79,70,229,0.15), 15, 0, 0, 8);", "")));
        
        box.setOnMouseClicked(e -> showDayTasks(date));

        Label dateLbl = new Label(String.valueOf(date.getDayOfMonth()));
        dateLbl.setStyle("-fx-text-fill: " + (isToday ? "#4f46e5" : "#1f2937") + "; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        box.getChildren().add(dateLbl);

        VBox tasksBox = new VBox(4);
        int count = 0;
        for (Task t : tasks) {
            if (t.getDeadline().equals(date.toString())) {
                count++;
                if (count <= 2) {
                    Label taskLbl = new Label(t.getTitle());
                    taskLbl.setMaxWidth(130);
                    taskLbl.setEllipsisString("...");
                    
                    String taskBg = "#4f46e5";
                    if ("DONE".equals(t.getStatus())) taskBg = "#10b981";
                    else if ("IN_PROGRESS".equals(t.getStatus())) taskBg = "#f59e0b";
                    else if (date.isBefore(LocalDate.now())) taskBg = "#ef4444";
                    
                    taskLbl.setStyle("-fx-background-color: " + taskBg + "; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 6;");
                    tasksBox.getChildren().add(taskLbl);
                }
            }
        }
        
        if (count > 2) {
            Label moreLbl = new Label("+" + (count - 2) + " more tasks");
            moreLbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 0 0 5;");
            tasksBox.getChildren().add(moreLbl);
        }
        
        box.getChildren().add(tasksBox);
        return box;
    }

    private void showDayTasks(LocalDate date) {
        VBox overlay = new VBox(15);
        overlay.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 30, 0, 0, 15);");
        overlay.setMaxSize(400, 500);
        overlay.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Tasks for " + date.toString());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        VBox list = new VBox(10);
        for (Task t : tasks) {
            if (t.getDeadline().equals(date.toString())) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #f9fafb; -fx-padding: 10; -fx-background-radius: 10;");
                
                Region dot = new Region();
                dot.setPrefSize(8, 8);
                String color = "#4f46e5";
                if ("DONE".equals(t.getStatus())) color = "#10b981";
                dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");
                
                Label tTitle = new Label(t.getTitle());
                tTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");
                
                row.getChildren().addAll(dot, tTitle);
                list.getChildren().add(row);
            }
        }

        if (list.getChildren().isEmpty()) {
            list.getChildren().add(new Label("No tasks for this day."));
        }

        Button close = new Button("CLOSE");
        close.setMaxWidth(Double.MAX_VALUE);
        close.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 10; -fx-cursor: hand;");
        
        // This is a bit tricky since CalendarView doesn't have a direct reference to DashboardUI's hideOverlay.
        // However, we can use a StackPane overlay if we wrap the CalendarView or pass a callback.
        // For simplicity and matching existing patterns, I'll assume it's shown in a generic popup or similar.
        // Wait, DashboardUI uses showOverlay(Node content).
        
        // I will use a simple internal overlay if possible, or just print for now.
        // Actually, I should probably make showDayTasks a callback to DashboardUI.
        
        overlay.getChildren().addAll(title, new ScrollPane(list), close);
        
        // Let's use a workaround: find the parent StackPane (mainStack)
        javafx.scene.Parent parent = getParent();
        while (parent != null && !(parent instanceof javafx.scene.layout.StackPane)) {
            parent = parent.getParent();
        }
        
        if (parent instanceof javafx.scene.layout.StackPane) {
            javafx.scene.layout.StackPane stack = (javafx.scene.layout.StackPane) parent;
            Region glass = new Region();
            glass.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
            stack.getChildren().addAll(glass, overlay);
            close.setOnAction(e -> stack.getChildren().removeAll(glass, overlay));
            glass.setOnMouseClicked(e -> stack.getChildren().removeAll(glass, overlay));
        }
    }
}
