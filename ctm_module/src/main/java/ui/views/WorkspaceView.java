package ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import model.Team;
import java.util.List;
import java.util.function.Consumer;

public class WorkspaceView extends VBox {

    private final VBox teamListContainer = new VBox(15);
    private final Button createBtn = new Button("+ Create Team");
    private final Button joinBtn = new Button("Join Team");
    private final Consumer<Team> onTeamSelected;

    public WorkspaceView(List<Team> teams, Consumer<Team> onTeamSelected, Runnable onCreate, Runnable onJoin) {
        this.onTeamSelected = onTeamSelected;
        
        setSpacing(40);
        setPadding(new Insets(60));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #f5f6fa;");

        Label title = new Label("Choose your workspace");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        Label sub = new Label("Select a team to start collaborating or create a new one.");
        sub.setStyle("-fx-font-size: 16px; -fx-text-fill: #6b7280;");

        HBox top = new HBox(10, createBtn, joinBtn);
        top.setAlignment(Pos.CENTER);
        styleActionBtn(createBtn, "#4f46e5", "white");
        styleActionBtn(joinBtn, "white", "#4f46e5");
        
        createBtn.setOnAction(e -> onCreate.run());
        joinBtn.setOnAction(e -> onJoin.run());

        teamListContainer.setAlignment(Pos.CENTER);
        refreshTeams(teams);

        ScrollPane scroll = new ScrollPane(teamListContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        getChildren().addAll(title, sub, top, scroll);
    }

    private void styleActionBtn(Button btn, String bg, String text) {
        btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + text + "; -fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 12; -fx-border-color: #4f46e5; -fx-border-radius: 12; -fx-cursor: hand;");
    }

    public void refreshTeams(List<Team> teams) {
        teamListContainer.getChildren().clear();
        FlowPane flow = new FlowPane(20, 20);
        flow.setAlignment(Pos.CENTER);
        
        for (Team team : teams) {
            VBox card = createTeamCard(team);
            card.setOnMouseClicked(e -> onTeamSelected.accept(team));
            flow.getChildren().add(card);
        }
        
        if (teams.isEmpty()) {
            Label empty = new Label("No teams found. Create one to get started!");
            empty.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic;");
            teamListContainer.getChildren().add(empty);
        } else {
            teamListContainer.getChildren().add(flow);
        }
    }

    private VBox createTeamCard(Team team) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(200, 150);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 5); -fx-cursor: hand;");
        
        Region icon = new Region();
        icon.setPrefSize(40, 40);
        icon.setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 20;");
        
        Label name = new Label(team.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1f2937;");
        
        Label owner = new Label("Owner: " + team.getOwnerId());
        owner.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        
        card.getChildren().addAll(icon, name, owner);
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(79,70,229,0.1), 20, 0, 0, 10); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 5); -fx-cursor: hand;"));
        
        return card;
    }
}
