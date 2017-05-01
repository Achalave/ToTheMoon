package tothemoon.simulation;

import tothemoon.genetics.Chromosome;
import tothemoon.neuralnet.IncorrectNumberOfInputsException;
import tothemoon.neuralnet.NeuralNetwork;



//@author Michael Haertling

public abstract class IntelligentEntity extends Entity{

    Brain brain;
    
    public IntelligentEntity(double x, double y, double width, double height, double mass, double initialRotation) {
        super(x, y, width, height, mass, initialRotation);
    }
    
    public void setBrain(Brain brain){
        this.brain = brain;
    }
    
    
    public void setFitness(double fitness){
        brain.getChromosome().setFitness(fitness);
    }
    
    public Chromosome getChromosome(){
        return brain.getChromosome();
    }
    
    public void setChromosome(Chromosome c){
        brain.setChromosome(c);
    }
    
    public int getNumInputs(){
        return brain.getNet().getNumInputs();
    }
    
    public void setNeuralNet(NeuralNetwork net){
        brain.setNet(net);
    }
    
    /**
     * Applies the given inputs to the entity, this should be specified
     * by the extending class
     * @param inputs The inputs to be utilized
     * @param milis
     */
    public abstract void applyInputsToEntity(double[] inputs,long milis);
    
    /**
     * Applies the inputs to the neural network in the internal brain
     * @param inputs The inputs for the neural network 
     * (length must match the expected length in the network)
     * @return The output from the neural network
     * @throws tothemoon.neuralnet.IncorrectNumberOfInputsException
     */
    public double[] applyInputsToNeuralNets(double[] inputs) throws IncorrectNumberOfInputsException{
        return brain.getNet().applyInputs(inputs);
    }
    
    /**
     * Applies the inputs through the entire system. First through the neural
     * network, which sends its output to the entity. 
     * Uses the applyInputsToEntity and applyInputsToNeuralNets methods.
     * @param inputs The inputs to be used
     * @param milis The amount of elapsed time since the last call
     * @throws IncorrectNumberOfInputsException 
     */
    public void applyInputs(double[] inputs,long milis) throws IncorrectNumberOfInputsException{
        applyInputsToEntity(applyInputsToNeuralNets(inputs),milis);
    }
}
