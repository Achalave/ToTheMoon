package tothemoon.simulation;

//@author Michael Haertling
public interface StatsForm {

    /**
     *
     * @return A string representation of the report contained within
     */
    public String generateReport();

    /**
     *Resets any statistics that should not persist between rounds
     */
    public void reset();

    /**
     * Should grab needed stats from the supplied stats form
     * @param form The statistical form to derive information from
     */
    public void applyStats(StatsForm form) throws UnsupportedStatsFormException;
}
