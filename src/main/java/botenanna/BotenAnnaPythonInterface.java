package botenanna;

import rlbot.Bot;
import rlbot.manager.BotManager;
import rlbot.pyinterop.DefaultPythonInterface;

import java.io.IOException;

public class BotenAnnaPythonInterface extends DefaultPythonInterface {

    public BotenAnnaPythonInterface(BotManager botManager) {
        super(botManager);
    }

    protected Bot initBot(int index, String botType, int team) {
        return new BotenAnnaBot(index, team);
    }
}