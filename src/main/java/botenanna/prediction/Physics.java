package botenanna.prediction;

import botenanna.game.Arena;
import botenanna.game.Ball;
import botenanna.math.Vector3;

public class Physics {

    public static final Vector3 GRAVITY = Vector3.DOWN.scale(650);

    /** Move a Ball. This includes bounces of walls and on the floor. This WILL change the balls data.
     * @param time must be zero or positive. */
    public static Rigidbody stepBall(Rigidbody ball, double time) {
        if (time < 0) throw new IllegalArgumentException("Time must be zero or positive.");
        if (time == 0) return ball;

        double timeSpent = 0;
        double timeLeft = time;
        double nextWallHit;
        double nextGroundHit;

        do {

            nextWallHit = predictArrivalAtAnyWall(ball, Ball.RADIUS);
            nextGroundHit = predictArrivalAtHeight(ball, Ball.RADIUS, true);

            // Check if ball doesn't hits anything
            if (timeLeft < nextGroundHit && timeLeft < nextWallHit) {
                stepBody(ball, timeLeft, true);
                return ball;
            } else if (nextWallHit < nextGroundHit) {
                // Simulate until ball it hits wall
                stepBody(ball, nextWallHit, true);
                timeSpent += nextWallHit;
                Vector3 vel = ball.getVelocity();
                if (willHitSideWallNext(ball, Ball.RADIUS)) {
                    ball.setVelocity(new Vector3(vel.x * Ball.BALL_WALL_BOUNCINESS, vel.y, vel.z));
                } else {
                    ball.setVelocity(new Vector3(vel.x, vel.y * Ball.BALL_WALL_BOUNCINESS, vel.z));
                }
            } else if (nextGroundHit == 0) {
                // Simulate ball rolling until it hits wall
                ball.setVelocity(ball.getVelocity().withZ(0));

                if (Double.isNaN(nextWallHit)) {
                    // The ball is laying still
                    break;
                }

                stepBody(ball, Math.min(nextWallHit, timeLeft), false);
                timeSpent += nextWallHit;

                Vector3 vel = ball.getVelocity();
                if (willHitSideWallNext(ball, Ball.RADIUS)) {
                    ball.setVelocity(new Vector3(vel.x * Ball.BALL_WALL_BOUNCINESS, vel.y, vel.z));
                } else {
                    ball.setVelocity(new Vector3(vel.x, vel.y * Ball.BALL_WALL_BOUNCINESS, vel.z));
                }
            } else {
                // Simulate until ball it hits ground
                stepBody(ball, nextGroundHit, true);
                timeSpent += nextGroundHit;
                Vector3 vel = ball.getVelocity();
                ball.setVelocity(new Vector3(vel.x, vel.y, vel.z * Ball.BALL_GROUND_BOUNCINESS));
            }

            timeLeft = time - timeSpent;
        } while (timeSpent <= time);

        return ball;
    }

    /** Get the path describing how a Ball will travel.
     * @param ball the ball to be simulated
     * @param duration must be zero or positive.
     * @param stepsize must be positive. A smaller step size will increase the accuracy of the Path. */
    public static Path getBallPath(Rigidbody ball, double duration, double stepsize) {
        if (duration < 0) throw new IllegalArgumentException("Duration must be zero or positive.");
        if (stepsize <= 0) throw new IllegalArgumentException("Step size must be positive.");

        Path path = new Path();
        Rigidbody clone = ball.clone();

        for (double time = 0; time <= duration; time += stepsize) {
            // Resetting the ball each iteration results in more calculations when the ball bounces a lot, but
            // it will be more precise in general, since there will be less rounding errors.
            clone.set(ball);
            Vector3 pos = stepBall(clone, time).getPosition();
            path.addTimeStep(time, pos);
        }

        return path;
    }

    /** Move a Rigidbody {@code time} seconds into the future.
     * @param body the Rigidbody to be simulated
     * @param time time passed in seconds
     * @return the {@code body} simulated {@code time} seconds. */
    public static <T extends Rigidbody> T stepBody(T body, double time, boolean affectedByGravity) {
        Vector3 actualAcceleration = affectedByGravity ? body.getAcceleration().plus(GRAVITY) : body.getAcceleration();

        // new_position = p + (1/2 * a * t^2) + (v * t)
        Vector3 newPos = body.getPosition().plus(actualAcceleration.scale(0.5 * time * time)).plus(body.getVelocity().scale(time));
        Vector3 newVel = body.getVelocity().plus(actualAcceleration.scale(time));
        body.setPosition(newPos);
        body.setVelocity(newVel);

        return body;
    }

