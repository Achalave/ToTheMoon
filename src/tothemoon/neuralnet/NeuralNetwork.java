package tothemoon.neuralnet;

import java.util.logging.Level;
import java.util.logging.Logger;



//@author Michael Haertling

public class NeuralNetwork {
    
    NeuralLayer[] layers;
    int numNeurons,numInputs;
    
    public NeuralNetwork(int numInputs, int numOutputs, int numHiddenLayers, 
            int numNeuronsPerLayer, ActivationFunction[] functions){
        int layerInputs = numInputs;
        layers = new NeuralLayer[numHiddenLayers+1];
        //Generate the hidden layers
        for(int i=0;i<numHiddenLayers;i++){
            layers[i] = new NeuralLayer(numNeuronsPerLayer,layerInputs);
            layerInputs = layers[i].getNumNeurons();
        }
        //Generate the output layer
        layers[layers.length-1] = new NeuralLayer(numOutputs,layerInputs,functions);
        numNeurons = numHiddenLayers*numNeuronsPerLayer+numOutputs;
        this.numInputs = numInputs;
    }
    
    public double[] applyInputs(double[] inputs) throws IncorrectNumberOfInputsException{
        for (NeuralLayer layer : layers) {
            inputs = layer.applyInputs(inputs);
        }
        return inputs;
    }
    
    public NeuralLayer[] getNeuralLayers(){
        return layers;
    }
    
    public void syncWeights(double[][] weights){
        int start = 0;
        for (NeuralLayer layer : layers) {
            try {
                layer.syncWeights(weights, start);
            } catch (IncorrectNumberOfWeightsException ex) {
                Logger.getLogger(NeuralNetwork.class.getName()).log(Level.SEVERE, null, ex);
            }
            start += layer.getNumNeurons();
        }
    }

    public int getNumNeurons() {
        return numNeurons;
    }

    public int getNumInputs() {
        return numInputs;
    }
    
    
}
