package ui.views;

import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

public class SidebarView extends VBox {
    private Consumer<String> onNavigate;
    private Label activeNav;
    private final Button addTaskBtn;

    public SidebarView(Consumer<String> navigateAction, boolean teamSelected) {
        this.onNavigate = navigateAction;
        setPrefWidth(240);
        getStyleClass().add("sidebar");
        setPadding(new Insets(0));

        // Profile Section
        VBox profile = new VBox(10);
        profile.setAlignment(Pos.CENTER);
        profile.setPadding(new Insets(40, 20, 40, 20));
        
        Region avatar = new Region();
        avatar.setPrefSize(60, 60);
        avatar.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 30;");
        
        String userEmail = utils.UserSession.getCurrentUserEmail();
        String displayUser = (userEmail != null && userEmail.contains("@")) ? userEmail.split("@")[0] : "User";
        
        Label name = new Label(displayUser.toUpperCase());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1f2937;");
        Label role = new Label(userEmail != null ? userEmail : "");
        role.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 10px;");
        
        profile.getChildren().addAll(avatar, name, role);

        Label teamsNav = createNavItem("Teams", "TEAMS");
        Label calendarNav = createNavItem("Calendar", "CALENDAR");
        
        VBox navBox = new VBox(5);
        navBox.setPadding(new Insets(0, 15, 0, 15));
        navBox.getChildren().addAll(teamsNav, calendarNav);

        if (teamSelected) {
            Label dashboardNav = createNavItem("Dashboard", "DASHBOARD");
            Label trackingNav = createNavItem("Tracking", "KANBAN");
            Label projectsNav = createNavItem("Projects", "PROJECTS");
            Label historyNav = createNavItem("Work History", "HISTORY");
            
            navBox.getChildren().add(1, dashboardNav);
            navBox.getChildren().add(2, trackingNav);
            navBox.getChildren().add(3, projectsNav);
            navBox.getChildren().add(4, historyNav);
        }

        Label toolsHeader = new Label("TOOLS");
        toolsHeader.setStyle("-fx-text-fill: #9ca3af; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 30 20 10 20;");
        
        Label settingsNav = createNavItem("Settings", "SETTINGS");
        Label logoutNav = createNavItem("Logout", "LOGOUT");

        navBox.getChildren().addAll(toolsHeader, settingsNav, logoutNav);

        addTaskBtn = new Button("+ Add New Task");
        addTaskBtn.getStyleClass().add("button-primary");
        addTaskBtn.setMaxWidth(Double.MAX_VALUE);
        addTaskBtn.setDisable(!teamSelected);
        VBox.setMargin(addTaskBtn, new Insets(40, 20, 20, 20));

        getChildren().addAll(profile, navBox, new Region(), addTaskBtn);
        VBox.setVgrow(getChildren().get(2), Priority.ALWAYS);

        if (teamSelected) selectNav((Label)navBox.getChildren().get(1)); // Default to Dashboard
        else selectNav(teamsNav); // Default to Teams
    }

    private Label createNavItem(String text, String viewKey) {
        Label nav = new Label(text);
        nav.getStyleClass().add("sidebar-nav-item");
        nav.setMaxWidth(Double.MAX_VALUE);
        
        nav.setOnMouseClicked(e -> {
            selectNav(nav);
            onNavigate.accept(viewKey);
        });
        
        return nav;
    }

    private void selectNav(Label nav) {
        if (activeNav != null) {
            activeNav.getStyleClass().remove("sidebar-nav-active");
        }
        activeNav = nav;
        activeNav.getStyleClass().add("sidebar-nav-active");
    }
    public Button getAddTaskBtn() {
        return addTaskBtn;
    }
}
