
import javafx.application.*;
import javafx.geometry.Pos;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

public class Sample101FX extends Application {
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    public void start(Stage stage) throws Exception {
        stage.setTitle("JavaFX Sample");
        stage.setWidth(200);
        stage.setHeight(80);
        
        BorderPane pane = new BorderPane();
        
        Label label = new Label("Enjoy JavaFX.");
        pane.setTop(label);
        BorderPane.setAlignment(label, Pos.CENTER);
        
        Button button = new Button("Push");
        pane.setCenter(button);
        BorderPane.setAlignment(button, Pos.CENTER);
        
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }
}
