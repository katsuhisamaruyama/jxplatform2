
import javafx.stage.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.text.*;
import javafx.scene.paint.*;

public class Sample105FX extends Application {
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    public void start(Stage stage) throws Exception {
        stage.setTitle("JavaFX Sample");
        stage.setWidth(700);
        stage.setHeight(500);
        
        Canvas canvas = new Canvas(700, 500);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        gc.setFill(Color.RED);
        gc.setFont(Font.font("SanSerif", FontPosture.ITALIC, 18));
        gc.fillText("Java", 200, 100);
        
        gc.setStroke(Color.BLUE);
        gc.strokeLine(10, 10, 690, 490);
        
        Group root = new Group();
        root.getChildren().add(canvas);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
