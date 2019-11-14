
import javafx.stage.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Line;

public class Sample106FX extends Application {
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    public void start(Stage stage) throws Exception {
        stage.setTitle("JavaFX Sample");
        stage.setWidth(700);
        stage.setHeight(500);
        
        Text text = new Text(200, 100, "Java");
        text.setFill(Color.RED);
        text.setFont(Font.font("SanSerif", FontPosture.ITALIC, 18));
        
        Line line = new Line(10, 10, 690, 490);
        line.setStroke(Color.BLUE);
        
        Group root = new Group();
        root.getChildren().addAll(text, line);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
