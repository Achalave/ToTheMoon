package tothemoon.neuralnet;



//@author Michael Haertling
/*
Uses a sigmoid function
*/
public class ZeroToOneActivationFunction implements ActivationFunction{

    final double response;
    
    public ZeroToOneActivationFunction(double res){
        response = res;
    }
    
    @Override
    public double calculate(double activation) {
        return (1/(1+Math.exp(-activation/response)));
    }
}
