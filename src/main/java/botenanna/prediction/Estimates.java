package botenanna.prediction;

import botenanna.game.Car;
import botenanna.math.Vector3;

public class Estimates {

    /** Returns an estimate of how long time it will take for a car to reach a ball. A speed parameter describes how
     * much effort the car expects to put into getting there. If speed = 1, the car is expected to boost straight to
     * the ball. If speed = 0.62, the car is expected to drive straight but without boost.
     * @param speed a double between 0.1 and 1 that describes how much effort is expected to be used to reach ball. */
    public static double timeTillCarCanHitBall(Vector3 carPosition, Rigidbody ball, double speed) {
        if (speed < 0.05) throw new IllegalArgumentException("Speed is too small");

        Rigidbody ballClone = ball.clone();
        double time = 0;
        double stepSize = 2;
        double carReach;
        double dist2;

        while (stepSize > 1/32d) {
            do {
                time += stepSize;
                ballClone.set(ball);
                Physics.stepBall(ballClone, time);
                carReach = time * speed * Car.MAX_VELOCITY_BOOST;

                dist2 = carPosition.getDistanceToSqr(ballClone.getPosition().scale(1, 1, 4));

            } while (dist2 < carReach * carReach);
            // latest step was too far
            time -= stepSize;

            stepSize /= 2;
        }

        return time;
    }
}
