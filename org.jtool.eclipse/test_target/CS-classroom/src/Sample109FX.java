
import javafx.stage.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

public class Sample109FX extends Application {
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    public void start(Stage stage) throws Exception {
        stage.setTitle("JavaFX Sample");
        stage.setWidth(700);
        stage.setHeight(500);
        
        MenuBar menuBar = new MenuBar();
        
        Menu fileMenu = new Menu("File");
        menuBar.getMenus().addAll(fileMenu);
        
        MenuItem openItem = new MenuItem("Open...");
        MenuItem saveItem = new MenuItem("Save");
        SeparatorMenuItem sp = new SeparatorMenuItem();
        MenuItem exitItem = new MenuItem("Exit");
        fileMenu.getItems().addAll(openItem, saveItem, sp, exitItem);
        
        exitItem.setOnAction(event -> Platform.exit());
        
        VBox menuArea = new VBox();
        menuArea.getChildren().addAll(menuBar);
        
        Scene scene = new Scene(menuArea);
        stage.setScene(scene);
        stage.show();
    }
}

