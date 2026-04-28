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
import javafx.scene.control.ProgressIndicator;
import client.model.Task;
import ui.components.StatCard;
import java.time.LocalDate;

public class DashboardView extends BorderPane {
    private ObservableList<Task> taskList;

    private final VBox workspaceListContainer = new VBox(10);
    private final Button createWorkspaceButton = new Button("+ New");
    private final Button joinWorkspaceButton = new Button("Join");
    private final Label syncStatusLabel = new Label();
    private final ProgressIndicator syncSpinner = new ProgressIndicator();
    private final Button addTaskButton = new Button("+ Add Task");
    private final Button viewTasksButton = new Button("View Tasks");

    public VBox getWorkspaceListContainer() { return workspaceListContainer; }
    public Button getCreateWorkspaceButton() { return createWorkspaceButton; }
    public Button getJoinWorkspaceButton() { return joinWorkspaceButton; }
    public Label getSyncStatusLabel() { return syncStatusLabel; }
    public ProgressIndicator getSyncSpinner() { return syncSpinner; }
    public Button getAddTaskButton() { return addTaskButton; }
    public Button getViewTasksButton() { return viewTasksButton; }

    private model.Team team;

    public DashboardView(ObservableList<Task> tasks, model.Team team) {
        this.taskList = tasks;
        this.team = team;
        getStyleClass().add("root");

        // --- TOP BAR ---
        setTop(createTopBar());

        refresh();
    }

    public void refresh() {
        // --- CENTER CONTENT (Scrollable) ---
        VBox centerContent = new VBox(30);
        centerContent.setPadding(new Insets(30));
        
        centerContent.getChildren().addAll(
            createWelcomeCard(),
            createTeamHeader(),
            createTeamMembersSection(),
            createStatsRow(),
            createWorkloadAnalysisSection(),
            createTimelineSection(),
            createWorkspaceSection()
        );

        ScrollPane scrollPane = new ScrollPane(centerContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setCenter(scrollPane);

        // --- RIGHT PANEL ---
        setRight(createRightPanel());
    }

    private final TextField codeField = new TextField("Loading...");
    private final HBox teamCodeRow = new HBox(10);

    public void setTeamCode(String code) {
        if (code != null && !code.isEmpty()) {
            codeField.setText(code);
            teamCodeRow.setVisible(true);
            teamCodeRow.setManaged(true);
        } else {
            teamCodeRow.setVisible(false);
            teamCodeRow.setManaged(false);
        }
    }

    private VBox createTeamHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(30));
        header.setStyle("-fx-background-color: linear-gradient(to right, #4f46e5, #8b5cf6); -fx-background-radius: 20;");

        Label teamName = new Label(team != null ? team.getName().toUpperCase() : "NO TEAM SELECTED");
        teamName.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

        String ownerInfo = (team != null && team.getOwnerUsername() != null) ? team.getOwnerUsername() : (team != null ? team.getOwnerId() : "Unknown");
        Label position = new Label("OWNER: " + ownerInfo); 
        position.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 14px; -fx-font-weight: bold;");

        Label codeLbl = new Label("Invite Code:");
        codeLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");
        
        codeField.setEditable(false);
        codeField.setStyle("-fx-font-family: 'monospace'; -fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 4;");
        
