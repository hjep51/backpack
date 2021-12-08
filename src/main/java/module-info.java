module Backpack {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.backpack to javafx.fxml;
    opens org.backpack.fxmlcontrollers to javafx.fxml;
    exports org.backpack;
}
