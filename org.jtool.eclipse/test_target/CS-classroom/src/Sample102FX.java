
import javafx.application.*;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

public class Sample102FX extends Application {
    
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
        
        ButtonEventHandler bh = new ButtonEventHandler(label);
        button.setOnAction(bh);
        
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }
}

class ButtonEventHandler implements EventHandler<ActionEvent> {
    private Label label;
    private boolean flag = true;
    
    ButtonEventHandler(Label l) {
        label = l;
    }
    
    public void handle(ActionEvent event) {
        if (flag == true) {
            label.setText("Enjoy JavaFX.");
            flag = false;
        } else {
            label.setText("Enjoy Java.");
            flag = true;
        }
    }
}
