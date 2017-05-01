package tothemoon.simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import tothemoon.VectorMath;

//@author Michael Haertling
public class Rocket extends IntelligentEntity{

    private final double maxSideBoost, maxMainBoost;
    private Image image;

    //Statistics
    RocketStatsForm stats;
    
    public Rocket(double x, double y, double width, double height, double mass,
            double initialRotation, double maxSideBoost, double maxMainBoost, double maxLandingRotation) {
        super(x, y, width, height, mass, initialRotation);
        this.maxSideBoost = maxSideBoost;
        this.maxMainBoost = maxMainBoost;
        
        this.setAcceptedLandingVariance(maxLandingRotation);
        
        stats = new RocketStatsForm();

        generateImage(Color.RED);
    }

    private void generateImage(Color c){
        //Draw the image
        int width = (int)getWidth();
        int height = (int)getHeight();
        BufferedImage i = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) i.getGraphics();
        g.setColor(c);
        g.drawRect(0, 0,  width - 1,  height - 1);
        g.drawRect(1, 1,  width - 3,  height - 3);
        g.drawRect(2, 2,  width - 4,  height - 4);
        g.setColor(Color.GREEN);
        g.drawLine(0, 0,  width, 0);
        g.dispose();
        image = i;
    }
    
    public void setColor(Color c){
        generateImage(c);
    }
    
    @Override
    public void setFitness(double fitness){
        super.setFitness(fitness);
        stats.setFitness(fitness);
    }
    
    @Override
    public void reset(double x, double y, double rotation) {
        super.reset(x, y, rotation);
        stats.reset();
    }


    @Override
    public void updatePosition(long milis){
        super.updatePosition(milis);
        //Record time survived
        stats.incrementTimeSurvived(milis);
    }

    @Override
    public void shiftPosition(double dx, double dy){
        super.shiftPosition(dx, dy);
        //Record the distance traveled
        stats.incrementDistanceTraveled(VectorMath.getDistance(dx, dy));
    }
    
    @Override
    public Image getImage() {
        return image;
    }


    @Override
    public void applyInputsToEntity(double[] inputs, long milis) {
        //Calculate the side and main forces
        double mainForce = inputs[0] * this.maxMainBoost;
        double sideForce = inputs[1] * this.maxSideBoost;
        
        //Vectorize the forces
        double mainX = mainForce * Math.sin(getRotation());
        double mainY = mainForce * Math.cos(getRotation());

        
        //Change the velocities
        double time = milis / 1000.0;
        this.incrementVelocityX(((mainX) / getMass()) * time);
        this.incrementVelocityY(-((mainY) / getMass()) * time);
//        this.setVelocityX(((mainX) / getMass()) * time);
//        this.setVelocityY(-((mainY) / getMass()) * time);

        
        //torque/(mr^2) = (angular acceleration)
//        this.incrementAngularVelocity((sideForce/(getMass()*Math.pow(getHeight()/2, 2))) * time);
        this.setAngularVelocity((sideForce/(getMass()*(getHeight()/2))) * time);
    }    

    @Override
    public RocketStatsForm getStats() {
        return stats;
    }
    
}
