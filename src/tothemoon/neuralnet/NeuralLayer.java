package tothemoon.neuralnet;

//@author Michael Haertling

import java.util.Arrays;

public class NeuralLayer {

    Neuron[] neurons;
    int numInputs;

    public NeuralLayer(int numNeurons, int numInputs, ActivationFunction[] funcs) {
        neurons = new Neuron[numNeurons];
        this.numInputs = numInputs;
        
        //Populate the array of neurons
        for (int i = 0; i < neurons.length; i++) {
            if (funcs != null) {
                neurons[i] = new Neuron(numInputs, funcs[i]);
            } else {
                neurons[i] = new Neuron(numInputs);
            }
        }
    }

    public NeuralLayer(int numNeurons, int numInputs) {
        neurons = new Neuron[numNeurons];
        this.numInputs = numInputs;
        //Populate the array of neurons
        for (int i = 0; i < neurons.length; i++) {
            neurons[i] = new Neuron(numInputs);
        }
    }

    public NeuralLayer(int numNeurons, int numInputs, ActivationFunction func) {
        neurons = new Neuron[numNeurons];
        //Populate the array of neurons
        for (int i = 0; i < neurons.length; i++) {
            neurons[i] = new Neuron(numInputs, func);
        }
    }

    public int getNumInputs() {
        return numInputs;
    }

    
    
    public double[] applyInputs(double[] inputs) throws IncorrectNumberOfInputsException {
        //Apply the inputs to every neuron in the layer
        double[] output = new double[neurons.length];
        for (int i = 0; i < neurons.length; i++) {
            output[i] = neurons[i].applyInputs(inputs);
        }
//        System.out.println("OUTPUTS: "+Arrays.toString(output));
        return output;
    }

    public int getNumNeurons() {
        return neurons.length;
    }

    //Separate out the weights and send them to the neuron
    public void syncWeights(double[][] genes, int start) throws IncorrectNumberOfWeightsException {
        for (int i = 0; i < neurons.length; i++) {
            neurons[i].syncWeights(genes[i + start]);
        }
    }
}
