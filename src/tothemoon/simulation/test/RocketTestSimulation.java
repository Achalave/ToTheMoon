package tothemoon.simulation.test;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import tothemoon.GUI.GalaxyView;
import tothemoon.Main;
import tothemoon.neuralnet.HumanOverrideNeuralNetwork;
import tothemoon.simulation.Brain;
import tothemoon.simulation.GalaxySimulation;
import tothemoon.simulation.GenerationNotifier;
import tothemoon.simulation.InvalidNumberOfThreadsException;
import tothemoon.simulation.Planet;
import tothemoon.simulation.Rocket;
import tothemoon.simulation.StatsForm;
import tothemoon.simulation.UpdateThread;



//@author Michael Haertling

public class RocketTestSimulation extends GalaxySimulation{

    StatsForm stats;
    Planet earth, moon;
    Rocket rocket;
    
    boolean running = false;
    
    HumanOverrideNeuralNetwork control;
    
    public RocketTestSimulation() {

    }

    @Override
    public void setup(HashMap<String, Double> vars) throws InvalidNumberOfThreadsException {
        stats = new BlankStatsForm();
        
        setupPlanets(vars);
        setupRocket(vars);
        
        //Set the starting view points
        this.setViewX(rocket.getX() - 400);
        this.setViewY(rocket.getY() - 400);
        
        super.setup(vars);
    }

    
    
    private void setupRocket(HashMap<String, Double> vars){
        double rocketDegrees = Math.toRadians(vars.get("RocketStartingDegrees"));
        double rocketWidth = vars.get("RocketWidth");
        double rocketHeight = vars.get("RocketHeight");
        double rocketMass = vars.get("RocketMass");
        double rocketSideBoost = vars.get("RocketMaxSideBoost");
        double rocketMainBoost = vars.get("RocketMaxMainBoost");
        double maxLandingRotation = Math.toRadians(vars.get("AcceptedLandingDegreeVariance"));
        
        //Create a new rocket
        //Determine where to place the rocket        
        //Find starting position for rocket
        Point2D rocketPosition = earth.getSurfacePoint(rocketDegrees);
        //Must shift the rocket so that the base is on the planet
        rocketDegrees += Math.PI / 2;
        rocketPosition.setLocation(
                rocketPosition.getX() + ((rocketHeight / 2) * Math.sin(rocketDegrees)),
                rocketPosition.getY() - ((rocketHeight / 2) * Math.cos(rocketDegrees))
        );

        //Create the rocket station
        rocket = new Rocket(rocketPosition.getX(), rocketPosition.getY(), rocketWidth,
                rocketHeight, rocketMass,
                (rocketDegrees), rocketSideBoost, rocketMainBoost,maxLandingRotation);
        
        addEntity(rocket);
        
        //Make the rocket invincible
        rocket.setBrain(new Brain(1,2,1,1,null));
        rocket.setAcceptedLandingVariance(Math.PI*2);
        rocket.setNeuralNet((control=new HumanOverrideNeuralNetwork()));
    }
    
    private void setupPlanets(HashMap<String, Double> vars) {
        //Determine the radius of the earth and moon
        int earthRadius = vars.get("EarthRadius").intValue();
        int moonRadius = vars.get("MoonRadius").intValue();
        int lunarDistance = vars.get("LunarDistance").intValue();
        double earthMass = vars.get("EarthMass");
        double moonMass = vars.get("MoonMass");
        double distanceDampening = vars.get("DistanceDampening");
        int spaceBuffer = vars.get("SpaceBuffer").intValue();

        //Use realistic proportions
        if (vars.get("AutomateDistances").intValue() == 1) {
            final double moonToEarthMass = 81.274;
            final double moonToEarthRadius = 3.669;
            final double earthRadiusToDistBetween = 60.336;

            //Set the moon mass based off the earth mass
            moonMass = earthMass / moonToEarthMass;

            //Set the moon size based off the earth size
            moonRadius = (int) (earthRadius / moonToEarthRadius);

            //Set the distance between the moon and earth
            lunarDistance = (int) ((earthRadius * earthRadiusToDistBetween) / distanceDampening);
        }

        //Determine where to position the planets
        int dist = (int) (lunarDistance / 2) + earthRadius + moonRadius;

        int change = (int) (dist * Math.sin(Math.PI / 4));

        int mid = (change + earthRadius + spaceBuffer);

        //Create the planets
        earth = new Planet("Earth", mid - change, mid + change, earthRadius, earthMass);
        moon = new Planet("Moon", mid + change, mid - change, moonRadius, moonMass);

        //Add the planets to the simulation
        this.addPlanet(moon);
        this.addPlanet(earth);
    }
    
    @Override
    public void updateScreenStats(int rocketsRemaining) {
//        screen.updateStats(getGenerationNumber(), rocketsRemaining, getElapsedTimeInGeneration());
    }

    @Override
    public StatsForm getStats() {
        return stats;
    }
    
    //Disable the fitness calculations and simply run a basic update loop
    @Override
    public void run() {
        //Don't start two run loops
        if (running) {
            return;
        }

        //Start up the main thread
        running = true;
        new Thread() {
            long lastTime;
            long elapsedTime;
            long builtTime;

            @Override
            public void run() {
                while (running) {
                    if (isViewerMode()) {

                        //Let time pass
                        lastTime = System.currentTimeMillis();
                        while ((elapsedTime = System.currentTimeMillis() - lastTime) < getViewWaitTime()) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        builtTime += elapsedTime;
                        //Update Everything in increments of wait time
                        while (builtTime > getUpdateWaitTime()) {
                            setRocketMovement();
                            manualUpdate(getUpdateWaitTime());
                            builtTime -= getUpdateWaitTime();
                            elapsedTimeInGeneration += getUpdateWaitTime();
                        }
                        
                        //Apply the stats to the view if in need of update
                        if (getScreen() != null) {
                            updateScreenStats(0);
                        }

                        //Draw Everything
                        draw();

                    } //The program is in fast mode, do nothing
                }
            }
        }.start();
    }

    @Override
    public UpdateThread createNewUpdateThread(ArrayList<Planet> planets, long simulationTime, long updateWaitTime) {
        return new RocketTestUpdateThread(planets,simulationTime,updateWaitTime);
    }

    public void setRocketMovement(){
        GalaxyView screen = getScreen();
        if(screen.isUpPressed()){
            control.setMainBoost(1);
        }else{
            control.setMainBoost(0);
        }
        
        if(screen.isLeftPressed()){
            control.setSideBoost(-1);
        }else if(screen.isRightPressed()){
            control.setSideBoost(1);
        }else{
            control.setSideBoost(0);
        }
    }
    
    @Override
    public int getNumThreads() {
        return 1;
    }

    @Override
    public void updateStatsForm() {
    }
}
