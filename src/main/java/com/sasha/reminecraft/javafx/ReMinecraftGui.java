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

    public static boolean launched = false;

    public void startLaunch() {
        launched = true;
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
        StackPane configPane = new StackPane();
        Tab chatTab = new Tab("Chat", prepareChatPane(stage));
        chatTab.setClosable(false);
        Tab configTab = new Tab("Configuration", configPane);
        configTab.setClosable(false);
        pane.getTabs().add(chatTab);
        pane.getTabs().add(configTab);
        return new Scene(pane);
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private StackPane prepareChatPane(Stage stage) {
        StackPane pane = new StackPane();
        Button relaunchButton = new Button("Restart");
        relaunchButton.setTranslateY(-HEIGHT / 2 + 55);
        relaunchButton.setTranslateX(-WIDTH / 2 + 40);
        relaunchButton.setOnAction(e -> {
            ReMinecraft.INSTANCE.reLaunch();
        });
        Button stopButton = new Button("Stop");
        stopButton.setTranslateX(-WIDTH / 2 + 95);
        stopButton.setTranslateY(-HEIGHT / 2 + 55);
        stopButton.setOnAction(e -> {
            ReMinecraft.INSTANCE.stop();
        });
        pane.getChildren().addAll(relaunchButton, stopButton);
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            relaunchButton.setTranslateX(-newVal.intValue() / 2 + 40);
            stopButton.setTranslateX(-newVal.intValue() / 2 + 95);
        });
        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            relaunchButton.setTranslateY(-newVal.intValue() / 2 + 55);
            stopButton.setTranslateY(-newVal.intValue() / 2 + 55);
        });

        return pane;
    }

}
