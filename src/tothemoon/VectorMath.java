package tothemoon;



//@author Michael Haertling

public class VectorMath {
    
    public static void normalize2D(double[] vect){
        double dist = Math.sqrt((vect[0]*vect[0])+(vect[1]*vect[1]));
        vect[0]/=dist;
        vect[1]/=dist;
    }
    
    public static double boundRotation(double rotation) {
        double twopi = 2 * Math.PI;
        if (rotation < 0) {
            while (rotation < 0) {
                rotation += twopi;
            }
        } else if (rotation > twopi) {
            while (rotation > twopi) {
                rotation -= twopi;
            }
        }
        return rotation;
    }
    
    public static double getDistance(double dx, double dy){
        return Math.sqrt(Math.pow(dy, 2)+Math.pow(dx, 2));
    }
    
    public static double getDistance(double x1, double y1, double x2, double y2){
        return getDistance(x1-x2,y1-y2);
    }
}
