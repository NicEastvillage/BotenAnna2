package botenanna.game;

import botenanna.math.Vector3;
import botenanna.physics.Rigidbody;
import rlbot.flat.BallInfo;

/** Ball constants */
public class Ball {

    public static final double RADIUS = 92.2;
    public static final double DIAMETER = RADIUS * 2;
    public static final double SLIDE_DECCELERATION = -230;
    public static final double BALL_GROUND_BOUNCINESS = -0.6;
    public static final double BALL_WALL_BOUNCINESS = -0.6;

    /** Create a ball from the GameData's BallInfo. This way position, velocity,
     * acceleration, rotation and gravity is set immediately. */
    public static Rigidbody get(BallInfo ball) {
        Rigidbody body = new Rigidbody();
        body.setPosition(Vector3.convert(ball.physics().location()));
        body.setVelocity(Vector3.convert(ball.physics().velocity()));
        body.setAcceleration(new Vector3()); // TODO Acceleration is unknown
        body.setRotation(Vector3.convert(ball.physics().rotation()));
        body.setAngularVelocity(Vector3.convert(ball.physics().angularVelocity()));
        return body;
    }
}
