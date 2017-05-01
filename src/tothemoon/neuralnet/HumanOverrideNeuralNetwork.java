package tothemoon.neuralnet;

import tothemoon.neuralnet.NeuralNetwork;
import tothemoon.neuralnet.ActivationFunction;



//@author Michael Haertling

public class HumanOverrideNeuralNetwork extends NeuralNetwork{
    
    double[] out;

    public HumanOverrideNeuralNetwork(int numInputs, int numOutputs, int numHiddenLayers, int numNeuronsPerLayer, ActivationFunction[] functions) {
        super(numInputs, numOutputs, numHiddenLayers, numNeuronsPerLayer, functions);
        out = new double[2];
    }
    
    public HumanOverrideNeuralNetwork(){
        super(1, 1, 1, 1, null);
        out = new double[2];
    }

    public void setSideBoost(double sideBoost) {
        this.out[1] = sideBoost;
    }

    public void setMainBoost(double mainBoost) {
        this.out[0] = mainBoost;
    }
    
    @Override
    public double[] applyInputs(double[] inputs){
        return out;
    }
    
}
