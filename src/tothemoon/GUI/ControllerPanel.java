/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tothemoon.GUI;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import tothemoon.MyAction;
import tothemoon.genetics.Chromosome;
import tothemoon.simulation.GalaxySimulation;
import tothemoon.simulation.GenerationNotifier;
import tothemoon.simulation.GeneticGalaxySimulation;
import tothemoon.simulation.GetBestNCompleteAction;
import tothemoon.simulation.InvalidNumberOfThreadsException;
import tothemoon.simulation.SelectiveRocketViewerSimulation;
import tothemoon.simulation.test.RocketTestSimulation;
import tothemoon.simulation.tothemoon.ToTheMoonSimulation;

/**
 *
 * @author Michael
 */
public class ControllerPanel extends javax.swing.JPanel implements KeyListener {

    /**
     * Creates new form Stats
     */
    HashMap<String, Double> vars = new HashMap<>();

    boolean wasRunning = false;

    int jumpCounter;

    GalaxySimulation currentSim;
    ArrayList<GalaxySimulation> sims;
    int simIndex;

    final MyAction toggleView;

    GenerationNotifier genNote;

    GalaxyView screen;

    public ControllerPanel(MyAction tv, GalaxyView v) throws FileNotFoundException {
        initComponents();

        jSplitPane1.setDividerLocation(350);

        this.statsTextArea.setEditable(false);
        this.statsTextArea.setDragEnabled(false);
        this.statsTextArea.setFocusable(false);
        sims = new ArrayList<>();

        this.toggleView = tv;
        screen = v;

        genNote = new GenerationNotifier() {
            @Override
            public void generationComplete(GalaxySimulation g) {
                genComplete(g);
            }
        };

        this.readVars();

        //Setup the vars field
        setupVarsField();

        //Setup the simulation combo box
//        this.simulationComboBox.removeAllItems();
//        this.simulationTypeComboBox.removeAllItems();
        //Fill the different types of simulation classes into the list
        this.simulationTypeComboBox.addItem(ToTheMoonSimulation.class);
        this.simulationTypeComboBox.addItem(RocketTestSimulation.class);
    }

    private void setupVarsField() {

        this.varPanel.setLayout(new BoxLayout(varPanel, BoxLayout.Y_AXIS));

        //Create a VariablePanel for each variable
        VariableUpdater varUpdate = new VariableUpdater() {

            @Override
            public void varUpdated(String var, double value) {
                vars.put(var, value);
            }

        };

        ArrayList<String> keys = new ArrayList<>(vars.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            VariablePanel var = new VariablePanel(key, vars.get(key), varUpdate);
            varPanel.add(var);
        }
    }

    public void genComplete(GalaxySimulation galaxy) {
        //Make sure the update is from the current galaxy
        if (galaxy != currentSim) {
            return;
        }

        statsTextArea.setText(this.currentSim.getStats().generateReport());

        //If jumps are enabled, handle it
        if (this.jumpCheckBox.isSelected()) {
            jumpCounter++;
            if (jumpCounter == (int) this.jumpSpinner.getModel().getValue()) {
                jumpCounter = 0;
                pause();
            }
        }
    }

    public void swap() {
        //Unpause the simulation
        this.wasRunning = currentSim.isRunning();
        this.currentSim.setViwerMode(true);
        this.toggleView.act();
        if (!currentSim.isRunning()) {
            resume();
        }
    }

    public void pause() {
        pauseButton.setText("Resume");
        this.currentSim.stop();
    }

    public void resume() {
        pauseButton.setText("Pause");
        this.currentSim.run();
    }

    private void readVars() throws FileNotFoundException {
        Scanner in = new Scanner(new File("startup.ini"));
        while (in.hasNextLine()) {
            String line = in.nextLine().trim();
            //Skip comments
            if (line.startsWith("-")) {
                continue;
            }
            //Parse by = sign
            String[] parts = line.split("=");

            //Skip invalid lines
            if (parts.length != 2) {
                continue;
            }

            //Parse the value to a double
            double value = Double.parseDouble(parts[1].trim());

            vars.put(parts[0].trim(), value);
        }
    }

