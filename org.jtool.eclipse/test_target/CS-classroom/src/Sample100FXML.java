
import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.fxml.*;

public class Sample100FXML extends Application {
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    public void start(Stage stage) throws Exception {
        stage.setTitle("JavaFX Sample");
        stage.setWidth(200);
        stage.setHeight(80);
        
        Parent root = FXMLLoader.load(getClass().getResource("Sample100FXML.xml"));
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
