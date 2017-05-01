package tothemoon.genetics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;



//@author Michael Haertling

public class GeneFactory {
    
    double mutationRate;
    double maxPerturbation;
    double crossoverRate;
    int numElite;
    int eliteCopies;
    int populationSize;
    
    //Statistics
    double currentTotalFitness;
    
    
    public GeneFactory(int populationSize, double mr, double mp, double cr, int numElite, int eliteCopies){
        mutationRate = mr;
        maxPerturbation = mp;
        crossoverRate = cr;
        this.numElite = numElite;
        this.eliteCopies = eliteCopies;
        this.populationSize = populationSize;
    }
    
    private void mutate(Chromosome c){
        //Randomly select and mutate the chromosome
        for(double[] gene:c.getGeneData()){
            for(int i=0; i<gene.length; i++){
                if(Math.random()<mutationRate){
                    //Mutate the data
                    gene[i] += (Math.random()-Math.random())*maxPerturbation;
                }
            }
        }
    }
    
    private void crossover(Chromosome c1, Chromosome c2){
        //Decide where to split
        int split = (int)(Math.random()*c1.getNumWeights());
        
        double[][] m1 = c1.getGeneData();
        double[][] m2 = c2.getGeneData();
        double store;
        
        //Begin the switch
        for(int r=0; r<m1.length; r++){
            //Stop spliting
            if(split <= 0){
                break;
            }
            for(int c=0; c<m1[r].length;c++){
                store = m1[r][c];
                m1[r][c] = m2[r][c];
                m2[r][c] = store;
                split--;
                //Stop spliting
                if(split <= 0){
                    break;
                }
            }
        }
    }
    
    private Chromosome getRouletteChromosome(ArrayList<Chromosome> chroms, double totalFitness){
        //Get a random fitness between 0 and total
        double slice = (Math.random()*totalFitness);
        double fitnessSum = 0;
        for(Chromosome c:chroms){
            fitnessSum += c.getFitness();
            if(fitnessSum >= slice){
                return c;
            }
        }
        
        //The total fitness supplied must have been incorrect
        return null;
    }
    
    
    private void addElite(ArrayList<Chromosome> cOld, ArrayList<Chromosome> cNew){
        //Get the numElite most elite
        int index = cOld.size()-1;
        for(int i=0;i<numElite;i++){
            //Add eliteCopies copies of each elite
            for(int c=0;c<eliteCopies;c++){
                //If first, just add the original
                if(c==0){
                    cNew.add(cOld.get(index));
                }
                //Else duplicate the original
                else{
                    try {
                        cNew.add(cOld.get(index).clone());
                    } catch (CloneNotSupportedException ex) {
                        Logger.getLogger(GeneFactory.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            index--;
            //Add a failsafe in case it has been asked to get more elite than
            //there are chromosomes
            if(index<0){
                break;
            }
        }
    }
    
    private void performCalculations(ArrayList<Chromosome> chroms){
        currentTotalFitness = 0;
        for(Chromosome c:chroms){
            double fitness = c.getFitness();
            currentTotalFitness += fitness;
        }
        
    }
    
    public ArrayList<Chromosome> evolveChromosomes(ArrayList<Chromosome> chroms){
        //Sort the chromosomes
        Collections.sort(chroms);
        
        //Do calculations with the fitness and progress
        performCalculations(chroms);
        
        //Begin creating a new set of chromosomes
        ArrayList<Chromosome> chromsNew = new ArrayList<>();
        
        //Add the elite to the new set
        addElite(chroms,chromsNew);
        
        //Start Loop until the population is the correct size
        while(chromsNew.size() < populationSize){
            try {
                //Get two chromosomes from the old array and clone them
                Chromosome c1 = getRouletteChromosome(chroms,currentTotalFitness).clone();
                Chromosome c2 = getRouletteChromosome(chroms,currentTotalFitness).clone();
                
                //Perform a crossover
                crossover(c1,c2);
                
                //Mutate the "new" chromosomes
                mutate(c1);
                mutate(c2);
                
                //Place them in the new set
                chromsNew.add(c1);
                if(chromsNew.size()<populationSize){
                    chromsNew.add(c2);
                }
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(GeneFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //Return the new set once populated
        return chromsNew;
    }
    
}