    /** Get the path which a RigidBody will travel.
     * @param body the body to be simulated
     * @param duration must be zero or positive.
     * @param stepsize must be positive. A smaller step size will increase the accuracy of the Path. */
    public static Path getBodyPath(Rigidbody body, double duration, double stepsize, boolean affectedByGravity) {
        if (duration < 0) throw new IllegalArgumentException("Duration must be zero or positive.");
        if (stepsize <= 0) throw new IllegalArgumentException("Step size must be positive.");

        Path path = new Path();
        Rigidbody clone = body.clone();

        for (double time = 0; time <= duration; time += stepsize) {
            clone.set(body);
            Vector3 pos = stepBody(clone, time, affectedByGravity).getPosition();
            path.addTimeStep(time, pos);
        }

        return path;
    }

    /** <p>Calculate when a rigidbody will be at a specific {@code height} with its current position, velocity and acceleration.
     * Will return NaN if {@code height} is never reached. This function only cares about movement
     * along the z-axis. Acceleration cannot be positive. Gravity will be included, if this is affected by gravity.</p>
     *
     * <p>List of possible cases (t refers to return value):
     * <ul>
     *     <li>If already at {@code height}, then t = 0.</li>
     *     <li>If acceleration is 0:</li>
     *     <ul>
     *         <li>If velocity is 0, then t = NaN.</li>
     *         <li>If velocity is away from {@code height}, then t = NaN.</li>
     *         <li>If velocity is towards {@code height}, then t > 0.</li>
     *     </ul>
     *     <li>If acceleration is negative:</li>
     *     <ul>
     *         <li>If {@code height} is below current z-coordinate, then t > 0.</li>
     *         <li>If the rigidbody's turning point lower than {@code height}, then t = NaN.</li>
     *         <li>If the rigidbody's turning point above {@code height}, then t > 0.</li>
     *     </ul>
     * </ul>
     * </p>
     *
     * @param height the height.
     * @return the expected time till the rigidbody will be at height in seconds, always positive, or NaN if {@code height} is never reached. */
    public static double predictArrivalAtHeight(Rigidbody body, double height, boolean affectedByGravity) {

        // If already at height, return 0
        if (height == body.getPosition().z) return 0;

        // Check if there is acceleration involved
        Vector3 actualAcceleration = affectedByGravity ? body.getAcceleration().plus(GRAVITY) : body.getAcceleration();
        if (actualAcceleration.z == 0) {
            // Only velocity is relevant
            return predictArrivalAtHeightLinear(body, height);

        } else {
            // Acceleration must be taken into account
            return predictArrivalAtHeightQuadratic(body, height, actualAcceleration.z);
        }
    }

    /** Helper function for {@link #predictArrivalAtHeight(Rigidbody, double, boolean)} for when acceleration is relevant.
     * @return the expected time till arrival, or NaN if the rigidbody will never arrive at {@code height} */
    private static double predictArrivalAtHeightQuadratic(Rigidbody body, double height, double actualZAcceleration) {

        Vector3 position = body.getPosition();
        Vector3 velocity = body.getVelocity();

        // Check if height is above current z, because then the rigidbody may never get there
        if (height > position.z) {

            // Elapsed time when arriving at the turning point
            double turningTime = -velocity.z / actualZAcceleration;

            // This is in the past?? -> acceleration must have been negative
            if (turningTime < 0) return Double.NaN;

            // Height at turning point
            double turningHeight = 0.5 * actualZAcceleration * turningTime * turningTime + velocity.z * turningTime + position.z;

            // Return null if height is never reached
            if (turningHeight < height) return Double.NaN;

            // The height is reached on the way up!
            if (position.z < height) {
                // See technical documents for this equation : t = (-v + sqrt(2*a*h - 2*a*p + v^2) / a
                return (-velocity.z + Math.sqrt(2 * actualZAcceleration * height - 2 * actualZAcceleration * position.z + velocity.z * velocity.z)) / actualZAcceleration;
            }

            // No cases hit, everything is fine
        }

        // See technical documents for this equation : t = -(v + sqrt(2*a*h - 2*a*p + v^2) / a
        return -(velocity.z + Math.sqrt(2 * actualZAcceleration * height - 2 * actualZAcceleration * position.z + velocity.z * velocity.z)) / actualZAcceleration;
    }

