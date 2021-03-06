package botenanna.prediction;

import botenanna.game.ActionSet;
import botenanna.game.BoostPad;
import botenanna.game.Car;
import botenanna.game.Situation;
import botenanna.math.Vector3;

import static botenanna.game.Car.*;

public class Simulation {

    /** Simulates the situation forward a stepsize measured in seconds.
     * The simulatio, simulates the player car, enemy car, ball and boostpads to create a new situation
     * @return A new simulated situation
     **/
    public static Situation  simulate(Situation situation, double stepsize, ActionSet action){
        if (stepsize < 0) throw new IllegalArgumentException("Step size must be more than zero. Current Step size is: "+stepsize);

        Rigidbody simulatedBall = simulateBall(situation.getBall(), stepsize);
        Car simulatedMyCar = simulateCarActions(situation.getMyCar(), action,  simulatedBall, stepsize);
        Car simulatedEnemyCar = steppedCar(situation.getEnemyCar(), stepsize);
        BoostPad[] simulatedBoostPads = simulateBoostPads(situation.getBoostPads(), simulatedEnemyCar, simulatedMyCar, stepsize);

        simulatedMyCar.setBallDependentVariables(simulatedBall);
        simulatedEnemyCar.setBallDependentVariables(simulatedBall);

        return new Situation(simulatedMyCar, simulatedEnemyCar, simulatedBall , simulatedBoostPads);
    }

    /** Simulates the boostPads, if any of the cars can pick up boost and they are stepped close to a pad deactivate them
     * @return an array of boostPads after simulation. */
    private static BoostPad[] simulateBoostPads(BoostPad[] boostPads, Car enemyCar, Car myCar, double stepsize) {

        BoostPad[] simulatedPads = new BoostPad[boostPads.length];

        for (int i = 0; i < boostPads.length; i++) {
            BoostPad pad = new BoostPad(boostPads[i]);
            simulatePickupOfBoostPad(pad, myCar);
            simulatePickupOfBoostPad(pad, enemyCar);
            pad.reduceRespawnTimeLeft(stepsize);
            simulatedPads[i] = pad;
        }

        return simulatedPads;
    }

    /** Checks if car is touching pad. If they do, give the car boost and refresh boostpads respawn timer. */
    private static void simulatePickupOfBoostPad(BoostPad pad, Car car) {
        if (pad.getPosition().getDistanceTo(car.getPosition()) < BoostPad.PAD_RADIUS) {
            pad.refreshRespawnTimer();
            car.addBoost(pad.getBoostAmount());
        }
    }

    /** @return a new ball which has been moved forwards. */
    public static Rigidbody simulateBall(Rigidbody ball, double step)    {
        return Physics.stepBall(ball, step);
    }

    /** @return a new car which has been moved forwards. */
    private static Car steppedCar(Car car, double step) {
        Car newCar = Physics.stepBody(car, step, car.isMidAir());
        Vector3 pos = newCar.getPosition();
        if (pos.z < Car.GROUND_OFFSET) {
            //Hit ground
            newCar.setPosition(pos.withZ(Car.GROUND_OFFSET));
            newCar.setVelocity(newCar.getVelocity().withZ(0));
            newCar.setIsMidAir(false);
        }
        return newCar;
    }

    /** Simulates a car with actions **
     * @param action the current actions from the Agent
     * @return a Car simulated forward in  the new situation     */
    private static Car simulateCarActions(Car car, ActionSet action, Rigidbody ball, double delta){

        boolean boosting = (action.isBoostDepressed() && car.getBoost() != 0);

        if (car.isMidAir()) {

        } else {
            // We are on the ground
            double newYaw = car.getRotation().yaw + getTurnRate(car) * action.getSteer() * delta;
            newYaw %= Math.PI; // Clamp to be between -PI and PI
            car.setRotation(car.getRotation().withYaw(newYaw));

            Vector3 acceleration = new Vector3();

            if (boosting) {
                acceleration = acceleration.plus(car.getFrontVector().scale(ACCELERATION_BOOST));
            } else if (action.getThrottle() != 0) {
                acceleration = acceleration.plus(car.getFrontVector().scale(getAccelerationStrength(car, (int)action.getThrottle(), false)));
            } else {
                // we assume our velocity is never sideways
                acceleration = acceleration.plus(car.getVelocity().getNormalized().scale(DECELERATION));
            }

            if (action.getSteer() != 0) {
                acceleration = acceleration.scale(TURN_ACCELERATION_DECREASE);
            }

            car.setAcceleration(acceleration);
        }

        car = steppedCar(car, delta);
        car.setBallDependentVariables(ball);

        return car;
    }

    /** @param dir Direction of acceleration. 1 for forwards, -1 for backwards. */
    public static double getAccelerationStrength(Car car, int dir, boolean boosting) {
        Vector3 vel = car.getVelocity();
        Vector3 front = car.getFrontVector();

        double velProjFrontSize = vel.dot(front) / front.dot(front);
        double velDir = (velProjFrontSize >= 0) ? 1 : -1;
        Vector3 velParallelFront = front.scale(velProjFrontSize);
        double velLength = velParallelFront.getMagnitude();

        return MAX_VELOCITY_BOOST * dir - velLength * velDir;

    }

    /** @return the turn rate of the car. */
    public static double getTurnRate(Car car) {

        double vel = car.getVelocity().getMagnitude();
        // See documentation "turnrate linear function.png" for math.
        return 1.325680896 + 0.0002869694124 * vel;
    }

    /** @return the torque added to the angularYawVelocity.
     * Constructed from https://samuelpmish.github.io/notes/RocketLeague/ground_control/ */
    public static double getGroundTurningTorque(double steer, double velocity, double angularYawVelocity) {
        double vel100 = velocity / 100d;
        double curve = 0.01311 * vel100 * vel100 + 0.56246 * vel100 + 7;
        curve /= 1000d;
        return steer * curve * velocity - angularYawVelocity;
    }
}