package botenanna;

import botenanna.behaviortree.BehaviorTree;
import botenanna.game.ActionSet;
import botenanna.game.Situation;
import rlbot.Bot;
import rlbot.ControllerState;
import rlbot.flat.GameTickPacket;
import rlbot.output.ControlsOutput;

public class BotenAnnaBot implements Bot {

    public enum Team {
        BLUE, ORANGE
    }

    private final Team team;
    private final int playerIndex;
    private BehaviorTree behaviorTree;
    private Situation lastInputReceived;

    /** A Rocket League agent. */
    public BotenAnnaBot(int playerIndex, int teamIndex, BehaviorTree tree) {
        this.playerIndex = playerIndex;
        team = (teamIndex == 0 ? Team.BLUE : Team.ORANGE);
        behaviorTree = tree;
    }

    /** Let the bot process the information from the input packet
     * @param packet the game tick packet from the game
     * @return an ActionSet of what the agent want to do
     */
    public ActionSet process(Situation packet) {
        if (behaviorTree == null) throw new RuntimeException("Behaviour Tree is null for bot #" + playerIndex);
        return behaviorTree.evaluate(packet);
    }

    public int getIndex() {
        return playerIndex;
    }

    public Team getTeam() {
        return team;
    }

    public BehaviorTree getBehaviorTree() {
        return behaviorTree;
    }

    public void setBehaviorTree(BehaviorTree tree) {
        behaviorTree = tree;
    }

    public Situation getLastInputReceived() {
        return lastInputReceived;
    }

    public void setLastInputReceived(Situation lastInputReceived) {
        this.lastInputReceived = lastInputReceived;
    }

    @Override
    public ControllerState processInput(GameTickPacket request) {
        if (request.playersLength() <= playerIndex || request.ball() == null) {
            return new ControlsOutput();
        }
        Situation situation = new Situation(request, playerIndex);
        return process(situation).toControllerState();
    }

    @Override
    public void retire() {
        System.out.println("Retiring BotenAnna bot #" + playerIndex);
    }
}