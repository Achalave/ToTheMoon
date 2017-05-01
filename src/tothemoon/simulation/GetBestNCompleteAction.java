package tothemoon.simulation;

import java.util.ArrayList;
import tothemoon.genetics.Chromosome;



//@author Michael Haertling

public abstract class GetBestNCompleteAction{
    
    ArrayList<Chromosome> bestN;
    
    public GetBestNCompleteAction(){
        bestN = new ArrayList<>();
    }
    
    public void addBest(Chromosome e){
        bestN.add(e);
    }
    
    public ArrayList<Chromosome> getBestN(){
        return bestN;
    }
    
    public void act(){
        act(bestN);
    }
    
    public abstract void act(ArrayList<Chromosome> best);
}
