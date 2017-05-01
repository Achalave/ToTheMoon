package tothemoon.simulation.test;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import tothemoon.VectorMath;
import tothemoon.neuralnet.IncorrectNumberOfInputsException;
import tothemoon.simulation.Entity;
import tothemoon.simulation.Planet;
import tothemoon.simulation.Rocket;
import tothemoon.simulation.UpdateThread;
import tothemoon.simulation.tothemoon.ToTheMoonUpdateThread;



//@author Michael Haertling

public class RocketTestUpdateThread extends UpdateThread{

    public RocketTestUpdateThread(ArrayList<Planet> planets, long simulationTime, long updateTime) {
        super(planets, simulationTime, updateTime);
    }

    @Override
    public void applyFitness(Entity e) {
    }
    
    @Override
    public void applyAllForces(Entity e,long elapsedTime){
        //Apply the self propultion of the rocket
        Rocket r = (Rocket)e;
        try {
            r.applyInputs(getInputs(r), elapsedTime);
        } catch (IncorrectNumberOfInputsException ex) {
            Logger.getLogger(ToTheMoonUpdateThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.applyAllForces(e, elapsedTime);
    }
  
     public double[] getInputs(Rocket r) {
        int numInputs = r.getNumInputs();
        //Find the moon and the earth
        Planet earth = null;
        Planet moon = null;
        for (Planet p : getPlanets()) {
            switch (p.getName()) {
                case "Moon":
                    moon = p;
                    break;
                case "Earth":
                    earth = p;
                    break;
            }
        }

        double[] inputs = new double[numInputs];
        int index = 0;

        /*
         Inputs to Do
         */
        //-Distance to Moon Surface
        Point2D p = moon.getNearestSurfacePoint(r);
        double distToMoon = p.distance(r.getCenterX(), r.getCenterY());
        inputs[index++] = distToMoon;

        //-Distance to Earth Surface
        p = earth.getNearestSurfacePoint(r);
        double distToEarth = p.distance(r.getCenterX(), r.getCenterY());
        inputs[index++] = distToEarth;

        //-Current Velocity X
        inputs[index++] = r.getVelocityX();

        //-Current Velocity Y
        inputs[index++] = r.getVelocityY();

        double[] vectMoon = {moon.getX() - r.getCenterX(), moon.getY() - r.getCenterY()};
        VectorMath.normalize2D(vectMoon);
        //-Direction Vector of Moon X
        inputs[index++] = vectMoon[0];

        //-Direction Vector of Moon Y
        inputs[index++] = vectMoon[1];

        double[] vectEarth = {earth.getX() - r.getCenterX(), earth.getY() - r.getCenterY()};
        VectorMath.normalize2D(vectEarth);
        //-Direction Vector of Earth X
        inputs[index++] = vectEarth[0];

        //-Direction Vector of Earth Y
        inputs[index++] = vectEarth[1];

        double rot = r.getRotation();
        double[] vectFacing = {Math.sin(rot), Math.cos(rot)};
        //-Direction Facing Vector X
        inputs[index++] = vectFacing[0];

        //-Direction Facing Vector Y
        inputs[index++] = vectFacing[1];

        /*
         Possible Input to Try
         */
        //-Net Force Vector X
        //-Net Force Vector Y
        return inputs;
    }
}
