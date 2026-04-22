package ui.views;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

public class SidebarView extends VBox {
    private Consumer<String> onNavigate;
    private Label activeNav;

    public SidebarView(Consumer<String> navigateAction) {
        this.onNavigate = navigateAction;
        setPrefWidth(220);
        getStyleClass().add("sidebar");
        setStyle("-fx-background-color: #161b22; -fx-padding: 30 15;");

        Label logo = new Label("TASKER PRO");
        logo.setStyle("-fx-text-fill: #8b5cf6; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0 0 40 0;");
        logo.setMaxWidth(Double.MAX_VALUE);
        logo.setAlignment(Pos.CENTER);

        Label dashboardNav = createNavItem("Dashboard", "DASHBOARD");
        Label myTasksNav = createNavItem("My Tasks", "KANBAN");
        Label projectsNav = createNavItem("Projects", "PROJECTS");
        Label calendarNav = createNavItem("Calendar", "CALENDAR");
        Label settingsNav = createNavItem("Settings", "SETTINGS");
        Label logoutNav = createNavItem("Logout", "LOGOUT");

        getChildren().addAll(logo, dashboardNav, myTasksNav, projectsNav, calendarNav, new Region(), settingsNav, logoutNav);
        VBox.setVgrow(getChildren().get(5), Priority.ALWAYS);

        // Default selection
        selectNav(dashboardNav);
    }

    private Label createNavItem(String text, String viewKey) {
        Label nav = new Label(text);
        nav.getStyleClass().add("sidebar-nav-item");
        nav.setMaxWidth(Double.MAX_VALUE);
        nav.setStyle("-fx-padding: 12 15; -fx-text-fill: #8b949e; -fx-cursor: hand; -fx-font-size: 14px; -fx-background-radius: 8;");
        
        nav.setOnMouseEntered(e -> { if (nav != activeNav) nav.setStyle(nav.getStyle() + "-fx-background-color: #21262d; -fx-text-fill: white;"); });
        nav.setOnMouseExited(e -> { if (nav != activeNav) nav.setStyle(nav.getStyle().split("-fx-background-color")[0]); });
        
        nav.setOnMouseClicked(e -> {
            selectNav(nav);
            onNavigate.accept(viewKey);
        });
        
        return nav;
    }

    private void selectNav(Label nav) {
        if (activeNav != null) {
            activeNav.setStyle("-fx-padding: 12 15; -fx-text-fill: #8b949e; -fx-cursor: hand; -fx-font-size: 14px; -fx-background-radius: 8;");
        }
        activeNav = nav;
        activeNav.setStyle("-fx-padding: 12 15; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 8; -fx-background-color: #8b5cf6; -fx-font-weight: bold;");
    }
}
