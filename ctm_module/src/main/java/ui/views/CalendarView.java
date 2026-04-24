package ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import model.Task;
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
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #0d1117;");

        HBox nav = new HBox(20);
        nav.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("CALENDAR");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button prev = new Button("←");
        Button next = new Button("→");
        styleNavBtn(prev);
        styleNavBtn(next);
        
        monthLabel = new Label();
        monthLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-min-width: 150px;");
        monthLabel.setAlignment(Pos.CENTER);

        prev.setOnAction(e -> { currentYearMonth = currentYearMonth.minusMonths(1); refresh(); });
        next.setOnAction(e -> { currentYearMonth = currentYearMonth.plusMonths(1); refresh(); });

        nav.getChildren().addAll(title, spacer, prev, monthLabel, next);

        calendarGrid = new GridPane();
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        VBox.setVgrow(calendarGrid, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(calendarGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(nav, scroll);
        refresh();
    }

    private void styleNavBtn(Button btn) {
        btn.setStyle("-fx-background-color: #21262d; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
    }

    public void refresh() {
        calendarGrid.getChildren().clear();
        monthLabel.setText(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentYearMonth.getYear());

        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (int i = 0; i < 7; i++) {
            Label dayLbl = new Label(days[i]);
            dayLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-weight: bold; -fx-padding: 10;");
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
        VBox box = new VBox(5);
        box.setMinSize(140, 100);
        box.setStyle("-fx-background-color: #161b22; -fx-background-radius: 10; -fx-padding: 10; -fx-border-color: #30363d; -fx-border-width: 1;");
        
        Label dateLbl = new Label(String.valueOf(date.getDayOfMonth()));
        dateLbl.setStyle("-fx-text-fill: " + (date.equals(LocalDate.now()) ? "#8b5cf6" : "white") + "; -fx-font-weight: bold;");
        
        box.getChildren().add(dateLbl);

        for (Task t : tasks) {
            if (t.getDeadline().equals(date.toString())) {
                Label taskLbl = new Label("• " + t.getTitle());
                String color = "#3498db";
                if ("DONE".equals(t.getStatus())) color = "#2ecc71";
                else if (date.isBefore(LocalDate.now())) color = "#e74c3c";
                
                taskLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px;");
                box.getChildren().add(taskLbl);
            }
        }
        
        return box;
    }
}
