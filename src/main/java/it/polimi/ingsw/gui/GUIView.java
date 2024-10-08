package it.polimi.ingsw.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/** Main class for the GUI*/
public class GUIView extends Application {

    /**
     * Controller of the login scene
     */
    private LoginController loginController;

    /**
     * Controller of the game scene
     */
    private PlayController playController;

    /**
     * Primary window of the graphical application
     */
    private Stage stage;

    /**
     * Image of the logo used as icon for the graphical application
     */
    private final Image logo;

    /**
     * Constructor of the gui main class, it's need to keep a quick reference to the instance
     */
    public GUIView() {
        logo = new Image(Objects.requireNonNull(GUIView.class.getResourceAsStream("img/misc/logo.png")));
    }

    /**
     * Main entry point for the JavaFX application, this method is called right after init()
     * It loads the scene, sets window title, sets window icon, sets size, sets closing options
     * @param stage top level container for the Graphical part of the whole application
     * @throws IOException if Login-view.fxml is not found
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GUIView.class.getResource("Login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        loginController = fxmlLoader.getController();
        stage.setTitle("Codex Naturalis");
        stage.getIcons().add(logo);
        stage.setScene(scene);
        stage.setMinHeight(400.0);
        stage.setMinWidth(600.0);
        stage.setResizable(false);
        stage.show();
        this.stage = stage;
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Getter to retrieve the instance of the login controller
     * @return the javafx controller of the login scene
     */
    public LoginController getLoginController() {
       return loginController;
    }

    /**
     * Getter to retrieve the instance of the play controller
     * @return the javafx controller of the game scene
     */
    public PlayController getPlayController() {
        return playController;
    }

    /**
     * This method switch the scene from the login to the actual game
     * @param playerNickname name of the player to display in the window title
     */
    public void playScene(String playerNickname) {
        FXMLLoader fxmlLoader = new FXMLLoader(GUIView.class.getResource("Play-view.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("CodexNaturalis | " + playerNickname);
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        playController = fxmlLoader.getController();
        stage.setMaximized(true);
    }

    /**
     * Main methods of the java fx application, needed for the application
     * @param args String arguments not used in this case
     */
    public static void main(String[] args) {
        launch();
    }


}
