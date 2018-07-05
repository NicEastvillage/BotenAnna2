package botenanna.game;

import botenanna.math.Vector2;
import botenanna.math.Vector3;
import botenanna.math.Zone;

/** The Arena object contains constants and method to help getting the right constants. */
public class Arena {

    public static final double LENGTH = 10280;
    public static final double WIDTH = 8240;
    public static final double HEIGHT = 4060;
    public static final double DIAGONAL = 14300;

    public static final double WALL_X = WIDTH/2;
    public static final double WALL_Y = LENGTH/2;

    public static final double GOAL_POST_X_OFFSET = 720;
    public static final double GOAL_HEIGHT = 700;
    public static final double GOAL_DEPHT = 800;

    public static final Zone FIELD = new Zone(new Vector3(WALL_X, WALL_Y, HEIGHT), new Vector3(-WALL_X, -WALL_Y, HEIGHT));
    public static final Zone ORANGE_GOAL_ZONE_AREA = new Zone(new Vector3(-GOAL_POST_X_OFFSET, WALL_Y, 0), new Vector3(GOAL_POST_X_OFFSET, (WALL_Y - 1000), 1500));
    public static final Zone BLUE_GOAL_ZONE_AREA = new Zone(new Vector3(-GOAL_POST_X_OFFSET, -WALL_Y, 0), new Vector3(GOAL_POST_X_OFFSET, -(WALL_Y - 1000), 1500));
    public static final Zone MIDFIELD_ACROSS = new Zone(new Vector3(-WALL_X, -2080, HEIGHT), new Vector3(WALL_X, 2080, 0));
    public static final Zone BLUE_CORNER_NEGATIVE = new Zone(new Vector3(-WALL_X, -WALL_Y, HEIGHT), new Vector3(0, 0, 0));
    public static final Zone BLUE_CORNER_POSITIVE = new Zone(new Vector3(-WALL_X, WALL_Y, HEIGHT), new Vector3(0, 0, 0));
    public static final Zone ORANGE_CORNER_NEGATIVE = new Zone(new Vector3(WALL_X, WALL_Y, HEIGHT), new Vector3(0, 0, 0));
    public static final Zone ORANGE_CORNER_POSITVE = new Zone(new Vector3(WALL_X, -WALL_Y, HEIGHT), new Vector3(0, 0, 0));
    public static final Zone ORANGE_GOAL_INSIDE = new Zone(new Vector3(-GOAL_POST_X_OFFSET, WALL_Y, 0), new Vector3(GOAL_POST_X_OFFSET, (WALL_Y + GOAL_DEPHT), GOAL_HEIGHT));
    public static final Zone BLUE_GOAL_INSIDE = new Zone(new Vector3(-GOAL_POST_X_OFFSET, -WALL_Y, 0), new Vector3(GOAL_POST_X_OFFSET, -(WALL_Y + GOAL_DEPHT), GOAL_HEIGHT));

    public static final Vector2 BLUE_GOALPOST_LEFT = new Vector2(-GOAL_POST_X_OFFSET, -WALL_Y);
    public static final Vector2 BLUE_GOALPOST_RIGHT = new Vector2(GOAL_POST_X_OFFSET, -WALL_Y);
    public static final Vector2 ORANGE_GOALPOST_LEFT = new Vector2(-GOAL_POST_X_OFFSET, WALL_Y);
    public static final Vector2 ORANGE_GOALPOST_RIGHT = new Vector2(GOAL_POST_X_OFFSET, WALL_Y);
    public static final Vector3 BLUE_GOAL_POS = Vector3.FORWARD.scale(-4850);
    public static final Vector3 ORANGE_GOAL_POS = Vector3.FORWARD.scale(4850);
    public static final Vector3 BLUE_GOAL_LINE_POS = Vector3.FORWARD.scale(-LENGTH/2);
    public static final Vector3 ORANGE_GOAL_LINE_POS = Vector3.FORWARD.scale(LENGTH/2);

    /** @return either +1 or -1, depending on which end of the y-axis this player's goal is. */
    public static int getTeamGoalYDirection(int playerIndex) {
        return playerIndex == 0 ? -1 : 1;
    }

    /** @return a Zone that is equal to the whole field, but all walls are offset.
     * This makes i useful to test if things are close to the walls. Positive offsets inwards. */
    public static Zone getFieldWithWallOffset(double offset) {
        double wx = WALL_X - offset;
        double wy = WALL_Y - offset;
        return new Zone(new Vector3(wx, wy, HEIGHT), new Vector3(-wx, -wy, 0));
    }

    /** @return the goal box owner by playerIndex */
    public static Zone getGoalBoxArea(int playerIndex) {
        return playerIndex == 0 ? BLUE_GOAL_ZONE_AREA : ORANGE_GOAL_ZONE_AREA;
    }

    /** @return the box inside goal belonging to playerIndex */
    public static Zone getGoalInside(int playerIndex) {
        return playerIndex == 0 ? BLUE_GOAL_INSIDE : ORANGE_GOAL_INSIDE;
    }

    /** @return a point in front of the goal belonging to playerIndex */
    public static Vector3 getGoalPos(int playerIndex) {
        return playerIndex == 0 ? BLUE_GOAL_POS : ORANGE_GOAL_POS;
    }

    /** @return a point on the line of the goal belonging to playerIndex */
    public static Vector3 getGoalLinePos(int playerIndex) {
        return playerIndex == 0 ? BLUE_GOAL_LINE_POS : ORANGE_GOAL_LINE_POS;
    }

    /** @return an array with the goal posts of the goal owned by playerIndex. The first element will be the left post,
     * and second element will be the right post. */
    public static Vector2[] getGoalPosts(int playerIndex) {
        return playerIndex == 0 ?
                new Vector2[] {BLUE_GOALPOST_LEFT, BLUE_GOALPOST_RIGHT} :
                new Vector2[] {ORANGE_GOALPOST_LEFT, ORANGE_GOALPOST_RIGHT};
    }
}
