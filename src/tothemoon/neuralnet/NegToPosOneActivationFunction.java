package tothemoon.neuralnet;



//@author Michael Haertling
/*
Uses a shifted and stretched sigmoid function
*/
public class NegToPosOneActivationFunction implements ActivationFunction{

    final double response;
    
    public NegToPosOneActivationFunction(double res){
        response = res;
    }
    
    @Override
    public double calculate(double activation) {
        return (2/(1+Math.exp(-activation/response)))-1;
    }
}
