package botenanna.behaviortree;

import botenanna.behaviortree.builder.BehaviourTreeBuildingException;
import botenanna.game.ActionSet;
import botenanna.game.Situation;

public class BehaviorTree implements Node {

    private Node topNode;
    private NodeStatus lastNodeStatus;
    private String currentNodeString = "";

    @Override
    public void reset() {
        lastNodeStatus = null;
    }

    @Override
    public NodeStatus run(Situation input) throws MissingNodeException {

        if (topNode == null) throw new MissingNodeException(this);

        NodeStatus newNodeStatus;
        // Check if last status has high priority
        if (lastNodeStatus != null && lastNodeStatus.isHighPriority) {
            newNodeStatus = lastNodeStatus.creator.run(input);
        } else {
            newNodeStatus = topNode.run(input);
        }

        boolean newNodeHasValidAction = newNodeStatus != null && newNodeStatus.status == Status.RUNNING;

        // If newNodeStatus's creator is not the same as the lastNodeStatus's creator, then
        // lastNodeStatus's creator and all dependencies will be reset.
        if (lastNodeStatus != null && (!newNodeHasValidAction || newNodeStatus.creator != lastNodeStatus.creator)) {
            lastNodeStatus.creator.reset();
            /* FIXME Apparently lastNodeStatus is null here?
            for (Node dependencies : lastNodeStatus.getDependencies()) {
                dependencies.reset();
            }*/
        }

        // Set lastNodeStatus to newNodeStatus
        if (!newNodeHasValidAction) {
            // If newNodeStatus is null, something went wrong, so we just create one now.
            lastNodeStatus = new NodeStatus(Status.RUNNING, new ActionSet(), this);
        } else {
            lastNodeStatus = newNodeStatus;
        }

        return lastNodeStatus;
    }

    /** Evaluate the behaviour tree.
     * @return the ActionSet. */
    public ActionSet evaluate(Situation input) {
        NodeStatus nodeStatus = run(input);
        return nodeStatus.output;
    }

    /** Set the top node of the tree to {@code child}.
     * @param child top node of the tree. */
    @Override
    public void addChild(Node child) throws BehaviourTreeBuildingException {
        if (topNode != null) {
            throw new BehaviourTreeBuildingException();
        }

        topNode = child;
    }

    /** Returns the current node as a string. The string will be identical to the string from the behaviour tree
     * source file that the node was created from. */
    public String getCurrentNodeAsString() {
        if (lastNodeStatus == null) {
            return "Initializing...";
        } else if (lastNodeStatus.creator instanceof Leaf) {
            return ((Leaf)lastNodeStatus.creator).getOriginalString();
        } else {
            return "Thinking...";
        }
    }
}