        Button copyBtn = new Button("Copy");
        copyBtn.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        copyBtn.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(codeField.getText());
            clipboard.setContent(content);
        });

        teamCodeRow.getChildren().addAll(codeLbl, codeField, copyBtn);
        teamCodeRow.setAlignment(Pos.CENTER_LEFT);
        teamCodeRow.setVisible(false);
        teamCodeRow.setManaged(false);

        header.getChildren().addAll(teamName, position, teamCodeRow);
        return header;
    }

    private VBox createTeamMembersSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label title = new Label("TEAM MEMBERS");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937; -fx-font-size: 14px;");

        HBox membersBox = new HBox(15);
        membersBox.setAlignment(Pos.CENTER_LEFT);

        if (team != null && team.getMembers() != null) {
            for (String member : team.getMembers()) {
                VBox m = new VBox(5);
                m.setAlignment(Pos.CENTER);
                Region avatar = new Region();
                avatar.setPrefSize(40, 40);
                avatar.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 20; -fx-border-color: #e5e7eb; -fx-border-radius: 20;");
                Label name = new Label(member.contains("@") ? member.split("@")[0] : member);
                name.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");
                m.getChildren().addAll(avatar, name);
                membersBox.getChildren().add(m);
            }
        }

        section.getChildren().addAll(title, membersBox);
        return section;
    }

    private VBox createTimelineSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label title = new Label("TEAM TIMELINE (RECENT ACTIVITY)");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937; -fx-font-size: 14px;");

        VBox list = new VBox(10);
        int count = 0;
        for (Task t : taskList) {
            if (count >= 5) break;
            Label item = new Label("• " + t.getTitle() + " - " + t.getStatus() + " (" + t.getDeadline() + ")");
            item.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 13px;");
            list.getChildren().add(item);
            count++;
        }

        if (count == 0) list.getChildren().add(new Label("No recent activity."));

        section.getChildren().addAll(title, list);
        return section;
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

        syncSpinner.setPrefSize(14, 14);
        syncSpinner.setVisible(false);
        syncStatusLabel.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");

        topBar.getChildren().addAll(title, searchField, spacer, syncSpinner, syncStatusLabel, dateLabel, dateSelector);
        return topBar;
    }

    private VBox createWelcomeCard() {
        VBox welcome = new VBox(15);
        welcome.getStyleClass().add("welcome-card");
        
        String username = utils.UserSession.getCurrentUserName();
        Label title = new Label("Welcome back, " + username);
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        long pending = getCount("DEADLINE") + getCount("IN_PROGRESS");
        Label sub = new Label("You have " + pending + " tasks remaining. Keep pushing forward!");
        sub.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 14px;");
        
        viewTasksButton.setStyle("-fx-background-color: white; -fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 25;");
        addTaskButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 25; -fx-border-color: white; -fx-border-radius: 10;");

        HBox actions = new HBox(15, viewTasksButton, addTaskButton);
        
        welcome.getChildren().addAll(title, sub, actions);
        return welcome;
    }

    private VBox createWorkspaceSection() {
        VBox section = new VBox(15);
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Your Workspaces");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        joinWorkspaceButton.setStyle("-fx-background-color: #30363d; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        createWorkspaceButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        HBox buttons = new HBox(10, joinWorkspaceButton, createWorkspaceButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        
        header.getChildren().addAll(title, spacer, buttons);
        
        section.getChildren().addAll(header, workspaceListContainer);
        return section;
    }

    public VBox createWorkspaceCard(String name, String ownerUserId, String lastSynced) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #161b22; -fx-background-radius: 12; -fx-border-color: #30363d; -fx-border-width: 1; -fx-padding: 15; -fx-cursor: hand;");
        
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        
        Label ownerLbl = new Label("Owner: " + ownerUserId);
        ownerLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px;");
        
        Label syncLbl = new Label("Last Synced: " + lastSynced);
        syncLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");
        
        card.getChildren().addAll(nameLbl, ownerLbl, syncLbl);
        return card;
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

    private VBox createWorkloadAnalysisSection() {
        VBox section = new VBox(20);
        section.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        
        Label title = new Label("WORKLOAD ANALYSIS");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937; -fx-font-size: 14px;");
        
        long high = taskList.stream().filter(t -> "High".equalsIgnoreCase(t.getPriority())).count();
        long medium = taskList.stream().filter(t -> "Medium".equalsIgnoreCase(t.getPriority())).count();
        long low = taskList.stream().filter(t -> "Low".equalsIgnoreCase(t.getPriority())).count();
        
        HBox stats = new HBox(60);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.getChildren().addAll(
            createWorkloadItem("HIGH PRIORITY", high, "#ef4444"),
            createWorkloadItem("MEDIUM PRIORITY", medium, "#f59e0b"),
            createWorkloadItem("LOW PRIORITY", low, "#10b981"),
            createWorkloadItem("TOTAL ACTIVE", taskList.stream().filter(t -> !"DONE".equals(t.getStatus())).count(), "#4f46e5")
        );
        
        section.getChildren().addAll(title, stats);
        return section;
    }

    private VBox createWorkloadItem(String label, long count, String color) {
        VBox item = new VBox(5);
        Label countLbl = new Label(String.valueOf(count));
        countLbl.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label labelLbl = new Label(label);
        labelLbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px; -fx-font-weight: bold;");
        item.getChildren().addAll(countLbl, labelLbl);
        return item;
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
