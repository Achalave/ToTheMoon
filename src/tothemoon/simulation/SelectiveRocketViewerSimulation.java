package tothemoon.simulation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import tothemoon.Main;
import tothemoon.genetics.Chromosome;
import tothemoon.neuralnet.ActivationFunction;
import tothemoon.neuralnet.NegToPosOneActivationFunction;
import tothemoon.neuralnet.NeuralNetwork;
import tothemoon.neuralnet.ZeroToOneActivationFunction;
import tothemoon.simulation.test.BlankStatsForm;
import tothemoon.simulation.test.RocketTestUpdateThread;

//@author Michael Haertling
public class SelectiveRocketViewerSimulation extends GalaxySimulation {

    StatsForm stats;
    Planet earth, moon;

    boolean running = false;

    RocketStation station;

    ArrayList<Chromosome> chroms;

    public SelectiveRocketViewerSimulation() {

    }

    @Override
    public void setup(HashMap<String, Double> vars) throws InvalidNumberOfThreadsException {
        stats = new BlankStatsForm();

        setupPlanets(vars);
        setupRockets(vars);

        //Set the starting view points
        this.setViewX(earth.getX());
        this.setViewY(earth.getY());

        super.setup(vars);
    }

    public void setChromosomes(ArrayList<Chromosome> c) {
        chroms = c;
    }

    public void setupRockets(HashMap<String, Double> vars) {
        double rocketDegrees = Math.toRadians(vars.get("RocketStartingDegrees"));
        double rocketWidth = vars.get("RocketWidth");
        double rocketHeight = vars.get("RocketHeight");
        double rocketMass = vars.get("RocketMass");
        double rocketSideBoost = vars.get("RocketMaxSideBoost");
        double rocketMainBoost = vars.get("RocketMaxMainBoost");
        double maxLandingRotation = Math.toRadians(vars.get("AcceptedLandingDegreeVariance"));

        int numInputs = vars.get("NumInputs").intValue();
        int numOutputs = vars.get("NumOutputs").intValue();
        int numHiddenLayers = vars.get("NumHiddenLayers").intValue();
        int neuronsPerHiddenLayer = vars.get("NeuronsPerHiddenLayer").intValue();

        ActivationFunction[] funcs = {new ZeroToOneActivationFunction(1), new NegToPosOneActivationFunction(1.5)};

        //Create the rocket station
        station = new RocketStation(earth, rocketWidth,
                rocketHeight, rocketMass,
                (rocketDegrees), rocketSideBoost, rocketMainBoost, maxLandingRotation);

        //Generate the rockets
        for (int i = 0; i < chroms.size(); i++) {
            Rocket r = station.generateNewRocket();
            r.setBrain(new Brain(numInputs, numOutputs, numHiddenLayers, neuronsPerHiddenLayer, funcs, chroms.get(i)));
            //Make the rockets a random color
            r.setColor(new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255)));
            addEntity(r);
        }
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
        screen.updateStats(0, rocketsRemaining, elapsedTimeInGeneration);
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
                        boolean complete = false;

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
                            manualUpdate(getUpdateWaitTime());
                            builtTime -= getUpdateWaitTime();
                            elapsedTimeInGeneration += getUpdateWaitTime();
                            complete = threadsComplete();
                            if (complete) {
                                break;
                            }
                        }

                        //Apply the stats to the view if in need of update
                        if (getScreen() != null) {
                            updateScreenStats(0);
                        }

                        //Draw Everything
                        draw();

                        //Check if the sim is done
                        if (complete) {
                            for (UpdateThread up : updaters) {
                                up.finalizeGeneration();
                            }
                            startNextCycle();
                            builtTime = 0;
                        }

                    } //The program is in fast mode, do nothing
                }
                running = false;
            }

        }.start();
    }

    @Override
    public void startNextCycle() {
        super.startNextCycle();
        //Reset the rockets
        for (Entity e : entities) {
            station.resetRocket((Rocket) e);
        }
    }

    @Override
    public UpdateThread createNewUpdateThread(ArrayList<Planet> planets, long simulationTime, long updateWaitTime) {
        return new RocketTestUpdateThread(planets, simulationTime, updateWaitTime);
    }

    @Override
    public int getNumThreads() {
        return 1;
    }

    @Override
    public void updateStatsForm() {
    }

    @Override
    public void setViwerMode(boolean mode) {
        super.setViwerMode(mode);
        if (mode == true && !running) {
            this.run();
        }
    }

}
