package tothemoon.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import tothemoon.MyAction;
import tothemoon.genetics.Chromosome;
import tothemoon.genetics.GeneFactory;

//@author Michael Haertling
public abstract class GeneticGalaxySimulation extends GalaxySimulation {

    protected GeneFactory geneFactory;
    int generationNumber = 0;

    //For GET BEST N
    int bestToGet;
    GetBestNCompleteAction bestGotten;

    @Override
    public void setup(HashMap<String, Double> vars) throws InvalidNumberOfThreadsException{
        double crossoverRate = vars.get("CrossoverRate");
        double mutationRate = vars.get("MutationRate");
        int numEliteChosen = vars.get("NumEliteChosen").intValue();
        int numCopiesPerElite = vars.get("NumCopiesPerElite").intValue();
        double maxPerturbation = vars.get("MaxPerturbation");

        //Create the gene factory
        int numEntities = vars.get("NumEntities").intValue();
        geneFactory = new GeneFactory(numEntities, mutationRate, maxPerturbation,
                crossoverRate, numEliteChosen, numCopiesPerElite);
        
        super.setup(vars);
    }

    protected long getElapsedTimeInGeneration() {
        return elapsedTimeInGeneration;
    }

    public int getGenerationNumber() {
        return generationNumber;
    }

    /**
     * Generates new chromosomes using the genetic algorithms and replants them
     * in the entities. Then it saves stats and restarts some variables.
     */
    @Override
    public void startNextCycle() {
        this.generationNumber++;

        //Collect all the chromosomes and apply fitnesses to them
        ArrayList<Chromosome> chroms = new ArrayList<>();
        for (Entity e : getEntities()) {
            IntelligentEntity ie = (IntelligentEntity) e;
            chroms.add(ie.getChromosome());
            endGenerationForEntity(ie);
        }

        //Check if should get best n
        if (bestGotten != null) {
            Collections.sort(chroms);
            int bestSoFar = 0;
            for (int i = chroms.size() - 1; i > 0 && bestSoFar < bestToGet; i--) {
                bestGotten.addBest(chroms.get(i));
                bestSoFar++;
            }
            bestGotten.act();
            bestGotten = null;
        }

        //Run through the genetics system
        chroms = geneFactory.evolveChromosomes(chroms);

        //Apply the new chromosomes to the rockets
        for (int i = 0; i < getEntities().size(); i++) {
            IntelligentEntity e = (IntelligentEntity) getEntities().get(i);
            Chromosome chrom = chroms.get(i);
            e.setChromosome(chrom);
        }

        super.startNextCycle();

    }

    public abstract void endGenerationForEntity(IntelligentEntity e);

    public void getBestNEntities(int n, GetBestNCompleteAction action) {
        bestToGet = n;
        bestGotten = action;
    }
}
