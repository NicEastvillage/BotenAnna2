package botenanna.behaviortree.intentions;

import botenanna.behaviortree.Leaf;
import botenanna.behaviortree.MissingNodeException;
import botenanna.behaviortree.NodeStatus;
import botenanna.behaviortree.Status;
import botenanna.game.ActionSet;
import botenanna.game.Situation;
import botenanna.prediction.AStar;
import botenanna.intentions.IntentionFunction;
import botenanna.math.TimeLine;

import javax.swing.event.MouseInputListener;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/** Intentions are nodes with no children. They use a intention function, the A*-algorithm, and simulation of the game to
 * find a sequence of ActionSets that will fulfil the intention. They return FAILURE when the method
 * {@link #shouldInterrupt(Situation)} returns true, and they return SUCCESS when the sequence is over. When there are
 * steps left in the sequence, they will return RUNNING with the evaluated ActionSet. */
public abstract class Intention extends Leaf {

    public static final double STEPSIZE = 0.05;

    private boolean isRunning = false;
    private IntentionFunction intentionFunction;
    private TimeLine<ActionSet> sequence;
    private LocalDateTime timer;

    public Intention(String[] arguments) throws IllegalArgumentException {
        super(arguments);

        if (!isValidNumberOfArguments(arguments.length)) throw new IllegalArgumentException();
    }

    protected boolean isValidNumberOfArguments(int argumentCount) {
        return argumentCount == 0;
    }

    @Override
    public void reset() {
        isRunning = false;
        intentionFunction = null;
        sequence = null;
    }

    @Override
    public NodeStatus run(Situation input) throws MissingNodeException {
        if (!isRunning) {
            intentionFunction = getIntentionFunction(input);
            sequence = AStar.findSequence(input, intentionFunction, STEPSIZE);
            timer = LocalDateTime.now();
            isRunning = true;
        }

        // Interrupted?
        if (shouldInterrupt(input)) {
            isRunning = false;
            return NodeStatus.DEFAULT_FAILURE;
        }

        double secondsPassed = timer.until(LocalDateTime.now(), ChronoUnit.MILLIS) * 0.001;

        // Out of next steps?
        if (sequence.getLastTime() < secondsPassed + STEPSIZE) {
            // Start over
            reset();
            return run(input);
        }

        ActionSet action = sequence.evaluate(secondsPassed);
        return new NodeStatus(Status.RUNNING, action, this);
    }

    protected abstract IntentionFunction getIntentionFunction(Situation input);
    protected abstract boolean shouldInterrupt(Situation input);
}
