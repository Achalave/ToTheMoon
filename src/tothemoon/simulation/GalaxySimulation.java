package tothemoon.simulation;

import tothemoon.simulation.tothemoon.ToTheMoonUpdateThread;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import tothemoon.GUI.GalaxyView;
import tothemoon.Main;
import tothemoon.VectorMath;

//@author Michael Haertling
public abstract class GalaxySimulation {

    public final ArrayList<Planet> planets;
    public final ArrayList<Entity> entities;

    /*
     Variables
     */
    String name;

    //Final variables
    long updateWaitTime;
    long viewWaitTime;
    long simulationTime;

    //Basic function variables
    private boolean running = false;
    protected long elapsedTimeInGeneration = 0;
    protected boolean viewerMode = false;
    protected GalaxyView screen;

    protected long totalRunningTime;

    GenerationNotifier genNotifier;

    //Store the last used view variables
    double viewX = 0;
    double viewY = 0;
    double zoom = 1;

    Thread[] threads;
    UpdateThread[] updaters;
    boolean threadsRunning = false;

    public GalaxySimulation() {
        planets = new ArrayList<>();
        entities = new ArrayList<>();
    }

    public void setup(HashMap<String, Double> vars) throws InvalidNumberOfThreadsException {
        //Set the final variables
        updateWaitTime = vars.get("WaitTime").longValue();
        simulationTime = vars.get("SimulationTime").longValue() * 1000;
        viewWaitTime = (long) ((1 / vars.get("FPS")) * 1000);
        setupThreads(vars);
    }

    private void setupThreads(HashMap<String, Double> vars) throws InvalidNumberOfThreadsException {
        int numThreads = getNumThreads();
        ArrayList<Entity> ents = getEntities();
        if (numThreads > ents.size() || numThreads <= 0) {
            throw new InvalidNumberOfThreadsException();
        }

        if (numThreads > 1) {
            threads = new Thread[numThreads - 1];
        }
        updaters = new UpdateThread[numThreads];

        //Create the updaters
        for (int i = 0; i < updaters.length; i++) {
            updaters[i] = createNewUpdateThread(getPlanets(), simulationTime, updateWaitTime);
        }

        //Fill them with entities
        int entsPer = ents.size() / numThreads;
        int entIndex = 0;
        for (UpdateThread up : updaters) {
            for (int i = 0; i < entsPer; i++) {
                up.addEntity(ents.get(entIndex++));
                if (entIndex == ents.size()) {
                    break;
                }
            }
        }
    }

    public abstract UpdateThread createNewUpdateThread(ArrayList<Planet> planets, long simulationTime, long updateWaitTime);

    public void setGenerationNotifier(GenerationNotifier gn) {
        genNotifier = gn;
    }

    public abstract int getNumThreads();

