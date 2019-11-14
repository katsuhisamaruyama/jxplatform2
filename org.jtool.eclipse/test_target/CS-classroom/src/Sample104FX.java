
import javafx.application.*;
import javafx.geometry.Pos;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

public class Sample104FX extends Application {
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    public void start(Stage stage) throws Exception {
        stage.setTitle("JavaFX Sample");
        stage.setWidth(200);
        stage.setHeight(80);
        
        BorderPane pane = new BorderPane();
        
        Label label = new Label("Enjoy Java.");
        pane.setTop(label);
        BorderPane.setAlignment(label, Pos.CENTER);
        
        Button button = new Button("Push");
        pane.setCenter(button);
        BorderPane.setAlignment(button, Pos.CENTER);
        
        LabelManager lm = new LabelManager(label);
        
        button.setOnAction(event -> lm.change());
        
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }
    
    class LabelManager {
        private Label label;
        private boolean flag = true;
        
        LabelManager(Label l) {
            label = l;
        }
        
        public void change() {
            if (flag == true) {
                label.setText("Enjoy JavaFX.");
                flag = false;
            } else {
                label.setText("Enjoy Java.");
                flag = true;
            }
        }
    }
}
