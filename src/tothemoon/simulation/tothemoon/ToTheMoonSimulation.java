package tothemoon.simulation.tothemoon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import tothemoon.VectorMath;
import tothemoon.genetics.ToTheMoonFitnessVariables;
import tothemoon.neuralnet.ActivationFunction;
import tothemoon.neuralnet.NegToPosOneActivationFunction;
import tothemoon.neuralnet.ZeroToOneActivationFunction;
import tothemoon.simulation.BrainGenerator;
import tothemoon.simulation.GeneticGalaxySimulation;
import tothemoon.simulation.IntelligentEntity;
import tothemoon.simulation.InvalidNumberOfThreadsException;
import tothemoon.simulation.Planet;
import tothemoon.simulation.Rocket;
import tothemoon.simulation.RocketStation;
import tothemoon.simulation.StatsForm;
import tothemoon.simulation.UnsupportedStatsFormException;
import tothemoon.simulation.UpdateThread;

//@author Michael Haertling
public class ToTheMoonSimulation extends GeneticGalaxySimulation {

    int numThreads;
    Planet earth, moon;
    RocketStation station;
    BrainGenerator bg;
    ToTheMoonFitnessVariables fitVars;
    ToTheMoonStatsForm stats;

    public ToTheMoonSimulation() {

    }

    @Override
    public void setup(HashMap<String, Double> vars) throws InvalidNumberOfThreadsException {
        fitVars = new ToTheMoonFitnessVariables(vars);
        stats = new ToTheMoonStatsForm(fitVars);
        numThreads = vars.get("NumThreads").intValue();
        stats.setNumThreads(numThreads);

        setupPlanets(vars);
        setupLogic(vars);
        setupRockets(vars);

        //Set the starting view points
        this.setViewX(station.getStartX() - 400);
        this.setViewY(station.getStartY() - 400);

        super.setup(vars);
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

    private void setupLogic(HashMap<String, Double> vars) {
        int numInputs = vars.get("NumInputs").intValue();
        int numOutputs = vars.get("NumOutputs").intValue();
        int numHiddenLayers = vars.get("NumHiddenLayers").intValue();
        int neuronsPerHiddenLayer = vars.get("NeuronsPerHiddenLayer").intValue();
        
        //Create the brain generator
        ActivationFunction[] funcs = {new ZeroToOneActivationFunction(1), new NegToPosOneActivationFunction(1.5)};
        bg = new BrainGenerator(numInputs, numOutputs,
                numHiddenLayers, neuronsPerHiddenLayer, funcs);

        
    }

    private void setupRockets(HashMap<String, Double> vars) {
        double rocketDegrees = Math.toRadians(vars.get("RocketStartingDegrees"));
        double rocketWidth = vars.get("RocketWidth");
        double rocketHeight = vars.get("RocketHeight");
        double rocketMass = vars.get("RocketMass");
        double rocketSideBoost = vars.get("RocketMaxSideBoost");
        double rocketMainBoost = vars.get("RocketMaxMainBoost");
        double maxLandingRotation = Math.toRadians(vars.get("AcceptedLandingDegreeVariance"));

        //Create the rocket station
        station = new RocketStation(earth, rocketWidth,
                rocketHeight, rocketMass,
                (rocketDegrees), rocketSideBoost, rocketMainBoost, maxLandingRotation);

        //Generate the rockets
        int numRockets = vars.get("NumEntities").intValue();
        for (int i = 0; i < numRockets; i++) {
            Rocket r = station.generateNewRocket();
            addEntity(r);
            //Add a brain to the rocket
            r.setBrain(bg.generateBrain());
        }
    }

    @Override
    public UpdateThread createNewUpdateThread(ArrayList<Planet> planets, long simulationTime, long updateWaitTime) {
        return new ToTheMoonUpdateThread(planets, simulationTime, updateWaitTime, fitVars);
    }

    @Override
    public void updateScreenStats(int rocketsRemaining) {
        if (screen.needsUpdate()) {

            //Find the center of the screen
            double screenCenterX = screen.getViewX() + screen.getWidth() / (2 * screen.getZoom());
            double screenCenterY = screen.getViewY() + screen.getHeight() / (2 * screen.getZoom());

            //Find earth vector
            double[] earthVector = {earth.getX() - screenCenterX, earth.getY() - screenCenterY};

            //Find moon vector
            double[] moonVector = {moon.getX() - screenCenterX, moon.getY() - screenCenterY};

            //Determine which are not in view
            boolean seeEarth = (VectorMath.getDistance(earthVector[0], earthVector[1]) - earth.getRadius() - screen.getRadius() / screen.getZoom()) < 0;
            boolean seeMoon = (VectorMath.getDistance(moonVector[0], moonVector[1]) - moon.getRadius() - screen.getRadius() / screen.getZoom()) < 0;

            //Normalize vectors if not in view
            if (!seeEarth) {
                VectorMath.normalize2D(earthVector);
            }

            if (!seeMoon) {
                VectorMath.normalize2D(moonVector);
            }

            screen.updateVectors(seeEarth, seeMoon, earthVector, moonVector);
        }

        screen.updateStats(getGenerationNumber(), rocketsRemaining, getElapsedTimeInGeneration());
    }

    /**
     * Generates new chromosomes using the genetic algorithms and replants them
     * in the entities. Then it saves stats and restarts some variables.
     */
    @Override
    public void startNextCycle() {

        super.startNextCycle();
        //Reset things and set stats
        stats.setGeneration(getGenerationNumber());
        for (UpdateThread up : getUpdaters()) {
            stats.setLongestGeneration(up.getElapsedTimeInSimulation());
        }

    }

    @Override
    public StatsForm getStats() {
        return stats;
    }

    @Override
    public int getNumThreads() {
        return numThreads;
    }

    @Override
    public void updateStatsForm() {
        stats.setGenerationsPerSec(this.getGenerationNumber() / ((this.totalRunningTime) / 1000.0));
    }

    @Override
    public void endGenerationForEntity(IntelligentEntity e) {
        //Apply the rockets stats
        try {
            stats.applyStats(e.getStats());
        } catch (UnsupportedStatsFormException ex) {
            Logger.getLogger(ToTheMoonSimulation.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Reset the rocket
        station.resetRocket((Rocket) e);
    }

}
