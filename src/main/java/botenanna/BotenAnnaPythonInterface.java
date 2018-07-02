package botenanna;

import rlbot.Bot;
import rlbot.SampleBot;
import rlbot.manager.BotManager;
import rlbot.pyinterop.DefaultPythonInterface;

import java.io.IOException;

public class BotenAnnaPythonInterface extends DefaultPythonInterface {

    public BotenAnnaPythonInterface(BotManager botManager) {
        super(botManager);
    }

    protected Bot initBot(int index, String botType, int team) {
        try {
            return new BotenAnnaBot(index, team, BotenAnna.defaultBTBuilder.buildUsingDefault());
        } catch (IOException e) {
            e.printStackTrace();
            return new SampleBot(index);
        }
    }
}
