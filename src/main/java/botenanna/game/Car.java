package botenanna.game;

import botenanna.math.RLMath;
import botenanna.math.Vector3;
import botenanna.prediction.Estimates;
import botenanna.prediction.Physics;
import botenanna.prediction.Rigidbody;
import rlbot.flat.GameTickPacket;
import rlbot.flat.PlayerInfo;

public class Car extends Rigidbody {

    // Global Variables
    public final static double ACCELERATION_BOOST = 650;
    public final static double ACCELERATION = 400;
    public final static double MAX_VELOCITY = 1410;
    public final static double MAX_VELOCITY_BOOST = 2300;
    public final static double TURN_ACCELERATION_DECREASE = 0.94;
    public final static double MAX_VELOCITY_WHILE_TURNING = MAX_VELOCITY * TURN_ACCELERATION_DECREASE;
    public final static double MAX_VELOCITY_WHILE_TURNING_BOOST = MAX_VELOCITY_BOOST * TURN_ACCELERATION_DECREASE;
    public final static double SUPERSONIC_SPEED_REQUIRED = MAX_VELOCITY_BOOST * 0.95;
    public final static double DECELERATION = -18;
    public final static double GROUND_OFFSET = 17.03;

    private final int team;
    private final int playerIndex;

    // Modifiable
    private int boost;
    private boolean hasJumped;
    private boolean hasDoubleJumped;
    private boolean isDemolished;

    // Dependent
    private Vector3 upVector;
    private Vector3 frontVector;
    private Vector3 sideVector;
    private boolean isSupersonic;
    private boolean isCarOnGround;
    private boolean isMidAir;
    private boolean isCarUpsideDown;
    private boolean isNearWall;

    private double distanceToBall;
    private double angleToBall;
    private double reachBallTimeFullSpeed;
    private double reachBallTimeNormalSpeed;
    private Vector3 reachBallPosFullSpeed;
    private Vector3 reachBallPosNormalSpeed;

    /** Constructor for a car in rocket league with data from the game packet. */
    public Car(int index, GameTickPacket packet) {

        playerIndex = index;
        PlayerInfo info = packet.players(index);
        team = info.team();

        setPosition(Vector3.convert(info.physics().location()));
        setVelocity(Vector3.convert(info.physics().velocity()));
        setRotation(Vector3.convert(info.physics().rotation()));
        setAngularVelocity(Vector3.convert(info.physics().angularVelocity()));

        boost = info.boost();
        hasJumped = info.jumped();
        hasDoubleJumped = info.doubleJumped();
        isDemolished = info.isDemolished();
        isSupersonic = info.isSupersonic();
        isMidAir = false; // TODO packet.getPlayers(index).getIsMidair() equivalent does not exist
        setBallDependentVariables(Ball.get(packet.ball()));

        isNearWall = !Arena.getFieldWithWallOffset(28).contains(getPosition());
    }

    /** Constructor for new car based on an old instance of car */
    public Car(Car oldCar) {

        team = oldCar.team;
        playerIndex = oldCar.playerIndex;

        setPosition(oldCar.getPosition());
        setVelocity(oldCar.getVelocity());
        setRotation(oldCar.getRotation());
        setAngularVelocity(oldCar.getAngularVelocity());

        boost = oldCar.boost;
        hasJumped = oldCar.hasJumped;
        hasDoubleJumped = oldCar.hasDoubleJumped;
        isDemolished = oldCar.isDemolished;
        isSupersonic = oldCar.isSupersonic;
        isMidAir = oldCar.isMidAir;

        distanceToBall = oldCar.distanceToBall;
        angleToBall = oldCar.angleToBall;
        reachBallTimeFullSpeed = oldCar.reachBallTimeFullSpeed;
        reachBallPosFullSpeed = oldCar.reachBallPosFullSpeed;
        reachBallTimeNormalSpeed = oldCar.reachBallTimeNormalSpeed;
        reachBallPosNormalSpeed = oldCar.reachBallPosNormalSpeed;

        isNearWall = !Arena.getFieldWithWallOffset(28).contains(getPosition());
    }

