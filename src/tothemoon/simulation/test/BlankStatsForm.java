package tothemoon.simulation.test;

import tothemoon.simulation.StatsForm;
import tothemoon.simulation.UnsupportedStatsFormException;



//@author Michael Haertling

public class BlankStatsForm implements StatsForm{

    @Override
    public String generateReport() {
        return "";
    }

    @Override
    public void reset() {
    }

    @Override
    public void applyStats(StatsForm form) throws UnsupportedStatsFormException {
        
    }
    
    
    
}
