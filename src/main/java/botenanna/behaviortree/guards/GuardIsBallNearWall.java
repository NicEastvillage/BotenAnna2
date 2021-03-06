package botenanna.behaviortree.guards;

import botenanna.behaviortree.Leaf;
import botenanna.behaviortree.MissingNodeException;
import botenanna.behaviortree.NodeStatus;
import botenanna.game.Situation;
import botenanna.math.Vector3;
import botenanna.math.Zone;

public class GuardIsBallNearWall extends Leaf {

    /** checks if the ball is close to a wall by taking the x and y value and comparing it to the
     *  coordinates of the ball and returns true if the ball is within it field.
     *
     *  Its signature is {@code GuardIntercept} */
    public GuardIsBallNearWall(String[] arguments) throws IllegalArgumentException {
        super(arguments);

        // Takes no arguments
        if (arguments.length != 0) throw new IllegalArgumentException();
    }

    @Override
    public void reset() {
        // Irrelevant
    }

    @Override
    public NodeStatus run(Situation situation) throws MissingNodeException {

        Zone fieldZone = new Zone(new Vector3(-4080, -5080, 4060), new Vector3(4080, 5080, 0));

        if (fieldZone.contains(situation.getBall().getPosition())) {
            return NodeStatus.DEFAULT_SUCCESS;
        }
        return NodeStatus.DEFAULT_FAILURE;
    }
}
