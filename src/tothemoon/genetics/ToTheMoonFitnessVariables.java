package tothemoon.genetics;

import java.util.HashMap;
import tothemoon.VectorMath;
import tothemoon.simulation.Rocket;



//@author Michael Haertling

public class ToTheMoonFitnessVariables {
    
    //These are used to calculate the fitness for each rocket
    public final double maxDistanceTraveledPoints;
    public final double distanceTraveledConversion;
    public final double maxDistanceToMoonPoints;
    public final double distanceToMoonConversion;
    public final double maxSurvivalTimePoints;
    public final double survivalTimeConversion;
    public final double pointsForLanding;
    public final double pointsForCrashingOnMoon;
    public final double pointsForCrashingOnEarth;
    
    public ToTheMoonFitnessVariables(HashMap<String,Double> init){
        maxDistanceTraveledPoints = init.get("MaxDistanceTraveledPoints");
        distanceTraveledConversion = init.get("DistanceTraveledConversion");
        maxDistanceToMoonPoints = init.get("MaxDistanceToMoonPoints");
        distanceToMoonConversion = init.get("DistanceToMoonConversion");
        maxSurvivalTimePoints = init.get("MaxSurvivalTimePoints");
        survivalTimeConversion = init.get("SurvivalTimeConversion");
        pointsForLanding = init.get("PointsForLanding");
        pointsForCrashingOnMoon = init.get("PointsForCrashingOnMoon");
        pointsForCrashingOnEarth = init.get("PointsForCrashingOnEarth");
    }
    
    
}