    @Override
    public Car clone() {
        return new Car(this);
    }

    public int getTeam() {
        return team;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public int getBoost() {
        return boost;
    }

    @Override
    public void setPosition(Vector3 position) {
        super.setPosition(position);
        isCarOnGround = position.z < 20;
    }

    @Override
    public void setVelocity(Vector3 velocity) {
        super.setVelocity(velocity);
        isSupersonic = velocity.getMagnitude() >= SUPERSONIC_SPEED_REQUIRED;
    }

    @Override
    public void setAcceleration(Vector3 acceleration) {
        super.setAcceleration(acceleration);
    }

    @Override
    public void setRotation(Vector3 rotation) {
        super.setRotation(rotation);
        upVector = RLMath.carUpVector(rotation);
        frontVector = RLMath.carFrontVector(rotation);
        sideVector = RLMath.carSideVector(rotation);
        isCarUpsideDown = upVector.z < 0;
    }

    @Override
    public void setAngularVelocity(Vector3 angularVelocity) {
        super.setAngularVelocity(angularVelocity);
    }

    public void setBallDependentVariables(Rigidbody ball) {
        Vector3 ballPosition = ball.getPosition();
        angleToBall = RLMath.carsAngleToPoint(getPosition().asVector2(),  getRotation().yaw, ballPosition.asVector2());
        distanceToBall = getPosition().getDistanceTo(ballPosition);

        Rigidbody ballClone = ball.clone();
        reachBallTimeFullSpeed = Estimates.timeTillCarCanHitBall(getPosition(), ball, 1);
        reachBallPosFullSpeed = Physics.stepBall(ballClone, reachBallTimeFullSpeed).getPosition();

        ballClone.set(ball);
        reachBallTimeNormalSpeed = Estimates.timeTillCarCanHitBall(getPosition(), ball, 0.63);
        reachBallPosNormalSpeed = Physics.stepBall(ballClone, reachBallTimeNormalSpeed).getPosition();
    }

    public void setBoost(int amount) {
        this.boost = Math.min(Math.max(0, amount), 100);
    }

    public void addBoost(int amount) {
        this.boost = Math.min(Math.max(0, this.boost + amount), 100);
    }

    public boolean isHasJumped() {
        return hasJumped;
    }

    public void setHasJumped(boolean hasJumped) {
        this.hasJumped = hasJumped;
    }

    public boolean hasDoubleJumped() {
        return hasDoubleJumped;
    }

    public void setHasDoubleJumped(boolean hasDoubleJumped) {
        if (hasDoubleJumped) hasJumped = true;
        this.hasDoubleJumped = hasDoubleJumped;
    }

    public boolean isDemolished() {
        return isDemolished;
    }

    public void setDemolished(boolean demolished) {
        isDemolished = demolished;
    }

    public Vector3 getUpVector() {
        return upVector;
    }

    public Vector3 getFrontVector() {
        return frontVector;
    }

    public Vector3 getSideVector() {
        return sideVector;
    }

    public boolean isSupersonic() {
        return isSupersonic;
    }

    public boolean isCarOnGround() {
        return isCarOnGround;
    }

    public boolean isMidAir() {
        return isMidAir;
    }

    public boolean isCarUpsideDown() {
        return isCarUpsideDown;
    }

    public boolean isNearWall() {
        return isNearWall;
    }

    public double getDistanceToBall() {
        return distanceToBall;
    }

    public double getAngleToBall() {
        return angleToBall;
    }

    public void setIsMidAir(boolean midAir) {
        isMidAir = midAir;
    }

    public boolean isHasDoubleJumped() {
        return hasDoubleJumped;
    }

    public double getReachBallTimeFullSpeed() {
        return reachBallTimeFullSpeed;
    }

    public double getReachBallTimeNormalSpeed() {
        return reachBallTimeNormalSpeed;
    }

    public Vector3 getReachBallPosFullSpeed() {
        return reachBallPosFullSpeed;
    }

    public Vector3 getReachBallPosNormalSpeed() {
        return reachBallPosNormalSpeed;
    }
}