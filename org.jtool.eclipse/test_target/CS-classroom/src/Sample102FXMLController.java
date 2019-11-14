
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;

public class Sample102FXMLController {
    
    private boolean flag = true;
    
    @FXML
    private Label label;
    
    @FXML
    public void pushAction(ActionEvent event) {
        if (flag == true) {
            label.setText("Enjoy JavaFX.");
            flag = false;
        } else {
            label.setText("Enjoy Java.");
            flag = true;
        }
    }
}
