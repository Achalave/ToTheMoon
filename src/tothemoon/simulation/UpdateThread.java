package tothemoon.simulation;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import tothemoon.VectorMath;

//@author Michael Haertling
public abstract class UpdateThread implements Runnable {

    private final ArrayList<Entity> entities;
    private final ArrayList<Planet> planets;

    long elapsedTimeInSimulation;
    final long simulationTime;
    final long updateTime;
    int remainingEntities;

    private volatile boolean threadSuspended = false;
    private boolean run;
    private boolean generationComplete;

    Thread thread;

    public UpdateThread(ArrayList<Planet> planets, long simulationTime, long updateTime) {
        entities = new ArrayList<>();
        this.planets = planets;
        this.simulationTime = simulationTime;
        this.updateTime = updateTime;
    }

    //This is run when start is called
    @Override
    public void run() {
        run = true;
        while (run) {
            doFullGeneration();
            //Generation complete
            finalizeGeneration();
            suspendThread();
        }
    }

    public void doFullGeneration() {
        //Update until generation is complete
        while (!generationComplete) {
            update(updateTime);
        }
    }

    public void suspendThread() {
        if (thread != null && run) {
            final Thread t = thread;
            //Go into suspended state
            threadSuspended = true;
            while (threadSuspended) {
                try {
                    synchronized (t) {
                        t.wait();
                    }
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    public void finalizeGeneration() {
        for (Entity e : entities) {
            applyFitness(e);
        }
        remainingEntities = entities.size();
        elapsedTimeInSimulation = 0;
        generationComplete = false;
    }

    /**
     *
     * @param elapsedTime
     */
    public void update(long elapsedTime) {
        boolean finished = true;
        //Make sure the simulation still has time left
        if (elapsedTimeInSimulation < simulationTime) {
            //Iterate through each rocket
            for (Entity e : entities) {
                //Only apply updates if the rocket is not crashed
                if (!e.isCrashed()) {
                    finished = false;
                    update(e, elapsedTime);
                }
            }
            //Increment Generation Elapsed Time
            elapsedTimeInSimulation += elapsedTime;
        } //The time is up, complete the generation
        else {
            generationComplete = true;
            return;
        }
        //If all entities are crashed, the generation is done
        generationComplete = finished;

    }

    public void update(Entity e, long elapsedTime) {
        //Apply forces
        applyAllForces(e, elapsedTime);
        //Adjust velocities
        this.adjustVelocitiesForCollissions(e);
        //Update position
        e.updatePosition(elapsedTime);
    }

    public void adjustVelocitiesForCollissions(Entity e) {
        Planet p = e.getCollsionPlanet();
        if (p != null) {
            double dx = e.getX() - p.getX();
            double dy = e.getY() - p.getY();

            if ((dx > 0 && e.getVelocityX() < 0) || dx < 0 && e.getVelocityX() > 0) {
                e.setVelocityX(0);
            }

            if ((dy > 0 && e.getVelocityY() < 0) || dy < 0 && e.getVelocityY() > 0) {
                e.setVelocityY(0);
            }
        }
    }

    public void applyAllForces(Entity e, long elapsedTime) {
        //Reset the collisionPlanet
        e.setCollisionPlanet(null);

        //Check if the entity has just crashed or landed
        for (Planet p : planets) {
            if (p.getColission(e)) {
                //Inform the entity of the collision
                e.setCollisionPlanet(p);
                //Get angle between planet and entity
                //Get angle between planet and entity
                double dx = e.getCenterX() - p.getX();
                double dy = e.getCenterY() - p.getY();
                double perfectAngle = VectorMath.boundRotation(Math.atan2(dx, dy));
                double actualAngle = Math.PI - e.getRotation();
                actualAngle = VectorMath.boundRotation(actualAngle);
                //If the angle is too high, the entity has crashed into the planet
                if ((Math.abs(perfectAngle - actualAngle)) > e.getAcceptedLandingVariance()) {
                    e.setCrashed(true);
                    remainingEntities--;
                } //Otherwise, the entity has landed

            }
        }
        //If there is no collision, apply gravity
        if (e.getCollsionPlanet() == null) {
            applyGravity(e, elapsedTime);
        }
    }

    public void applyGravity(Entity e, long elapsedTime) {
        //Apply gravity from the planet
        for (Planet p : planets) {
            double grav = p.calculateGravity(e);
            e.applyForceFrom(grav, p.getX(), p.getY(), elapsedTime);
        }
    }

    public ArrayList<Entity> getEnitites() {
        return entities;
    }

    public void addEntity(Entity e) {
        entities.add(e);
        this.remainingEntities++;
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public ArrayList<Planet> getPlanets() {
        return planets;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public boolean threadSuspended() {
        return this.threadSuspended;
    }

    public int getRemainingEntities() {
        return remainingEntities;
    }

    public void resumeThread() {
        if (thread != null && threadSuspended) {
            final Thread t = this.thread;
            threadSuspended = false;
            synchronized (t) {
                t.notify();
            }
        }
    }

    public void stopThread() {
        run = false;
        resumeThread();
    }

    public boolean generationIsComplete() {
        return this.generationComplete;
    }

    public long getElapsedTimeInSimulation() {
        return elapsedTimeInSimulation;
    }

    public long getSimulationTime() {
        return simulationTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public boolean isGenerationComplete() {
        return generationComplete;
    }

    public abstract void applyFitness(Entity e);

}
