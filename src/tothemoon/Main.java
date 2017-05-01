package tothemoon;

import tothemoon.GUI.ControllerPanel;
import tothemoon.GUI.GalaxyView;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.FileNotFoundException;
import javax.swing.JFrame;
import javax.swing.JPanel;

//@author Michael Haertling
public class Main {
    

    //Basic function variables
    boolean viewerMode = false;
    JFrame frame;
    ControllerPanel statsPanel;
    CardLayout layout;
    JPanel mainPanel;
    GalaxyView screen;

    public Main() throws FileNotFoundException {
        //Setup the simulation
        setup();
    }

    public static void main(String[] args) throws FileNotFoundException {
        Main main = new Main();
        //Begin running the simulation
        main.run();
    }

    private void setup() throws FileNotFoundException {
        //Create the GUI
        frame = new JFrame("To The Moon");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MyAction toggle = new MyAction(){

            @Override
            public void act() {
                toggleViewerMode();
            }
            
        };
        screen = new GalaxyView(toggle);
        
        statsPanel = new ControllerPanel(toggle,screen);
        mainPanel = new JPanel();
        layout = new CardLayout();
        mainPanel.setLayout(layout);
        frame.add(mainPanel);
        mainPanel.add(statsPanel, "stats");
        mainPanel.add(screen, "screen");
        screen.addKeyListener(screen);
//        frame.addKeyListener(statsPanel);
        frame.addMouseListener(screen);
        frame.addMouseWheelListener(screen);
        frame.addMouseMotionListener(screen);
        
        //Add actions to control panel
        

        //Find the maximum screen size that can be used
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(screen.getGraphicsConfiguration());
        int taskBarSize = scnMax.bottom;
        int side = (int) Math.min(screenSize.getWidth(), screenSize.getHeight() - taskBarSize);

        //Set the frame size
        frame.setSize(side, side);
        


    }

    public void run(){
        frame.setVisible(true);
    }

    public void toggleViewerMode() {
        if (viewerMode) {
            viewerMode = false;
            //Switch to status board view
            layout.show(mainPanel, "stats");
            statsPanel.requestFocus();
            statsPanel.swappedBack();
        } else {
            viewerMode = true;
            //Switch to rocket overview
            layout.show(mainPanel, "screen");
            screen.requestFocus();
        }
    }

    

}
