package botenanna.behaviortree.guards;

import botenanna.behaviortree.Leaf;
import botenanna.behaviortree.MissingNodeException;
import botenanna.behaviortree.NodeStatus;
import botenanna.game.Arena;
import botenanna.game.Situation;
import botenanna.math.RLMath;
import botenanna.math.Vector2;
import botenanna.math.Vector3;

public class GuardIsInAimingCone extends Leaf {

    /** The GuardIsInAimingCone checks if the car is position, so that the ball is in between the car and the
     * enemy's goal.
     *
     * <p> It's signature is: {@code GuardIsInAimingCone}</p> */
    public GuardIsInAimingCone(String[] arguments) throws IllegalArgumentException {
        super(arguments);

        // Takes no arguments
        if (arguments.length != 0) throw new IllegalArgumentException();
    }

    @Override
    public void reset() {
        // Irrelevant
    }

    @Override
    public NodeStatus run(Situation input) throws MissingNodeException {

        Vector2 ballPos = input.getBall().getPosition().asVector2();
        Vector2 carPos = input.getMyCar().getPosition().asVector2();
        Vector2[] posts = Arena.getGoalPosts(input.enemyPlayerIndex);

        Vector2 postAToBall = ballPos.plus(ballPos.minus(posts[0]));
        Vector2 postBToBall = ballPos.plus(ballPos.minus(posts[1]));

        boolean in = RLMath.isPointInUnboundTriangle(carPos, ballPos, postAToBall, postBToBall);
        return in ? NodeStatus.DEFAULT_SUCCESS : NodeStatus.DEFAULT_FAILURE;
    }
}
