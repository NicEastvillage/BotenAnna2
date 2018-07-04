package botenanna.behaviortree.tasks;

import botenanna.behaviortree.*;
import botenanna.game.ActionSet;
import botenanna.game.Car;
import botenanna.game.Situation;
import botenanna.math.RLMath;
import botenanna.math.Vector3;
import botenanna.prediction.Estimates;
import botenanna.prediction.Physics;

import java.util.function.Function;

public class TaskGoTowardsBall extends Leaf {

    private static final double SLIDE_ANGLE = 1.7;

    private double speed;
    private boolean allowSlide = true;

    /** <p>The TaskGoTowardsBall steers the car towards where it estimates the ball will be. A speed parameter describes
     * how fast the car should try to reach the ball. if speed = 1, it will boost straight, if speed = 0.63 it will drive
     * normally. Anything in between is allowed too.
     * By default the agent will slide if the angle to the ball is too high. This can be toggled through arguments
     * so the agent never slides.</p>
     *
     * <p>It's signature is {@code TaskGoTowardsBall <speed:DOUBLE> [allowSlide:BOOLEAN]}</p>*/
    public TaskGoTowardsBall(String[] arguments) throws IllegalArgumentException {
        super(arguments);
        if (arguments.length == 0 || arguments.length > 2) {
            throw new IllegalArgumentException();
        }

        speed = Double.parseDouble(arguments[0]);

        if (arguments.length == 2) {
            allowSlide = Boolean.parseBoolean(arguments[1]);
        }
    }

    @Override
    public void reset() {
        // Irrelevant
    }

    @Override
    public NodeStatus run(Situation input) throws MissingNodeException {

        // Get the needed positions and rotations
        Vector3 myPos = input.getMyCar().getPosition();
        Vector3 myRotation = input.getMyCar().getRotation();
        double velocity = input.getMyCar().getVelocity().getMagnitude();

        double time = Estimates.timeTillCarCanHitBall(myPos, input.getBall(), speed);
        Vector3 point = Physics.stepBall(input.getBall().clone(), time).getPosition();

        double ang = RLMath.carsAngleToPoint(myPos.asVector2(), myRotation.yaw, point.asVector2());

        // Smooth the angle to a steering amount - this avoids wobbling
        double steering = RLMath.steeringSmooth(ang);

        ActionSet output;

        if (velocity < speed * Car.MAX_VELOCITY_BOOST && RLMath.doesCarFacePoint(myPos.asVector2(), myRotation.yaw, point.asVector2()))
            output = new ActionSet().withThrottle(1).withSteer(steering).withBoost();
        else if (velocity > speed * Car.MAX_VELOCITY_BOOST)
            output = new ActionSet().withSteer(steering);
        else
            output = new ActionSet().withThrottle(1).withSteer(steering);

        if (allowSlide) {
            // Do slide for sharp turning
            if (ang > SLIDE_ANGLE || ang < -SLIDE_ANGLE) {
                output.withSlide();
            }
        }

        return new NodeStatus(Status.RUNNING, output, this);
    }
}