package tothemoon.simulation;

import tothemoon.genetics.Chromosome;
import tothemoon.neuralnet.NeuralNetwork;
import tothemoon.neuralnet.ActivationFunction;
import tothemoon.neuralnet.NeuralLayer;



//@author Michael Haertling

public class Brain {
    
    Chromosome chromosome;
    NeuralNetwork net;
    
    public Brain(int numInputs, int numOutputs, int numHiddenLayers, 
            int numNeuronsPerLayer, ActivationFunction[] functions){
        //Instantiate the neural network
        net = new NeuralNetwork(numInputs,numOutputs,numHiddenLayers,
                numNeuronsPerLayer,functions);
        //Instantiate the chromosome
        int[] weights = new int[net.getNumNeurons()];
        int index = 0;
        NeuralLayer[] layers = net.getNeuralLayers();
        for(NeuralLayer layer:layers){
            for(int i=0;i<layer.getNumNeurons();i++){
                //Get the number of inputs per neuron in the net
                //remember to account for the bias (hence, the +1)
                weights[index++] = layer.getNumInputs()+1;
            }
        }
        chromosome = new Chromosome(net.getNumNeurons(),weights);
        //Sync up the chromosome with the entire net
        net.syncWeights(chromosome.getGeneData());
    }
    
    public Brain(NeuralNetwork n){
        net = n;
    }
    
    public Brain(int numInputs, int numOutputs, int numHiddenLayers, 
            int numNeuronsPerLayer, ActivationFunction[] functions,Chromosome c){
        //Instantiate the neural network
        net = new NeuralNetwork(numInputs,numOutputs,numHiddenLayers,
                numNeuronsPerLayer,functions);
        chromosome = c;
        net.syncWeights(chromosome.getGeneData());
    }
    
    public Chromosome getChromosome(){
        return chromosome;
    }
    
    public void setChromosome(Chromosome c){
        chromosome = c;
        net.syncWeights(c.getGeneData());
    }
    
    public NeuralNetwork getNet(){
        return net;
    }
    
    public void setNet(NeuralNetwork n){
        net = n;
    }
}
