package botenanna.behaviortree.guards;

import botenanna.game.Ball;
import botenanna.behaviortree.ArgumentTranslator;
import botenanna.behaviortree.Leaf;
import botenanna.behaviortree.MissingNodeException;
import botenanna.behaviortree.NodeStatus;
import botenanna.game.Situation;
import botenanna.math.Vector3;
import botenanna.math.Zone;
import botenanna.prediction.Physics;

import java.util.function.Function;

public class GuardWillBallHitGoal extends Leaf {

    private Function<Situation, Object> areaFunc;

    /** The guard GuardWillBallHitGoal checks if the path of the ball ends in the goal box using a simulation
     * of where the ball is when it reaches the wall. The guard returns SUCCESS when the ball has a path that
     * ends in the given box area. (Goal box) and returns FAILURE if it will not.
     *
     * Its signature is: {@code GuardWillBallHitGoal <boxArea:Zone>}*/
    public GuardWillBallHitGoal(String[] arguments) throws IllegalArgumentException {
        super(arguments);

        if (arguments.length != 1) throw new IllegalArgumentException();

        areaFunc = ArgumentTranslator.get(arguments[0]);
    }

    @Override
    public void reset() {
        // Irrelevant
    }

    @Override
    public NodeStatus run(Situation situation) throws MissingNodeException {

        // Determine time it will take for ball to hit next Y-positive wall
        double time = Physics.predictArrivalAtWallYPositive(situation.getBall(), Ball.RADIUS);

        // Find position when hitting wall
        Vector3 destination = Physics.stepBall(situation.getBall(), time).getPosition();

        // Determine area
        Zone zone = (Zone) areaFunc.apply(situation);

        if (zone.contains(destination)) {
            return NodeStatus.DEFAULT_SUCCESS;
        } else {
            return NodeStatus.DEFAULT_FAILURE;
        }
    }
}