    /** Helper function for {@link #predictArrivalAtHeight(Rigidbody, double, boolean)} for when there is no acceleration, only velocity.
     * @return the expected time till arrival, or NaN if the rigidbody will never arrive at {@code height} */
    private static double predictArrivalAtHeightLinear(Rigidbody body, double height) {

        Vector3 position = body.getPosition();
        Vector3 velocity = body.getVelocity();

        if (velocity.z == 0) return Double.NaN; // no velocity

        // time of arrival
        double arrivalTime = (height - position.z) / velocity.z;

        if (arrivalTime < 0) return Double.NaN; // time is in the past -> will never get there

        return arrivalTime;
    }

    /** @param offset the offset from the wall. Relevant for any objects with a radius.
     * @return the time until arrival at any wall. Can be NaN. */
    public static double predictArrivalAtAnyWall(Rigidbody body, double offset) {
        double[] arrivalTimes = {
                predictArrivalAtWallXPositive(body, offset),
                predictArrivalAtWallXNegative(body, offset),
                predictArrivalAtWallYPositive(body, offset),
                predictArrivalAtWallYNegative(body, offset)
        };
        double earliestTimeOfArrival = Double.NaN;
        for (int i = 0; i < arrivalTimes.length; i++) {
            if (!Double.isNaN(arrivalTimes[i]) && (Double.isNaN(earliestTimeOfArrival) || arrivalTimes[i] < earliestTimeOfArrival)) {
                earliestTimeOfArrival = arrivalTimes[i];
            }
        }
        return earliestTimeOfArrival;
    }

    /** @return whether the next wall hit will be a side wall of the arena as opposed to an end wall (those by the goals).
     * If the Rigidbody never hits a wall, false i returned. */
    public static boolean willHitSideWallNext(Rigidbody body, double offset) {
        double[] arrivalTimes = {
                predictArrivalAtWallXPositive(body, offset),
                predictArrivalAtWallXNegative(body, offset),
                predictArrivalAtWallYPositive(body, offset),
                predictArrivalAtWallYNegative(body, offset)
        };
        double wallIndex = -1;
        double earliestTimeOfArrival = Double.NaN;
        for (int i = 0; i < arrivalTimes.length; i++) {
            if (!Double.isNaN(arrivalTimes[i])) {
                if (Double.isNaN(earliestTimeOfArrival) || arrivalTimes[i] < earliestTimeOfArrival) {
                    earliestTimeOfArrival = arrivalTimes[i];
                    wallIndex = i;
                }
            }
        }
        if (wallIndex == 0 || wallIndex == 1)
            return true;
        else
            return false;
    }


    /** @param offset the offset from the wall. Relevant for any objects with a radius.
     * @return the time until arrival at wall at x positive. */
    public static double predictArrivalAtWallXPositive(Rigidbody body, double offset) {
        double distance = Arena.WIDTH / 2 - offset;
        if (body.getVelocity().x > 0) {
            if (body.getPosition().x < distance) {
                return (distance - body.getPosition().x) / body.getVelocity().x;
            } else {
                // We assume that if the RigidBody is outside of the field, it will be pushed in immediately
                return 0;
            }
        }
        return Double.NaN;
    }

    /** @param offset the offset from the wall. Relevant for any objects with a radius.
     * @return the time until arrival at wall at x negative. */
    public static double predictArrivalAtWallXNegative(Rigidbody body, double offset) {
        double distance = Arena.WIDTH / 2 - offset;
        if (body.getVelocity().x < 0) {
            if (body.getPosition().x > -distance) {
                return (-distance - body.getPosition().x) / body.getVelocity().x;
            } else {
                // We assume that if the RigidBody is outside of the field, it will be pushed in immediately
                return 0;
            }
        }
        return Double.NaN;
    }

    /** @param offset the offset from the wall. Relevant for any objects with a radius.
     * @return the time until arrival at wall at y positive. */
    public static double predictArrivalAtWallYPositive(Rigidbody body, double offset) {
        double distance = Arena.LENGTH / 2 - offset;
        if (body.getVelocity().y > 0) {
            if (body.getPosition().y < distance) {
                return (distance - body.getPosition().y) / body.getVelocity().y;
            } else {
                // We assume that if the RigidBody is outside of the field, it will be pushed in immediately
                return 0;
            }
        }
        return Double.NaN;
    }

    /** @param offset the offset from the wall. Relevant for any objects with a radius.
     * @return the time until arrival at wall at y negative. */
    public static double predictArrivalAtWallYNegative(Rigidbody body, double offset) {
        double distance = Arena.LENGTH / 2 - offset;
        if (body.getVelocity().y < 0) {
            if (body.getPosition().y > -distance) {
                return (-distance - body.getPosition().y) / body.getVelocity().y;
            } else {
                // We assume that if the RigidBody is outside of the field, it will be pushed in immediately
                return 0;
            }
        }
        return Double.NaN;
    }
}
