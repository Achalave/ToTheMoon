package tothemoon.neuralnet;

//@author Michael Haertling

public class Neuron {

    double[] weights;
    ActivationFunction function;
    int numWeights;

    public Neuron(int numInputs) {
        this.numWeights = numInputs+1;
    }

    public Neuron(int numInputs, ActivationFunction func) {
        //Remember the +1 accounts for the activation threshold
        //by converting it to a bias. This must be multiplied by -1
        this(numInputs);

        function = func;
    }

    public double applyInputs(double[] inputs) throws IncorrectNumberOfInputsException {
        //Make sure it is the correct number of inputs
        if (inputs.length != weights.length - 1) {
            System.out.println(inputs.length+" "+weights.length);
            throw new IncorrectNumberOfInputsException();
        }

        //Calculate the weighted sum
        double weightedSum = 0;
        for (int i = 0; i < inputs.length; i++) {
            weightedSum += weights[i] * inputs[i];
        }

        //Remove the bias from the sum
        weightedSum -= weights[weights.length - 1];

        //Run the sum through the activation function
        if (function != null) {
            return function.calculate(weightedSum);
        } else {
            return weightedSum;
        }
    }

    public double[] getWeights() {
        return weights;
    }

    public int getNumWeights() {
        return weights.length;
    }

    public void syncWeights(double[] g) throws IncorrectNumberOfWeightsException {
        if(g.length != numWeights){
            throw new IncorrectNumberOfWeightsException();
        }
        weights = g;
    }
    
}
