package botenanna.math;


public class Zone {

    private double lowX;
    private double lowY;
    private double lowZ;
    private double highX;
    private double highY;
    private double highZ;

    public Zone(Vector3 cornerA, Vector3 cornerB) {
        lowX = Math.min(cornerA.x, cornerB.x);
        lowY = Math.min(cornerA.y, cornerB.y);
        lowZ = Math.min(cornerA.z, cornerB.z);
        highX = Math.max(cornerA.x, cornerB.x);
        highY = Math.max(cornerA.y, cornerB.y);
        highZ = Math.max(cornerA.z, cornerB.z);
    }

    public boolean contains(Vector3 point) {
        if (lowX <= point.x && point.x <= highX) {
            if (lowY <= point.y && point.y <= highY) {
                return lowZ <= point.z && point.z <= highZ;
            }
        }
        return false;
    }
}