    public void addPlanet(Planet p) {
        planets.add(p);
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public abstract StatsForm getStats();

    public abstract void updateStatsForm();

    public void run() {
        //Don't start two run loops
        if (running) {
            return;
        }

        //Start up the main thread
        running = true;
        new Thread() {
            long lastTime;
            long elapsedTime;
            long builtTime;

            @Override
            public void run() {
                while (running) {
                    lastTime = System.currentTimeMillis();
                    if (viewerMode) {
                        boolean complete = false;
                        //Make sure the threads are not running
                        if (threadsRunning) {
                            //Shut down the threads
                            stopThreads();
                        }
                        //Let time pass

                        while ((elapsedTime = System.currentTimeMillis() - lastTime) < viewWaitTime) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        builtTime += elapsedTime;
                        //Update Everything in increments of wait time
                        while (builtTime > updateWaitTime) {
                            manualUpdate(updateWaitTime);

                            builtTime -= updateWaitTime;
                            //Increment my elapsed generation time
                            elapsedTimeInGeneration += updateWaitTime;

                            complete = threadsComplete();
                            if (complete) {
                                break;
                            }
                        }

                        //Find the number of rockets remaining
                        int rocketsRemaining = 0;
                        for (UpdateThread up : updaters) {
                            rocketsRemaining += up.getRemainingEntities();
                        }
                        //Apply the stats to the view if in need of update
                        if (screen != null) {
                            updateScreenStats(rocketsRemaining);
                        }

                        //Draw Everything
                        draw();

                        //Check if the generation has been completed
                        if (complete) {
                            for (UpdateThread up : updaters) {
                                up.finalizeGeneration();
                            }
                            startNextCycle();
                            builtTime = 0;
                        }
                    } //The program is in fast mode
                    else {
                        //If threads are not running, start them
                        if (!threadsRunning) {
                            threadsRunning = true;
                            startThreads();
                        } //Otherwise un-suspend them
                        else {
                            resumeThreads();
                        }
                        //Run the final updater on this thread
                        updaters[updaters.length - 1].doFullGeneration();
                        //Run the finalization method
                        updaters[updaters.length - 1].finalizeGeneration();
                        //Wait for the other threads to be suspended
                        while (!threadsSuspended()) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(GalaxySimulation.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        startNextCycle();
                        //Increment the total running time
                        totalRunningTime += System.currentTimeMillis() - lastTime;
                    }
                }
            }
        }.start();
    }

    public void startThreads() {
        //If there is only one updater, don't make threads
        if (threads == null) {
            return;
        }
        //Start up all the threads
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(updaters[i]);
            updaters[i].setThread(threads[i]);
            threads[i].start();
        }
        threadsRunning = true;
    }

    /**
     * Calls the resume method for each updater thread, releasing each one from
     * suspension
     */
    public void resumeThreads() {
        //Resume all the threads
        for (UpdateThread up : updaters) {
            if (up.threadSuspended()) {
                up.resumeThread();
            }
        }
    }

    /**
     * Calls the stop method for every updater thread and resumes them
     */
    public void stopThreads() {
        //Stop all the threads
        for (UpdateThread up : updaters) {
            up.stopThread();
        }
        threadsRunning = false;
    }

    /**
     *
     * @return true if all threads have completed their generation simulation
     */
    public boolean threadsComplete() {
        for (UpdateThread up : updaters) {
            if (!up.generationIsComplete()) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @return true if all but the last updater thread has been suspended
     */
    public boolean threadsSuspended() {
        for (int i = 0; i < updaters.length - 1; i++) {
            if (!updaters[i].threadSuspended()) {
                return false;
            }
        }
        return true;
    }

    /**
     * To be used to do a single update manually. This is used when in viewer
     * mode.
     *
     * @param timeElapsed The time elapsed since the last call
     */
    protected void manualUpdate(long timeElapsed) {
        //Iterate through all the threads and call update manually
        for (UpdateThread up : updaters) {
            if (!up.generationIsComplete()) {
                up.update(timeElapsed);
            }
        }
    }

    /**
     * Draws everything to the designated view
     */
    public void draw() {
        //Draw all the rockets and such
        if (viewerMode) {
            Graphics2D g = screen.getBufferGraphics();
            for (Planet p : planets) {
                p.draw(g, -screen.getViewX(), -screen.getViewY(), screen.getZoom());
            }

            for (Entity r : entities) {
                r.draw(g, -screen.getViewX(), -screen.getViewY(), screen.getZoom());
            }
            screen.repaint();
        }
    }

    /**
     * Updates the screen stats
     *
     * @param rocketsRemaining The number of non-crashed/landed entities
     */
    public abstract void updateScreenStats(int rocketsRemaining);

    public void startNextCycle() {
        elapsedTimeInGeneration = 0;
        if (genNotifier != null) {
            this.genNotifier.generationComplete(this);
        }
        this.updateStatsForm();
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    //This is called when the view mode has changed
    public void setViwerMode(boolean mode) {
        viewerMode = mode;
        //If now in screen mode, set the variables back to what they were
        if (mode) {
            screen.setZoom(zoom);
            screen.setViewPosition(viewX, viewY);
        } //Otherwise, store them
        else {
            zoom = screen.getZoom();
            viewX = screen.getViewX();
            viewY = screen.getViewY();
        }
    }

    public void syncGalaxyView(GalaxyView v) {
        screen = v;
    }

    public void setName(String n) {
        name = n;
    }

    public ArrayList<Planet> getPlanets() {
        return planets;
    }

    public double getViewX() {
        return viewX;
    }

    public void setViewX(double viewX) {
        this.viewX = viewX;
    }

    public double getViewY() {
        return viewY;
    }

    public void setViewY(double viewY) {
        this.viewY = viewY;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public long getUpdateWaitTime() {
        return updateWaitTime;
    }

    public boolean isViewerMode() {
        return viewerMode;
    }

    public boolean isThreadsRunning() {
        return threadsRunning;
    }

    protected UpdateThread[] getUpdaters() {
        return updaters;
    }

    protected GalaxyView getScreen() {
        return screen;
    }

    public long getViewWaitTime() {
        return viewWaitTime;
    }

    public int getNumEntities() {
        return entities.size();
    }

    @Override
    public String toString() {
        return name;
    }
}
