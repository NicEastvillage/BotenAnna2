package botenanna;

import botenanna.behaviortree.builder.BehaviourTreeBuilder;
import botenanna.display.BallInfoDisplay;
import botenanna.display.BotInfoDisplay;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import rlbot.manager.BotManager;
import rlbot.pyinterop.PythonInterface;
import rlbot.pyinterop.PythonServer;
import rlbot.PortReader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class BotenAnnaWindow extends Application {

    public static BehaviourTreeBuilder defaultBTBuilder;
    public static ArrayBlockingQueue<BotenAnnaBot> updateQueue = new ArrayBlockingQueue<>(5);

    private Pane root;
    private Pane botInfoDisplayRoot;
    private PythonServer pythonServer;
    private Map<BotenAnnaBot, BotInfoDisplay> botInfoDisplays;

    @Override
    public void start(Stage stage) throws Exception {

        // Technical stuff
        createDefaultBehaviourTreeBuilder(stage);
        startPythonServer();

        root = new VBox();

        // Info displays
        botInfoDisplayRoot = new VBox();
        root.getChildren().add(botInfoDisplayRoot);
        botInfoDisplays = new HashMap<>();

        Scene scene = new Scene(root, 350, 160);
        stage.setScene(scene);
        stage.setTitle("Boten Anna - Data Window");
        stage.setAlwaysOnTop(true);
        stage.show();
    }

    private void startPythonServer() throws Exception {
        Integer port = PortReader.readPortFromFile("port.cfg");
        PythonInterface pythonInterface = new BotenAnnaPythonInterface(new BotManager());
        pythonServer = new PythonServer(pythonInterface, port);
        pythonServer.start();

        // Setup timer that acts each frame and checks bot updates placed in updateQueue
        final LongProperty lastUpdate = new SimpleLongProperty();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate.get() > 0) {
                    final BotenAnnaBot bot = updateQueue.poll();
                    if (bot != null) {
                        updateBotInfoDisplay(bot);
                    }
                    lastUpdate.set(now);
                }
            }
        };
        timer.start();
    }

    private void createDefaultBehaviourTreeBuilder(Stage stage) {
        defaultBTBuilder = new BehaviourTreeBuilder(stage);
        defaultBTBuilder.setupDefaultFile();
        try {
            // Build a behaviour tree to make sure file is valid. The tree is immediately discarded
            defaultBTBuilder.buildUsingDefault();
        } catch (Exception e) {
            System.out.println("Error when opening behaviour tree source file: " + e.getMessage());
            System.exit(-1);
        }
    }

    public void updateBotInfoDisplay(BotenAnnaBot bot) {
        // Create new display if it is a new bot
        if (!botInfoDisplays.containsKey(bot)) {
            BotInfoDisplay display = new BotInfoDisplay(bot);
            botInfoDisplayRoot.getChildren().add(display);
            botInfoDisplays.put(bot, display);
        }
        botInfoDisplays.get(bot).update();
    }
}
