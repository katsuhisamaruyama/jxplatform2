
import javafx.stage.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.effect.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import javafx.scene.shape.Line;
import javafx.scene.layout.*;

public class Sample108FX extends Application {
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    public void start(Stage stage) throws Exception {
        stage.setTitle("JavaFX Sample");
        stage.setWidth(700);
        stage.setHeight(500);
        
        final Pane canvas = new Pane();
        
        canvas.setOnMousePressed(event -> mousePressed(event, canvas));
        canvas.setOnMouseDragged(event -> mouseDragged(event, canvas));
        canvas.setOnMouseReleased(event -> mouseReleased(event, canvas));
        
        Scene scene = new Scene(canvas);
        stage.setScene(scene);
        stage.show();
    }
    
    Line line = null;
    
    private void mousePressed(MouseEvent event, Pane canvas) {
        line = new Line(event.getX(), event.getY(), event.getX(), event.getY());
        line.setStroke(Color.GREEN);
        line.setBlendMode(BlendMode.MULTIPLY);
        
        canvas.getChildren().add(line);
    }
    
    private void mouseDragged(MouseEvent event, Pane canvas) {
        if (line != null) {
            line.setEndX(event.getX());
            line.setEndY(event.getY());
        }
    }
    
    private void mouseReleased(MouseEvent event, Pane canvas) {
        if (line != null) {
            if (line.getStartX() == event.getX() && line.getStartY() == event.getY()) {
                canvas.getChildren().remove(line);
            }
        }
    }
}
