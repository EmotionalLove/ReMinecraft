package com.sasha.reminecraft.javafx;

import com.sasha.reminecraft.ReMinecraft;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 * Created by Sasha at 4:01 PM on 11/28/2018
 */
public class ReMinecraftGui extends Application implements IReMinecraftGui {

    public static final int WIDTH = 600;
    public static final int HEIGHT = 400;

    public static TextArea areaToLogTo = null;

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
        ScrollPane configPane = new ScrollPane();
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
        areaToLogTo = new TextArea();
        areaToLogTo.setEditable(false);
        areaToLogTo.setMaxSize(WIDTH - 75, HEIGHT - 160);
        pane.getChildren().addAll(relaunchButton, stopButton, areaToLogTo);
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            relaunchButton.setTranslateX(-newVal.intValue() / 2 + 40);
            stopButton.setTranslateX(-newVal.intValue() / 2 + 95);
            areaToLogTo.setMaxWidth((double)newVal - 75);
        });
        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            relaunchButton.setTranslateY(-newVal.intValue() / 2 + 55);
            stopButton.setTranslateY(-newVal.intValue() / 2 + 55);
            areaToLogTo.setMaxHeight((double)newVal - 160);
    });

        return pane;
    }

}