    public void setSimulation(int index) {
        simIndex = index;
        currentSim = sims.get(index);
        currentSim.syncGalaxyView(screen);
        //Adjust the buttons
        if (currentSim.isRunning()) {
            this.pauseButton.setText("Pause");
        } else {
            this.pauseButton.setText("Resume");
        }
        //Set the stat board
        this.statsTextArea.setText(currentSim.getStats().generateReport());
        //Check if the sim is a genetic sim
        viewBestButton.setEnabled(currentSim instanceof GeneticGalaxySimulation);
    }

    public void enableButtons() {
        this.pauseButton.setEnabled(true);
        this.backToViewButton.setEnabled(true);
        this.renameSimButton.setEnabled(true);
        this.viewBestButton.setEnabled(true);
    }

    public void disableButtons() {
        this.pauseButton.setEnabled(false);
        this.backToViewButton.setEnabled(false);
        this.renameSimButton.setEnabled(false);
        this.viewBestButton.setEnabled(false);
    }

    //This is called when the view has been swaped back to this panel from
    //the simulation view
    public void swappedBack() {
        //If the sim wasnt running, pause it again
        if (!this.wasRunning) {
            pause();
        }
        currentSim.setViwerMode(false);
    }
    
    public void addNewSimulation(GalaxySimulation sim){
        if(sim == null){
            return;
        }
        //Enable all the buttons
        enableButtons();
        //Name the simulation
        sim.setName(sim.getClass().getSimpleName() + " " + sims.size());
        //Add the new simulation
        sims.add(sim);
        //Set the simulation
        this.setSimulation(sims.size() - 1);

        //Add the simulation to the combo box
        this.simulationComboBox.addItem(sim);
        this.simulationComboBox.setSelectedIndex(simulationComboBox.getItemCount() - 1);
        //Change the text of the pause button
        this.pauseButton.setText("Start");
    }
    
