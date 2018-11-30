package com.sasha.reminecraft.javafx;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.sasha.reminecraft.Configuration;
import com.sasha.reminecraft.ReMinecraft;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.util.stream.Collectors;

/**
 * Created by Sasha at 4:01 PM on 11/28/2018
 */
public class ReMinecraftGui extends Application implements IReMinecraftGui {

    public static final int WIDTH = 600;
    public static final int HEIGHT = 400;

    private static Image img;
    private static Image img1;

    public static TextArea areaToLogTo = null;
    private static ChoiceBox<String> dropdown = null;

    public static boolean launched = false;

    public void startLaunch() {
        launched = true;
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        img = new Image(ReMinecraftGui.class.getClassLoader().getResource("background.png").toString());
        img1 = new Image(ReMinecraftGui.class.getClassLoader().getResource("background2.png").toString());
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
        Tab chatTab = new Tab("Chat", prepareChatPane(stage));
        chatTab.setClosable(false);
        Tab configTab = new Tab("Configuration", prepareConfigPane(stage));
        configTab.setClosable(false);
        pane.getTabs().add(chatTab);
        pane.getTabs().add(configTab);
        return new Scene(pane);
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private StackPane prepareChatPane(Stage stage) {
        StackPane pane = new StackPane();
        applyBackgroundImage(pane, img, WIDTH, HEIGHT);
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

        TextField field = new TextField();
        field.setPromptText("Send a chat message or type a command...");
        field.setOnKeyReleased(key -> {
            if (key.getCode() == KeyCode.ENTER) {
                if (field.getText().length() > 256) {
                    return;
                }
                ReMinecraft.INSTANCE.minecraftClient.getSession().send(new ClientChatPacket(field.getText()));
                field.setText("");
            }
        });

        pane.getChildren().addAll(relaunchButton, stopButton, areaToLogTo, field);
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            relaunchButton.setTranslateX(-newVal.intValue() / 2 + 40);
            stopButton.setTranslateX(-newVal.intValue() / 2 + 95);
            areaToLogTo.setMaxWidth((double) newVal - 75);

        });
        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            relaunchButton.setTranslateY(-newVal.intValue() / 2 + 55);
            stopButton.setTranslateY(-newVal.intValue() / 2 + 55);
            areaToLogTo.setMaxHeight((double) newVal - 160);
        });
        return pane;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private StackPane prepareConfigPane(Stage stage) {
        StackPane pane = new StackPane();
        applyBackgroundImage(pane, img1, WIDTH, HEIGHT);
        Button saveButton = new Button("Save");
        saveButton.setTranslateY(-HEIGHT / 2 + 55);
        saveButton.setTranslateX(-WIDTH / 2 + 40);
        saveButton.setOnAction(e -> {
            // todo
        });
        Button discardButton = new Button("Discard");
        discardButton.setTranslateX(-WIDTH / 2 + 95);
        discardButton.setTranslateY(-HEIGHT / 2 + 55);
        discardButton.setOnAction(e -> {
            // todo
        });

        dropdown = new ChoiceBox<>();
        dropdown.setAccessibleText("Ok");
        dropdown.getItems().add("Populating configurations...");
        dropdown.setTranslateY(-HEIGHT / 2 + 55);
        dropdown.setTranslateX((WIDTH - WIDTH / 2) - 30);
        dropdown.setOnAction(e -> {
            dropdown.setTranslateX((WIDTH - WIDTH / 2) - dropdown.getWidth());
        });

        pane.getChildren().addAll(saveButton, discardButton, dropdown);
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setTranslateX(-newVal.intValue() / 2 + 40);
            discardButton.setTranslateX(-newVal.intValue() / 2 + 95);
            dropdown.setTranslateX((newVal.intValue() - newVal.intValue() / 2) - dropdown.getWidth());
        });
        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setTranslateY(-newVal.intValue() / 2 + 55);
            discardButton.setTranslateY(-newVal.intValue() / 2 + 55);
            dropdown.setTranslateY(-newVal.intValue() / 2 + 55);
        });
        return pane;
    }

    private void populateConfigPane(StackPane pane, Configuration selectedConfiguration) {
        for (Field declaredField : selectedConfiguration.getClass().getDeclaredFields()) {
            if (!declaredField.getName().startsWith("var_") || declaredField.getAnnotation(Configuration.ConfigSetting.class) == null) continue;
            if (declaredField.getType() == boolean.class) {
                // put check mark
            }
            if (declaredField.getType() == double.class) {
                // put txt field with note that says decimal
            }
            if (declaredField.getType() == float.class) {
                // put txt field with note that says decimal
            }
            if (declaredField.getType() == int.class) {
                // put txt field with note that says whole number
            }
            if (declaredField.getType() == long.class) {
                // put txt field with note that says whole number
            }
            if (declaredField.getType() == String.class) {
                if (declaredField.getName().toLowerCase().contains("password")) {
                    // put a password field
                }
                else {
                    // put a normal text field
                }
            }
        }
    }

    public static void refreshConfigurationEntries() {
        dropdown.getItems().clear();
        dropdown.getItems()
                .addAll(ReMinecraft.INSTANCE.configurations
                        .stream()
                        .map(Configuration::getConfigName)
                        .collect(Collectors.toList()));
    }

    private void applyBackgroundImage(Region node, Image img, double width, double height) {
        BackgroundImage myBI = new BackgroundImage
                (img,
                        BackgroundRepeat.SPACE,
                        BackgroundRepeat.SPACE,
                        BackgroundPosition.CENTER,
                        BackgroundSize.DEFAULT);
        node.setBackground(new Background(myBI));
    }

    private void applyBackgroundImage(Region node, Image img) {
        BackgroundImage myBI = new BackgroundImage
                (img,
                        BackgroundRepeat.SPACE,
                        BackgroundRepeat.SPACE,
                        BackgroundPosition.CENTER,
                        BackgroundSize.DEFAULT);
        node.setBackground(new Background(myBI));
    }

}
