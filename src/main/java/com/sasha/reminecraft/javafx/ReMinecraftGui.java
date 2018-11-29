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

    public static final int WIDTH = 600;
    public static final int HEIGHT = 400;

    public void startLaunch() {
        launch();
    }

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
        stage.setTitle("RE:Minecraft " + ReMinecraft.VERSION);
        TabPane pane = new TabPane();
        StackPane chatPane = new StackPane();
        StackPane configPaned = new StackPane();
        Tab chatTab = new Tab("Chat", chatPane);
        chatTab.setClosable(false);
        Tab configTab = new Tab("Configuration", configPaned);
        configTab.setClosable(false);
        pane.getTabs().add(chatTab);
        pane.getTabs().add(configTab);
        return new Scene(pane);
    }

    private StackPane prepareChatPane(StackPane pane) {
        Button relaunchButton = new Button("Restart");
        return pane;
    }

}
