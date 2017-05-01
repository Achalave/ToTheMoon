package tothemoon.simulation;

//@author Michael Haertling
public class RocketStatsForm implements StatsForm {

    private double distanceTraveled = 0;
    private long timeSurvived = 0;
    private double distanceToMoon = Double.MAX_VALUE;
    private double fitness;

    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    public void setDistanceTraveled(double distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
    }

    public long getTimeSurvived() {
        return timeSurvived;
    }

    public void setTimeSurvived(long timeSurvived) {
        this.timeSurvived = timeSurvived;
    }

    public double getDistanceToMoon() {
        return distanceToMoon;
    }

    public void setDistanceToMoon(double distanceToMoon) {
        this.distanceToMoon = distanceToMoon;
    }

    public void incrementTimeSurvived(long milis) {
        this.timeSurvived += milis;
    }

    public void incrementDistanceToMoon(double distance) {
        this.distanceToMoon += distance;
    }

    public void incrementDistanceTraveled(double distance) {
        this.distanceTraveled += distance;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    @Override
    public void reset() {
        distanceTraveled = 0;
        timeSurvived = 0;
        distanceToMoon = Double.MAX_VALUE;
        fitness = 0;
    }

    @Override
    public void applyStats(StatsForm form) throws UnsupportedStatsFormException {
        throw new UnsupportedStatsFormException();
    }

    @Override
    public String generateReport() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
