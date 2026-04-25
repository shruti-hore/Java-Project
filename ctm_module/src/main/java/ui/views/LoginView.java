package ui.views;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginView extends VBox {
    private final TextField emailField = new TextField();
    public TextField getEmailField() { return emailField; }
}