    private GalaxySimulation createNewSimulation(Class simClass){
        
        
        GalaxySimulation sim = null;
        try {
            sim = (GalaxySimulation) simClass.newInstance();
            sim.setup(vars);
            sim.setGenerationNotifier(genNote);
        } catch (InvalidNumberOfThreadsException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ControllerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sim;
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        backToViewButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        varPanel = new javax.swing.JPanel();
        createNewSimulationButton = new javax.swing.JButton();
        simulationComboBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jumpCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jumpSpinner = new javax.swing.JSpinner();
        renameSimButton = new javax.swing.JButton();
        simulationTypeComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        viewBestSpinner = new javax.swing.JSpinner();
        viewBestButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        statsTextArea = new javax.swing.JTextArea();

        jPanel1.setEnabled(false);

        backToViewButton.setText("Back to View");
        backToViewButton.setEnabled(false);
        backToViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backToViewButtonActionPerformed(evt);
            }
        });

        pauseButton.setText("Start");
        pauseButton.setEnabled(false);
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Simulation Variables");

        javax.swing.GroupLayout varPanelLayout = new javax.swing.GroupLayout(varPanel);
        varPanel.setLayout(varPanelLayout);
        varPanelLayout.setHorizontalGroup(
            varPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 318, Short.MAX_VALUE)
        );
        varPanelLayout.setVerticalGroup(
            varPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 212, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(varPanel);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE))
        );

        createNewSimulationButton.setText("Create New Simulation");
        createNewSimulationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNewSimulationButtonActionPerformed(evt);
            }
        });

        simulationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simulationComboBoxActionPerformed(evt);
            }
        });

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setText("Current Simulation:");

        jumpCheckBox.setText("Enable Jumps");
        jumpCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jumpCheckBoxActionPerformed(evt);
            }
        });

        jLabel1.setText("Pause Every");

        jLabel2.setText("Generations");

        jumpSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jumpCheckBox)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jumpSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jumpSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jumpCheckBox))
        );

        renameSimButton.setText("Rename Simulation");
        renameSimButton.setEnabled(false);
        renameSimButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameSimButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("View Best");

        viewBestSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        viewBestButton.setText("View Best");
        viewBestButton.setEnabled(false);
        viewBestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewBestButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(pauseButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(backToViewButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(viewBestSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(viewBestButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(simulationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(17, 17, 17))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(renameSimButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(createNewSimulationButton, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(simulationTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(pauseButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(backToViewButton))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel5)
                                        .addComponent(viewBestSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(simulationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(renameSimButton)
                                    .addComponent(viewBestButton))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(simulationTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(createNewSimulationButton)))
                .addContainerGap(243, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(jPanel1);

        statsTextArea.setColumns(20);
        statsTextArea.setRows(5);
        jScrollPane1.setViewportView(statsTextArea);

        jSplitPane1.setLeftComponent(jScrollPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void backToViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backToViewButtonActionPerformed
        swap();
    }//GEN-LAST:event_backToViewButtonActionPerformed

    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
        if (!currentSim.isRunning()) {
            resume();
        } else {
            pause();
        }
    }//GEN-LAST:event_pauseButtonActionPerformed

    private void jumpCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jumpCheckBoxActionPerformed
        jumpCounter = 0;
    }//GEN-LAST:event_jumpCheckBoxActionPerformed

    private void createNewSimulationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNewSimulationButtonActionPerformed
        Class simClass = (Class) this.simulationTypeComboBox.getSelectedItem();
        this.addNewSimulation(this.createNewSimulation(simClass));
    }//GEN-LAST:event_createNewSimulationButtonActionPerformed

    private void simulationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simulationComboBoxActionPerformed
        //A new simulation has been chosen, set it to the current sim
        int index = sims.indexOf(this.simulationComboBox.getSelectedItem());
        if (index >= 0) {
            this.setSimulation(index);
        }
    }//GEN-LAST:event_simulationComboBoxActionPerformed

    private void renameSimButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameSimButtonActionPerformed
        String name = JOptionPane.showInputDialog(this, "Rename: ");
        currentSim.setName(name);
        this.simulationComboBox.repaint();
    }//GEN-LAST:event_renameSimButtonActionPerformed

    private void viewBestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewBestButtonActionPerformed
        int bestToGet = (int) this.viewBestSpinner.getValue();
        //Get the rockets and copy them
        GeneticGalaxySimulation sim = (GeneticGalaxySimulation)currentSim;
        sim.getBestNEntities(bestToGet, new GetBestNCompleteAction(){

            @Override
            public void act(ArrayList<Chromosome> best) {
                //Create the sim
                SelectiveRocketViewerSimulation sim = new SelectiveRocketViewerSimulation();
                sim.setChromosomes(best);
                try {
                    sim.setup(vars);
                } catch (InvalidNumberOfThreadsException ex) {
                    Logger.getLogger(ControllerPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                //Pause the current sim
                currentSim.stop();
                //Add the sim
                addNewSimulation(sim);
                //Switch to the sim view
                swap();
            }
            
        });
    }//GEN-LAST:event_viewBestButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backToViewButton;
    private javax.swing.JButton createNewSimulationButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JCheckBox jumpCheckBox;
    private javax.swing.JSpinner jumpSpinner;
    private javax.swing.JButton pauseButton;
    private javax.swing.JButton renameSimButton;
    private javax.swing.JComboBox simulationComboBox;
    private javax.swing.JComboBox simulationTypeComboBox;
    private javax.swing.JTextArea statsTextArea;
    private javax.swing.JPanel varPanel;
    private javax.swing.JButton viewBestButton;
    private javax.swing.JSpinner viewBestSpinner;
    // End of variables declaration//GEN-END:variables

    @Override
    public void keyTyped(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_F) {
            swap();
        }
    }

    @Override
    public void keyPressed(KeyEvent ke) {
    }

    @Override
    public void keyReleased(KeyEvent ke) {
    }
}

class VariablePanel extends JPanel {

    JLabel label;
    JTextField valueField;
    double value;
    VariableUpdater update;

    public VariablePanel(String var, double val, VariableUpdater up) {
        this.valueField = new JTextField(val + "");
        value = val;
        valueField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    //Try to parse the input
                    value = Double.parseDouble(valueField.getText());
                    update.varUpdated(label.getText(), value);
                    valueField.setForeground(Color.GREEN);
                    Timer time = new Timer();
                    time.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            valueField.setForeground(Color.BLACK);
                        }
                    }, 1000);
                } catch (NumberFormatException ex) {
                    //If not a double format, revert to previous value
                    valueField.setText(value + "");
                    //Set the text to red for one second to indicate an error
                    valueField.setForeground(Color.RED);
                    Timer time = new Timer();
                    time.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            valueField.setForeground(Color.BLACK);
                        }
                    }, 1000);
                }
            }

        });
        label = new JLabel(var);
        this.add(label);
        this.add(valueField);
        update = up;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    }

}
