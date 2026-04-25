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
                     "-fx-border-color: " + border + "; -fx-border-width: 2; -fx-border-radius: 16;");
        
        Label dateLbl = new Label(String.valueOf(date.getDayOfMonth()));
        dateLbl.setStyle("-fx-text-fill: " + (isToday ? "#4f46e5" : "#1f2937") + "; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        box.getChildren().add(dateLbl);

        VBox tasksBox = new VBox(4);
        for (Task t : tasks) {
            if (t.getDeadline().equals(date.toString())) {
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
        
        box.getChildren().add(tasksBox);
        return box;
    }
}
