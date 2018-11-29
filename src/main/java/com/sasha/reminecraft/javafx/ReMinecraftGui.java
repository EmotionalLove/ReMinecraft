package com.sasha.reminecraft.javafx;

import com.sasha.reminecraft.ReMinecraft;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Created by Sasha at 4:01 PM on 11/28/2018
 */
public class ReMinecraftGui extends Application implements IReMinecraftGui {

    public static final int WIDTH = 450;
    public static final int HEIGHT = 400;

    public

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setHeight(HEIGHT);
        primaryStage.setWidth(WIDTH);
        primaryStage.setOnCloseRequest(close -> {
            if (ReMinecraft.INSTANCE == null) {
                System.exit(0);
                return;
            }
            ReMinecraft.INSTANCE.stop();
        });
        primaryStage.setScene(prepareScreen(primaryStage));
        primaryStage.show();
    }

    private Scene prepareScreen(Stage stage) {
        TabPane pane = new TabPane();
        StackPane chatPane = new StackPane();
        StackPane configPaned = new StackPane();
        pane.getTabs().add(new Tab("Chat", chatPane));
        pane.getTabs().add(new Tab("Configuration", configPaned));
        return new Scene(pane);
    }

    private StackPane prepareChatPane(StackPane pane) {
        Button relaunchButton = new Button("Restart");
        return pane;
    }

}
