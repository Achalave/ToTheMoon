package tothemoon.simulation;


import tothemoon.neuralnet.ActivationFunction;
import tothemoon.simulation.Brain;



//@author Michael Haertling

public class BrainGenerator {
    
    int numInputs, numOutputs, numHiddenLayers, numNeuronsPerLayer;
    ActivationFunction[] functions;
    
    public BrainGenerator(int numInputs, int numOutputs, int numHiddenLayers,
            int numNeuronsPerLayer, ActivationFunction[] functions) {
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.numHiddenLayers = numHiddenLayers;
        this.numNeuronsPerLayer = numNeuronsPerLayer;
        this.functions = functions;
    }
    
    public Brain generateBrain(){
        return new Brain(numInputs, numOutputs, numHiddenLayers, numNeuronsPerLayer, functions);
    }
    
}
