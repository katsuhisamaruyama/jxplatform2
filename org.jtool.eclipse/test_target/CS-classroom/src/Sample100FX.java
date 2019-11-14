
import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;

public class Sample100FX extends Application {
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    public void start(Stage stage) throws Exception {
        stage.setTitle("JavaFX Sample");
        stage.setWidth(200);
        stage.setHeight(80);
        
        Label label = new Label("Enjoy JavaFX.");
        
        Scene scene = new Scene(label);
        
        stage.setScene(scene);
        stage.show();
    }
}
