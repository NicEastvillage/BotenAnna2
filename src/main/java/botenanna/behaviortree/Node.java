package botenanna.behaviortree;

import botenanna.behaviortree.builder.BehaviourTreeChildException;
import botenanna.game.Situation;

public interface Node {
    /** Reset any data held by the node. */
    void reset();
    /** Run the node and its children.
     * @return a NodeStatus containing the result of the node. */
    NodeStatus run(Situation input) throws MissingNodeException;
    void addChild(Node child) throws BehaviourTreeChildException;
}
