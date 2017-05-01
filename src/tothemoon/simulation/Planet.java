package tothemoon.simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

//@author Michael Haertling
public class Planet {

    final static double gravConstant = 6.67408 * Math.pow(10, -11);
    final String name;
    int radius;
    double mass;
    int x, y;

    public Planet(String name, int x, int y, int radius, double mass) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.mass = mass;
        this.name = name;
    }

    //Returns the force exerted by this planet on the matter object
    public double calculateGravity(Entity r) {
        double distance = distanceBetweenCenters(r);
        return ( mass * r.getMass()) / distance;
    }
    
    public double distanceBetweenCenters(Entity r) {
        return distanceFromPoint((int) r.getCenterX(), (int) r.getCenterY());
    }

    public double distanceFromPoint(int x, int y) {
        return Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2));
    }

    public boolean getColission(Entity r) {
        Rectangle2D rect = r.getRectangle();
        Point2D pf = getRotatedCenterForColission(r);
//        System.out.println(pf);
        Ellipse2D newBounds = new Ellipse2D.Double(pf.getX() - radius, pf.getY() - radius, radius * 2, radius * 2);
        return newBounds.intersects(rect);
    }

    public Point2D getRotatedCenterForColission(Entity r){
        Point2D p = new Point2D.Double(x, y);
        Point2D pf = new Point2D.Double();
        AffineTransform.getRotateInstance(r.getRotation(), r.getCenterX(), r.getCenterY()).transform(p, pf);
        return pf;
    }
    
    public Point2D getNearestSurfacePoint(double x, double y) {
        //Find dx and dy
        double dx = x - this.x;
        double dy = y - this.y;

        //Find the angle and use it to find the surface point
        return getSurfacePoint(Math.atan2(dx,dy));
    }

    public Point2D getSurfacePoint(double rads) {
        double xn = radius * Math.cos(rads);
        double yn = radius * Math.sin(rads);

        return new Point2D.Double(x + xn, y + yn);
    }

    public Point2D getNearestSurfacePoint(Entity r) {
        return getNearestSurfacePoint(r.getCenterX(), r.getCenterY());
    }

    public void draw(Graphics2D g, double sx, double sy, double zoom) {
        g.setColor(Color.BLUE);
        int radiusN = (int)(radius*zoom);
        int topX = (int)((x+sx)*zoom-radiusN);
        int topY = (int)((y+sy)*zoom-radiusN);
        g.fillOval(topX, topY, radiusN * 2, radiusN * 2);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    
    public int getRadius(){
        return radius;
    }
    
    public String getName(){
        return name;
    }
}
