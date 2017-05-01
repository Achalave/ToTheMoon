package tothemoon.simulation;

//@author Michael Haertling

import java.awt.geom.Point2D;

public class RocketStation {

    double startX, startY, width, height;
    double startRot, mass, sideBoost, mainBoost, maxLandingRotation;
    Planet planet;
    boolean randomMode;

    public RocketStation(Planet p, double width, double height, double mass, double rot, double sideBoost, double mainBoost,double maxLandingRotation) {
        randomMode = rot<0;
        planet = p;
        this.startRot = rot;
        this.width = width;
        this.height = height;
        this.sideBoost = sideBoost;
        this.mainBoost = mainBoost;
        this.mass = mass;
        this.maxLandingRotation = maxLandingRotation;
        findLocation();
    }

    private void findLocation(){
        //If random mode, find a random starting rotation
        if(randomMode){
            startRot = Math.random()*Math.PI*2;
        }
        //Determine where to place the rocket        
        //Find starting position for rocket
        Point2D rocketPosition = planet.getSurfacePoint(startRot-Math.PI/2);
        //Must shift the rocket so that the base is on the planet
        rocketPosition.setLocation(
                rocketPosition.getX() + ((height / 2) * Math.sin(startRot)),
                rocketPosition.getY() - ((height / 2) * Math.cos(startRot))
        );
        
        startX = rocketPosition.getX();
        startY = rocketPosition.getY();
    }

    public Rocket generateNewRocket() {
        if(randomMode){
            findLocation();
        }
        return new Rocket(startX, startY, width, height, mass, startRot, sideBoost, mainBoost,maxLandingRotation);
    }

    public void resetRocket(Rocket r){
        if(randomMode){
            findLocation();
        }
        r.reset(startX, startY, startRot);
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }
    
    
    
}
