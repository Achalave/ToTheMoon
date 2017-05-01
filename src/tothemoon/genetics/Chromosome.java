package tothemoon.genetics;

import java.util.Arrays;



//@author Michael Haertling

public class Chromosome implements Comparable<Chromosome>{
    
    double[][] genes;
    double fitness;
    final int totalWeights;
    
    public Chromosome(int numGenes, int[] numCodesPerGene){
        //instantiate the genome array
//        genes = new Gene[numGenes];
//        
//        //fill it with random values between -1 and 1
//        for(int i=0;i<genes.length;i++){
//            genes[i] = new Gene(numCodesPerGene);
//        }
        int tw = 0;
        genes = new double[numGenes][];
        for(int r=0;r<numGenes;r++){
            double[] gene = new double[numCodesPerGene[r]];
            tw += numCodesPerGene[r];
            for(int c=0; c<gene.length; c++){
                gene[c] = Math.random()-Math.random();
            }
            genes[r] = gene; 
        }
        this.totalWeights = tw;
        
    }
    
    //For use with cloning
    private Chromosome(double[][] genes, int totalWeights,double fitness){
        this.genes = genes;
        this.totalWeights = totalWeights;
        this.fitness = fitness;
    }

    
    public int getNumWeights(){
        return totalWeights;
    }
    

    public void setFitness(double fitness){
        this.fitness = fitness;
    }

    public double getFitness(){
        return fitness;
    }
    
    @Override
    public int compareTo(Chromosome t) {
        double compare = fitness-t.fitness;
        if(compare == 0){
            return 0;
        }else if(compare<0){
            return -1;
        } else{
            return 1;
        }
    }
    
    public double[][] getGeneData(){
        return genes;
    }
    
    @Override
    public synchronized Chromosome clone() throws CloneNotSupportedException{
        //Replicate the genes
        double[][] copy = new double[genes.length][];
        for(int i=0; i<genes.length;i++){
            copy[i] = Arrays.copyOf(genes[i], genes[i].length);
        }
        return new Chromosome(copy,totalWeights,fitness);
    }
    
    @Override
    public String toString(){
        String out = "";
        for(double[] g:genes){
            out += Arrays.toString(g)+"\n";
        }
        return out;
    }
}
