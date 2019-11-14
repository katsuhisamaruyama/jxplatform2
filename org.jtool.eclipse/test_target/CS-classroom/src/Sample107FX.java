
import javafx.stage.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.paint.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

public class Sample107FX extends Application {
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    final static int RADIUS = 20;
    
    public void start(Stage stage) throws Exception {
        stage.setTitle("JavaFX Sample");
        stage.setWidth(700);
        stage.setHeight(500);
        
        final Pane canvas = new Pane();
        
        canvas.setOnMousePressed(event -> mousePressed(event, canvas));
        
        Scene scene = new Scene(canvas);
        stage.setScene(scene);
        stage.show();
    }
    
    private void mousePressed(MouseEvent event, Pane canvas) {
        double x = event.getX();
        double y = event.getY();
        
        Circle circle = new Circle(x, y, RADIUS);
        circle.setStroke(Color.BLUE);
        circle.setFill(Color.WHITE);
        
        canvas.getChildren().add(circle);
    }
}
