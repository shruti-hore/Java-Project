package ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoginView extends BorderPane {

    private final TextField emailField;
    private final PasswordField passwordField;
    private final PasswordField confirmPasswordField;
    private final Button primaryButton;
    private final Button toggleButton;
    private final Label statusLabel;
    private final ProgressIndicator spinner;

    public LoginView() {
        getStyleClass().add("root");
        setStyle("-fx-background-color: #0d1117;");

        // Initialize components
        this.emailField = createInput("Enter your email");
        this.passwordField = createPasswordInput("Enter your password");
        this.confirmPasswordField = createPasswordInput("Confirm your password");
        this.confirmPasswordField.setVisible(false);
        this.confirmPasswordField.setManaged(false);
        
        this.primaryButton = createPrimaryButton("SIGN IN");
        this.toggleButton = new Button("No account? Register");
        this.toggleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #58a6ff; -fx-font-size: 12px; -fx-cursor: hand;");
        this.toggleButton.setMaxWidth(Double.MAX_VALUE);
        
        this.statusLabel = createStatusLabel();
        
        this.spinner = new ProgressIndicator();
        this.spinner.setVisible(false);
        this.spinner.setManaged(false);
        this.spinner.managedProperty().bind(this.spinner.visibleProperty());
        this.spinner.setPrefSize(20, 20);

        // Layout Assembly
        VBox centerContent = new VBox(20);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(40));
        
        centerContent.getChildren().add(createCard());
        
        setCenter(centerContent);
    }

    private VBox createCard() {
        VBox card = new VBox(15);
        card.setMaxWidth(400);
        card.setStyle("-fx-background-color: #161b22; -fx-background-radius: 12; -fx-padding: 30; -fx-border-color: #30363d; -fx-border-width: 1; -fx-border-radius: 12;");
        card.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Zero-Knowledge Login");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Manage your tasks efficiently and securely");
        subtitle.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 13px;");

        VBox emailBox = new VBox(5);
        Label eLbl = new Label("EMAIL ADDRESS");
        eLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #8b949e;");
        emailBox.getChildren().addAll(eLbl, emailField);

        VBox passBox = new VBox(5);
        Label pLbl = new Label("MASTER PASSWORD");
        pLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #8b949e;");
        passBox.getChildren().addAll(pLbl, passwordField, confirmPasswordField);

        card.getChildren().addAll(
            title, 
            subtitle, 
            new Region() { { setMinHeight(10); } }, 
            emailBox, 
            passBox, 
            spinner, 
            statusLabel, 
            primaryButton, 
            toggleButton
        );
        
        return card;
    }

    private TextField createInput(String placeholder) {
        TextField field = new TextField();
        field.setPromptText(placeholder);
        field.setStyle("-fx-background-color: #21262d; -fx-text-fill: white; -fx-padding: 12; -fx-background-radius: 6; -fx-border-color: #30363d; -fx-border-radius: 6;");
        return field;
    }

    private PasswordField createPasswordInput(String placeholder) {
        PasswordField field = new PasswordField();
        field.setPromptText(placeholder);
        field.setStyle("-fx-background-color: #21262d; -fx-text-fill: white; -fx-padding: 12; -fx-background-radius: 6; -fx-border-color: #30363d; -fx-border-radius: 6;");
        return field;
    }

    private Button createPrimaryButton(String label) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 6; -fx-cursor: hand;");
        return btn;
    }

    private Label createStatusLabel() {
        Label label = new Label("");
        label.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 13px;");
        label.setWrapText(true);
        return label;
    }

    // Getters
    public TextField getEmailField() { return emailField; }
    public PasswordField getPasswordField() { return passwordField; }
    public PasswordField getConfirmPasswordField() { return confirmPasswordField; }
    public Button getPrimaryButton() { return primaryButton; }
    public Button getToggleButton() { return toggleButton; }
    public Label getStatusLabel() { return statusLabel; }
    public ProgressIndicator getSpinner() { return spinner; }
}
