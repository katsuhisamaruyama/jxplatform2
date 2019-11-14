
import javafx.application.*;
import javafx.fxml.*;
import javafx.stage.*;
import javafx.scene.*;

public class Sample102FXML extends Application {
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    public void start(Stage stage) throws Exception {
        stage.setTitle("JavaFX Sample");
        stage.setWidth(200);
        stage.setHeight(80);
        
        Parent root = FXMLLoader.load(getClass().getResource("Sample102FXML.xml"));
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
