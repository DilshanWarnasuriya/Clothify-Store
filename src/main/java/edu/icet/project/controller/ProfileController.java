package edu.icet.project.controller;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import edu.icet.project.bo.BoFactory;
import edu.icet.project.bo.custom.UserBo;
import edu.icet.project.dto.User;
import edu.icet.project.util.AlertMessage;
import edu.icet.project.util.AlertType;
import edu.icet.project.util.BoType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Objects;

public class ProfileController {

    @FXML
    private Circle imageCircle;
    @FXML
    private Label lblUserName;
    @FXML
    private JFXTextField txtAddress;
    @FXML
    private JFXTextField txtContactNo;
    @FXML
    private JFXTextField txtEmail;
    @FXML
    private JFXPasswordField txtPassword;
    @FXML
    private Circle loggedUserImage;
    @FXML
    private Label loggedUserName;
    @FXML
    private Label loggedUserType;

    @FXML
    private JFXPasswordField txtConfirmPassword;
    @FXML
    private JFXPasswordField txtCurrentPassword;
    @FXML
    private JFXPasswordField txtNewPassword;

    private final UserBo userBo = BoFactory.getInstance().getBo(BoType.USER);
    private static User loggedUser;
    private String url;

    public void setUser(User user) {
        loggedUser = user;
        loggedUserDetail();
        setUserDetails();
    }

    private void loggedUserDetail() {
        loggedUserName.setText(loggedUser.getName());
        loggedUserType.setText(loggedUser.getUserType());
        loggedUserImage.setFill(new ImagePattern(new Image(loggedUser.getImageUrl())));
    }

    // set user details to textBoxes
    private void setUserDetails() {
        imageCircle.setFill(new ImagePattern(new Image(loggedUser.getImageUrl())));
        lblUserName.setText(loggedUser.getName());
        txtEmail.setText(loggedUser.getEmail());
        txtAddress.setText(loggedUser.getAddress());
        txtContactNo.setText(loggedUser.getContactNo());
    }

    // Select image Button Action Event
    @FXML
    void selectImageOnAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        File file = fileChooser.showOpenDialog(imageCircle.getScene().getWindow());

