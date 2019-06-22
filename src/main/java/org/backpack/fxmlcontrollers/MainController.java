package org.backpack.fxmlcontrollers;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.backpack.io.PackTheBag;

/**
 *
 * @author hjep
 */
public class MainController implements Initializable {

    @FXML
    private Button targetfolderbutton;

    @FXML
    private Button addfiletobackpack;

    @FXML
    private Button addfoldertobackpack;
    
    @FXML
    private Button bagandpack;

    @FXML
    private TextField targetfolderfield;
    
    @FXML
    private ListView filelistview;
    
    @FXML
    private Label statuslabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statuslabel.setText("");

        targetfolderbutton.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File targetFolder = directoryChooser.showDialog(stage);
            targetfolderfield.setText(targetFolder.getAbsolutePath());
        });

        addfiletobackpack.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            List<File> files = fileChooser.showOpenMultipleDialog(stage);
            if (files != null) {
                for (File file : files) {
                    filelistview.getItems().add(file.getAbsolutePath());
                }
            }
        });

        addfoldertobackpack.setOnAction(e -> {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File folder = directoryChooser.showDialog(stage);
            if (folder != null) {
                filelistview.getItems().add(folder.getAbsolutePath());
            }
        });
        
        bagandpack.setOnAction(e -> {
            statuslabel.setText("Packing the bag, please wait");
            PackTheBag bag = new PackTheBag(filelistview.getItems(), targetfolderfield.getText(), true);
            if(bag.pack()) {
                statuslabel.setText("Bag has been packed");
            }else{
                statuslabel.setText("Ups, bag was lost in transit");
            }
        });
    }
}
