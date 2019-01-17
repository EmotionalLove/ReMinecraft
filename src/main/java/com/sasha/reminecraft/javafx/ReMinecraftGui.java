package com.sasha.reminecraft.javafx;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.sasha.reminecraft.Configuration;
import com.sasha.reminecraft.ReMinecraft;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Sasha at 4:01 PM on 11/28/2018
 */
public class ReMinecraftGui extends Application {

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
        Tab configTab = new Tab("About", prepareConfigPane(stage));
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
        areaToLogTo.setWrapText(true);
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
        field.setMaxWidth(WIDTH - 100);
        field.setTranslateY(HEIGHT - (HEIGHT / 2) - 50);

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
            field.setTranslateY(newVal.intValue() - (newVal.intValue() / 2) - 50);
        });
        return pane;
    }

    private StackPane prepareConfigPane(Stage stage) {
        StackPane background = new StackPane();
        TilePane pane = new TilePane();
        pane.setPadding(new Insets(5, 0, 5, 0));
        pane.setPrefRows(2);
        pane.setPrefColumns(3);
        pane.setMaxWidth(150);
        pane.setAlignment(Pos.CENTER);
        applyBackgroundImage(background, img1, WIDTH, HEIGHT);
        return background;
    }

    private void populateConfigPane(Stage stage, TilePane pane, Configuration selectedConfiguration) throws IllegalAccessException {
        pane.getChildren().removeIf(n -> (!(n instanceof Button)));
        int translateValue = (int) (-stage.getHeight() / 2 + 25);
        List<Node> elements = new ArrayList<>();
        for (Field declaredField : selectedConfiguration.getClass().getDeclaredFields()) {
            if (!declaredField.getName().startsWith("var_") || declaredField.getAnnotation(Configuration.ConfigSetting.class) == null)
                continue;
            Label label = new Label(declaredField.getName().replace("var_", ""));
            //label.setTranslateY(translateValue - 40);
            //label.setTranslateX(-stage.getWidth() / 2 - 40);
            if (!elements.contains(label)) elements.add(label);
            if (declaredField.getType() == boolean.class) {
                // put check mark
                CheckBox bool = new CheckBox(declaredField.getName().replace("var_", ""));
                //bool.setTranslateY(translateValue);
                bool.setSelected((boolean) declaredField.get(selectedConfiguration));
                bool.setId(declaredField.getName());
                if (!elements.contains(bool)) elements.add(bool);
                translateValue += 40;
            } else if (declaredField.getType() == double.class) {
                // put txt field with note that says decimal
                TextField field = new TextField();
                field.setMaxWidth(250);
                field.setText(declaredField.get(selectedConfiguration) + "");
                field.setId(declaredField.getName());
                //field.setTranslateY(translateValue);
                if (!elements.contains(field)) if (!elements.contains(field)) elements.add(field);
                translateValue += 40;
            } else if (declaredField.getType() == float.class) {
                // put txt field with note that says decimal
                TextField field = new TextField();
                field.setMaxWidth(250);
                field.setText(declaredField.get(selectedConfiguration) + "");
                field.setId(declaredField.getName());
                //field.setTranslateY(translateValue);
                if (!elements.contains(field)) elements.add(field);
                translateValue += 40;
            } else if (declaredField.getType() == int.class) {
                // put txt field with note that says whole number
                TextField field = new TextField();
                field.setMaxWidth(250);
                field.setText(declaredField.get(selectedConfiguration) + "");
                field.setId(declaredField.getName());
                //field.setTranslateY(translateValue);
                if (!elements.contains(field)) elements.add(field);
                translateValue += 40;
            } else if (declaredField.getType() == long.class) {
                // put txt field with note that says whole number
                TextField field = new TextField();
                field.setMaxWidth(250);
                field.setText(declaredField.get(selectedConfiguration) + "");
                field.setId(declaredField.getName());
                //field.setTranslateY(translateValue);
                if (!elements.contains(field)) elements.add(field);
                translateValue += 40;
            } else if (declaredField.getType() == String.class) {
                if (declaredField.getName().toLowerCase().contains("password")) {
                    PasswordField field = new PasswordField();
                    field.setMaxWidth(250);
                    field.setText((String) declaredField.get(selectedConfiguration));
                    field.setId(declaredField.getName());
                    //field.setTranslateY(translateValue);
                    if (!elements.contains(field)) elements.add(field);
                    translateValue += 40;
                } else {
                    // put a normal text field
                    TextField field = new TextField();
                    field.setMaxWidth(250);
                    field.setText((String) declaredField.get(selectedConfiguration));
                    field.setId(declaredField.getName());
                    //field.setTranslateY(translateValue);
                    if (!elements.contains(field)) elements.add(field);
                    translateValue += 40;
                }
            }
        }
        stage.resizableProperty().addListener((obs, oldVal, newVal) -> {
            try {
                populateConfigPane(stage, pane, selectedConfiguration);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        for (Node element : elements) {
            pane.getChildren().add(element);
        }
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

    private static Configuration getConfigFromName(String name) {
        for (Configuration configuration : ReMinecraft.INSTANCE.configurations) {
            if (configuration.getConfigName().equalsIgnoreCase(name)) {
                return configuration;
            }
        }
        return null;
    }

    private void setConfigVar(Field field, Object instance, Object value) throws IllegalAccessException {
        if (field.getType() == boolean.class || field.getType() == String.class) {
            field.set(instance, value);
        }
        if (field.getType() == double.class) {
            field.set(instance, Double.parseDouble((String) value));
        }
        if (field.getType() == float.class) {
            field.set(instance, Float.parseFloat((String) value));
        }
        if (field.getType() == int.class) {
            field.set(instance, Integer.parseInt((String) value));
        }
        if (field.getType() == long.class) {
            field.set(instance, Long.parseLong((String) value));
        }
    }

}