        try {
            if (file != null) {
                Path path = new File("src/main/resources/images/profile/" + file.getName()).toPath();
                Files.copy(file.toPath(), path, StandardCopyOption.REPLACE_EXISTING);

                url = path.toUri().toString();
                imageCircle.setFill(new ImagePattern(new Image(path.toUri().toString())));
            }
        } catch (IOException e) {
            AlertMessage.getInstance().informerAlert(AlertType.ERROR, e.getMessage());
        }
    }

    // Update Button Action Event
    @FXML
    void updateDetailOnAction() {
        String email = txtEmail.getText();
        String address = txtAddress.getText();
        String contactNo = txtContactNo.getText();
        String imageUrl = url == null ? loggedUser.getImageUrl() : url;
        String password = txtPassword.getText();
        String userPassword = new String(Base64.getDecoder().decode(loggedUser.getPassword()));

        if (email.isEmpty() || address.isEmpty() || contactNo.isEmpty() || password.isEmpty()) {
            AlertMessage.getInstance().informerAlert(AlertType.WARNING, "Please fill all data");
        } else if (loggedUser.getEmail().equals(email) && loggedUser.getAddress().equals(address) && loggedUser.getContactNo().equals(contactNo)) {
            AlertMessage.getInstance().informerAlert(AlertType.WARNING, "No any changes");
        } else if (contactNo.length() != 10 || contactNo.charAt(0) != '0') {
            AlertMessage.getInstance().informerAlert(AlertType.WARNING, "Please enter valid contact number");
        } else if (password.length() < 8) {
            AlertMessage.getInstance().informerAlert(AlertType.WARNING, "Please enter 8 or mor characters for password");
        } else if (!userPassword.equals(password)) {
            AlertMessage.getInstance().informerAlert(AlertType.WARNING, "Password is wrong. enter correct password");
        } else {
            loggedUser.setEmail(email);
            loggedUser.setAddress(address);
            loggedUser.setContactNo(contactNo);
            loggedUser.setImageUrl(imageUrl);

            boolean res = userBo.updateUser(loggedUser);
            if (res) AlertMessage.getInstance().informerAlert(AlertType.SUCCESS, "user detail update successful");
            else AlertMessage.getInstance().informerAlert(AlertType.ERROR, "user detail update fail");
            refreshOnAction();
        }
    }

    // Refresh Button Action Event
    @FXML
    void refreshOnAction() {
        setUserDetails();
        loggedUserDetail();
    }

    // Close Button Action Event
    @FXML
    void closeOnAction() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Confirmation Dialog");
        alert.setContentText("Are you close the program");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        ImageView icon = new ImageView(new Image("images/icons/WARNING.png"));
        alert.setGraphic(icon);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                System.exit(0);
            }
        });
    }

    // Minimize Button Action Event
    @FXML
    void minimizeOnAction() {
        Stage stage = (Stage) txtAddress.getScene().getWindow();
        stage.setIconified(true);
    }


    // ----------------------------------------- change password window -----------------------------------------------

    // change password window Change password button
    @FXML
    void changePasswordOnAction() {
        String currentPassword = txtCurrentPassword.getText();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        String userPassword = new String(Base64.getDecoder().decode(loggedUser.getPassword()));

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            AlertMessage.getInstance().informerAlert(AlertType.WARNING, "Please fill all passwords");
        } else if (!newPassword.equals(confirmPassword)) {
            AlertMessage.getInstance().informerAlert(AlertType.WARNING, "Confirm password is wrong");
        } else if (!currentPassword.equals(userPassword)) {
            AlertMessage.getInstance().informerAlert(AlertType.WARNING, "Current password is wrong");
        } else if (newPassword.length() < 8) {
            AlertMessage.getInstance().informerAlert(AlertType.WARNING, "Please enter more than 8 characters to password");
        } else {
            String password = Base64.getEncoder().encodeToString(newPassword.getBytes());
            loggedUser.setPassword(password);
            boolean res = userBo.updateUser(loggedUser);
            if (res) {
                AlertMessage.getInstance().informerAlert(AlertType.SUCCESS, "Password Change Successful");
                Stage stage = (Stage) txtConfirmPassword.getScene().getWindow();
                stage.close();
            } else AlertMessage.getInstance().informerAlert(AlertType.ERROR, "Password Change Fail");

        }
    }

    @FXML
    void changePasswordRefreshOnAction() {
        txtCurrentPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");
    }

    @FXML
    void changePasswordWindowCloseOnAction() {
        Stage stage = (Stage) txtCurrentPassword.getScene().getWindow();
        stage.close();
    }

    // Minimize Button Action Event
    @FXML
    void changePasswordWindowMinimizeOnAction() {
        Stage stage = (Stage) txtCurrentPassword.getScene().getWindow();
        stage.setIconified(true);
    }


    // ------------------------------- navigation buttons ---------------------------------------

    @FXML
    void changePasswordNavigation() throws IOException {
        Stage stage = new Stage();
        stage.getIcons().add(new Image("images/icons/Logo.png"));
        Scene scene = new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/ChangePassword.fxml"))));
        stage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
    }

    @FXML
    void homePageNavigation() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Home.fxml"));
        Parent root = loader.load();
        HomeController controller = loader.getController();
        controller.setUser(loggedUser);

        Stage stage = (Stage) lblUserName.getScene().getWindow();
        stage.getIcons().add(new Image("images/icons/Logo.png"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        stage.show();
    }

    @FXML
    void placeOrderPageNavigation() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PlaceOrder.fxml"));
        Parent root = loader.load();
        PlaceOrderController controller = loader.getController();
        controller.setUser(loggedUser);

        Stage stage = (Stage) lblUserName.getScene().getWindow();
        stage.getIcons().add(new Image("images/icons/Logo.png"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        stage.show();
    }

    @FXML
    void productPageNavigation() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Product.fxml"));
        Parent root = loader.load();
        ProductController controller = loader.getController();
        controller.setUser(loggedUser);

        Stage stage = (Stage) lblUserName.getScene().getWindow();
        stage.getIcons().add(new Image("images/icons/Logo.png"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        stage.show();
    }

    @FXML
    void orderPageNavigation() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Orders.fxml"));
        Parent root = loader.load();
        OrdersController controller = loader.getController();
        controller.setUser(loggedUser);

        Stage stage = (Stage) lblUserName.getScene().getWindow();
        stage.getIcons().add(new Image("images/icons/Logo.png"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        stage.show();
    }

    @FXML
    void customerPageNavigation() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Customer.fxml"));
        Parent root = loader.load();
        CustomerController controller = loader.getController();
        controller.setUser(loggedUser);

        Stage stage = (Stage) lblUserName.getScene().getWindow();
        stage.getIcons().add(new Image("images/icons/Logo.png"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        stage.show();
    }

    @FXML
    void supplierPageNavigation() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Supplier.fxml"));
        Parent root = loader.load();
        SupplierController controller = loader.getController();
        controller.setUser(loggedUser);

        Stage stage = (Stage) lblUserName.getScene().getWindow();
        stage.getIcons().add(new Image("images/icons/Logo.png"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        stage.show();
    }
}
