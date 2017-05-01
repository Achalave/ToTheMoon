package tothemoon.simulation.tothemoon;

//@author Michael Haertling
import tothemoon.genetics.ToTheMoonFitnessVariables;
import tothemoon.simulation.RocketStatsForm;
import tothemoon.simulation.StatsForm;
import tothemoon.simulation.UnsupportedStatsFormException;

public class ToTheMoonStatsForm implements StatsForm {

    double highestFitness;
    double highestFitnessInGeneration;
    long longestSurvival;
    double mostDistanceTraveled;
    double closestToMoon = Double.MAX_VALUE;
    int generation;
    long longestGeneration;
    int numThreads;
    double generationsPerSec;

    //Key Constants
    ToTheMoonFitnessVariables fitVars;

    public ToTheMoonStatsForm(ToTheMoonFitnessVariables fitVars) {
        this.fitVars = fitVars;
    }

    public double getMaxDistanceTraveledPoints() {
        return fitVars.maxDistanceTraveledPoints;
    }

    public double getDistanceTraveledConversion() {
        return fitVars.distanceTraveledConversion;
    }

    public double getMaxDistanceToMoonPoints() {
        return fitVars.maxDistanceToMoonPoints;
    }

    public double getDistanceToMoonConversion() {
        return fitVars.distanceToMoonConversion;
    }

    public double getMaxSurvivalTimePoints() {
        return fitVars.maxSurvivalTimePoints;
    }

    public double getSurvivalTimeConversion() {
        return fitVars.survivalTimeConversion;
    }

    public double getHighestFitness() {
        return highestFitness;
    }

    public void setHighestFitness(double highestFitness) {
        this.highestFitness = Math.max(highestFitness, this.highestFitness);
    }

    public double getHighestFitnessInGeneration() {
        return highestFitnessInGeneration;
    }

    public void setHighestFitnessInGeneration(double highestFitnessInGeneration) {
        this.highestFitnessInGeneration = Math.max(highestFitnessInGeneration, this.highestFitnessInGeneration);
    }

    public long getLongestSurvival() {
        return longestSurvival;
    }

    public void setLongestSurvival(long longestSurvival) {
        this.longestSurvival = Math.max(longestSurvival, this.longestSurvival);
    }

    public double getMostDistanceTraveled() {
        return mostDistanceTraveled;
    }

    public void setMostDistanceTraveled(double mostDistanceTraveled) {
        this.mostDistanceTraveled = Math.max(mostDistanceTraveled, this.mostDistanceTraveled);
    }

    public double getClosestToMoon() {
        return closestToMoon;
    }

    public void setClosestToMoon(double closestToMoon) {
        this.closestToMoon = Math.min(closestToMoon, this.closestToMoon);
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public long getLongestGeneration() {
        return longestGeneration;
    }

    public void setLongestGeneration(long longestGeneration) {
        this.longestGeneration = Math.max(longestGeneration, this.longestGeneration);
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public double getGenerationsPerSec() {
        return generationsPerSec;
    }

    public void setGenerationsPerSec(double generationsPerSec) {
        this.generationsPerSec = generationsPerSec;
    }

    
    @Override
    public String generateReport() {
        String text = "";
        text += "Number of Threads: " + getNumThreads() + "\n";
        text += "Generations Per Second: " + this.getGenerationsPerSec()+ "\n\n";
        text += "Generation: " + getGeneration() + "\n\n";
        text += "Most Fit: " + getHighestFitness() + "\n";
        text += "Most Fit in Generation: " + getHighestFitnessInGeneration() + "\n\n";
        text += "Longest Survival Time: " + getLongestSurvival() + "\n(fitness result: "
                + Math.min(getLongestSurvival() * getSurvivalTimeConversion(), getMaxSurvivalTimePoints()) + ")\n\n";
        text += "Most Distance Traveled: " + getMostDistanceTraveled() + "\n(fitness result: "
                + Math.min(getMostDistanceTraveled() * getDistanceTraveledConversion(), getMaxDistanceTraveledPoints()) + ")\n\n";
        text += "Closest To Moon: " + getClosestToMoon() + "\n(fitness result: "
                + Math.min(getDistanceToMoonConversion() / getClosestToMoon(), getMaxDistanceToMoonPoints()) + ")\n\n";
        text += "Longest Generation: " + getLongestGeneration() + "\n\n";
        return text;
    }

    @Override
    public void reset() {
        this.highestFitnessInGeneration = 0;
    }

    @Override
    public void applyStats(StatsForm form) throws UnsupportedStatsFormException {
        if (!(form instanceof RocketStatsForm)) {
            throw new UnsupportedStatsFormException();
        }
        RocketStatsForm rsf = (RocketStatsForm)form;
        
        this.setClosestToMoon(rsf.getDistanceToMoon());
        this.setHighestFitness(rsf.getFitness());
        this.setHighestFitnessInGeneration(rsf.getFitness());
        this.setLongestSurvival(rsf.getTimeSurvived());
        this.setMostDistanceTraveled(rsf.getDistanceTraveled());
    }

}
