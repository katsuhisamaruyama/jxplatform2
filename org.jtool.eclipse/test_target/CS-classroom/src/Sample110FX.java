
import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.event.*;
import java.util.Optional;

public class Sample110FX extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    public void start(Stage stage) throws Exception {
        stage.setTitle("JavaFX Sample");
        stage.setWidth(300);
        stage.setHeight(100);
        
        Group root = new Group();
        
        Button button = new Button("Question");
        button.setOnAction(event -> action(event));
        root.getChildren().add(button);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    
    private void action(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Can you see me?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            Platform.exit();
        }
    }
}
