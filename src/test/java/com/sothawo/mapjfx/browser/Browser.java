package com.sothawo.mapjfx.browser;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class Browser extends Application {

    /** the embedded webview. */
    private WebView webView;

    /** the engine of the embbeded webview. */
    private WebEngine webEngine;

    /** tesxt field for entry of url. */
    private TextField urlTextField;

    // button to start loading the webpage. */
    private Button goButton;

    @Override
    public void start(Stage primaryStage) throws Exception {

        webView = new WebView();
        webEngine = webView.getEngine();

        primaryStage.setTitle("Browser");

        // create content
        BorderPane content = new BorderPane();

        // at the top url entry filed and go button
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(5, 5, 5, 5));
        hbox.setSpacing(5);

        urlTextField = new TextField();
        HBox.setHgrow(urlTextField, Priority.ALWAYS);
        hbox.getChildren().add(urlTextField);

        goButton = new Button("Go");
        goButton.setOnAction(evt -> {
            webEngine.load(urlTextField.getText());
        });

        hbox.getChildren().add(goButton);

        content.setTop(hbox);

        content.setCenter(webView);

        primaryStage.setScene(new Scene(content, 1024, 800));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
