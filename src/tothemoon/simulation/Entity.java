package tothemoon.simulation;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import tothemoon.VectorMath;

/**
 *
 * @author Michael
 */
public abstract class Entity implements Matter{

    private double centerX, centerY, x, y;
    final private double width, height;

    private double velocityX = 0, velocityY = 0;
    private double angularVelocity = 0;
    
    Rectangle2D rectStorage;
    boolean rectValid = false;

    private double rotation;
    final private double mass;
    
    private Planet collisionPlanet;
    private double acceptedLandingVariance = 2*Math.PI;
    private boolean crashed = false;

    public Entity(double x, double y, double width, double height, double mass, double initialRotation) {
        this.mass = mass;
        rotation = initialRotation;
        rotation = VectorMath.boundRotation(rotation);
        centerX = x;
        centerY = y;
        this.x = centerX - width / 2;
        this.y = centerY - height / 2;
        this.width = width;
        this.height = height;
    }

    public double getRotation() {
        return rotation;
    }

    /**
     * Resets variables between simulations
     * @param x Specifies what to reset x to
     * @param y Specifies what to reset y to
     * @param rotation Specifies what to reset rotation to
     */
    public void reset(double x, double y, double rotation) {
        this.setAngularVelocity(0);
        this.setVelocityX(0);
        this.setVelocityY(0);
        this.setPosition(x, y);
        this.setRotation(rotation);
        this.setCrashed(false);
    }
    
    public abstract Image getImage();

    public void draw(Graphics2D g, double sx, double sy, double zoom) {
        AffineTransform af = new AffineTransform();

        af.rotate(getRotation(), (centerX + sx) * zoom, (centerY + sy) * zoom);
        af.translate((x + sx) * zoom, (y + sy) * zoom);
        if (zoom > 1) {
            af.scale(zoom, zoom);
        }

        g.drawImage(getImage(), af, null);
    }

    /**
     * EXTERNAL FORCES SHOULD BE APPLIED BEFORE UPDATING
     * Calculates the thruster acceleration, applies it to the aggregate
     * velocity and then moves the rocket
     * @param milis Time elapsed since the last update call
     */
    public void updatePosition(long milis) {
        double time = milis / 1000.0;
        
        //Move the distance
        double dx = this.getVelocityX() * time;
        double dy = this.getVelocityY() * time;
        this.shiftPosition(dx,dy);
        
        //Rotate
        this.rotate(this.getAngularVelocity()*time);
    }
    
    public void applyForce(double forceX, double forceY, long milis) {
        //Find the velocities
        double time = milis / 1000.0;
        double velX = (forceX / getMass()) * time;
        double velY = (forceY / getMass()) * time;
        
        //Update the velocities
        this.incrementVelocityX(velX);
        this.incrementVelocityY(velY);
    }

    public void applyForceFrom(double force, double x, double y, long milis) {
        double cx = this.getCenterX();
        double cy = this.getCenterY();

        double dx = x-cx;
        double dy = y-cy;

        //Find the angle of application
        double angle = Math.atan2(dx, dy);
        //The trig in JAVA is so weird
//        angle += Math.PI/2;
        
        //Find the force components
        double fx = force * Math.sin(angle);
        double fy = force * Math.cos(angle);

        applyForce(fx, fy, milis);
    }
    
    @Override
    public double getMass() {
        return mass;
    }

    public void setRotation(double rot) {
        rotation = rot;
        rotation = VectorMath.boundRotation(rotation);
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setX(double x) {
        this.x = x;
        centerX = x + width / 2;
        rectValid = false;
    }

    public void setY(double y) {
        this.y = y;
        centerY = y + height / 2;
        rectValid = false;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
        this.x = centerX - width / 2;
        rectValid = false;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
        this.y = centerY - height / 2;
        rectValid = false;
    }

    public void shiftX(double x) {
        this.centerX += x;
        this.x += x;
        rectValid = false;
    }

    public void shiftY(double y) {
        this.centerY += y;
        this.y += y;
        rectValid = false;
    }

    public void shiftPosition(double x, double y) {
        shiftX(x);
        shiftY(y);
    }

    public void setPosition(double x, double y) {
        setX(x);
        setY(y);
    }

    public Rectangle2D getRectangle() {
        if (!rectValid) {
            rectStorage = new Rectangle2D.Double(x, y, width, height);
            rectValid = true;
        }
        return rectStorage;
    }

    public void rotate(double r) {
        rotation += r;
        rotation = VectorMath.boundRotation(rotation);
    }

    public double getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public double getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(double angularVelocity) {
        this.angularVelocity = angularVelocity;
    }
    
    public void incrementAngularVelocity(double inc){
        this.setAngularVelocity(angularVelocity+inc);
    }
         
    public void incrementVelocityX(double inc){
        this.setVelocityX(velocityX+inc);
    }
    
    public void incrementVelocityY(double inc){
        this.setVelocityY(velocityY+inc);
    }

    public Planet getCollsionPlanet() {
        return collisionPlanet;
    }

    public void setCollisionPlanet(Planet collsionPlanet) {
        this.collisionPlanet = collsionPlanet;
    }

    public double getAcceptedLandingVariance() {
        return acceptedLandingVariance;
    }

    public void setAcceptedLandingVariance(double acceptedLandingVariance) {
        this.acceptedLandingVariance = acceptedLandingVariance;
    }

    public boolean isCrashed() {
        return crashed;
    }

    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
    }

    
    
    public abstract StatsForm getStats();
    
}
