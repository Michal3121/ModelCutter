/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import com.jogamp.opengl.util.FPSAnimator;
import com.vividsolutions.jts.geom.Coordinate;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import static java.awt.event.MouseEvent.BUTTON1;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Point3f;

/**
 *
 * @author MICHAL
 */
public class GUI extends javax.swing.JFrame {
    
    private final GLCanvas glCanvas; // lepsia hardverova akceleracia ako pri GL panelu
    private final GLProfile glprofile; //urcuje verziu OpenGL
    private Renderer renderer; 
    private final FPSAnimator animator; // urcuje kolkokrat za sekundu sa vykresli obraz
    private static final int FPS = 60; //60
    private final Aplication app;
    
    private final ModelManagerImpl modelManager;
    private List<Model> models;
    private Model newModel;
    private Point3f planeCenter;
   
    /**
     * Creates new form MainGUI
     */ 
    public GUI() {
        super.setTitle("ModelCutter");
        initComponents();
        initMenu();
        
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });  
        
        setLocationRelativeTo(null); // vykreslovanie do stredu obrazovky
        
        //-------------------------------------
        models = new ArrayList<>();
        modelManager = new ModelManagerImpl();
      
        
        glprofile = GLProfile.get(GLProfile.GL2); // urcenie verziu OpenGL 2.0
        glCanvas = new GLCanvas(new GLCapabilities(glprofile)); // objekt verzie OpenGL
        openGlPanel.add(glCanvas);
        
        glCanvas.addMouseMotionListener(new MouseMotionListener(){

            @Override
            public void mouseDragged(MouseEvent e) {
                //if(e.isAltDown()){ funguje zvlastne - pri stlacenom kolisku sa vykona
                    renderer.setMouseX(e.getX());
                    renderer.setMouseY(e.getY()); 
                //}    
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        
        });
        
        glCanvas.addMouseWheelListener(new MouseWheelListener(){
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                renderer.setMouseZoom(e.getPreciseWheelRotation());
            }
        });
        
        glCanvas.addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON2){
                    renderer.setMiddleMouseButtonPressed(true); 
                }
                
                if(e.getButton() == MouseEvent.BUTTON1){
                    renderer.setLeftMouseButtonPressed(true);
                }
                
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                renderer.setMiddleMouseButtonPressed(false);
                renderer.setLeftMouseButtonPressed(false);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        
        });
        
        glCanvas.addKeyListener(new KeyListener(){

            @Override
            public void keyTyped(KeyEvent e) {
              
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ALT){
                    //renderer.setAltPressed(true);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ALT){
                    //renderer.setAltPressed(false);
                }
            }
        
        });
        
        
        renderer = new Renderer(openGlPanel.getWidth(), openGlPanel.getHeight()); 
        app = new Aplication();
        
        
        animator = new FPSAnimator(glCanvas, FPS, true);
        animator.start();      
        
        glCanvas.setSize(openGlPanel.getWidth(), openGlPanel.getHeight());
        //System.out.println("Rozmery canvasu width: " + openGlPanel.getWidth() + " height: " + openGlPanel.getHeight());
        
        //setActions();
        glCanvas.addGLEventListener(renderer);
        //glPanel.addKeyListener(openGlJPanel);
        glCanvas.requestFocusInWindow();
        
    }
    
    private void initMenu(){
        
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        
        JMenuItem saveAsAction = new JMenuItem("Save As...");
        JMenuItem loadModel = new JMenuItem("Open File...");
        JMenuItem exitAction = new JMenuItem("Exit");
        
        JMenuItem about = new JMenuItem("About");
        
        //file.add(saveAction);
        file.add(saveAsAction);
        file.add(loadModel);
        file.addSeparator();
        
        file.add(exitAction);
        
        help.add(about);
        
        //setDefaultLightWeightPopupEnabled(false);
        //saveAsAction.setDefaultLightWeightPopupEnabled(false);
        
        
        //JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        //ToolTipManager.sharedInstance().setLight WeightPopupEnabled(false);
        
        loadModel.addActionListener(new ActionListener(){
            
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser openingChooser = new JFileChooser(); 
                FileNameExtensionFilter stl = new FileNameExtensionFilter("STL Files (*.stl;*.STL)", "stl", "STL");
                
                openingChooser.addChoosableFileFilter(stl);   
                openingChooser.setFileFilter(stl);
                
                int openValue = openingChooser.showOpenDialog(null);
                                
                if(openValue == JFileChooser.CANCEL_OPTION){
                    return;
                }
               
                if(openValue == JFileChooser.APPROVE_OPTION){
                    models.clear();
                    newModel = modelManager.loadModel(new File(openingChooser.getSelectedFile().getAbsolutePath()));
                    renderer = new Renderer(newModel, openGlPanel.getWidth(), openGlPanel.getHeight());
                    glCanvas.addGLEventListener(renderer);
                    
                    float planeX = newModel.getModelCenter().x;
                    float planeY = newModel.getModelCenter().y;
                    float planeZ = newModel.getModelCenter().z;
                    
                    planeCenter = new Point3f(planeX, planeY, planeZ);  
                    renderer.setPlane(new Plane(planeCenter));
                }     
            }  
        });
        
        saveAsAction.addActionListener(new ActionListener(){ 

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser savingChooser = new JFileChooser(); 
                FileNameExtensionFilter stl = new FileNameExtensionFilter("STL Files (*.stl;*.STL)", "stl", "STL");
                
                savingChooser.addChoosableFileFilter(stl);   
                savingChooser.setFileFilter(stl);
                
                int saveValue = savingChooser.showSaveDialog(null);
                
                if(saveValue == JFileChooser.CANCEL_OPTION){
                    return;
                }
                
                if(saveValue == JFileChooser.APPROVE_OPTION){
                    
                    System.out.println("Cesta " + savingChooser.getSelectedFile().getAbsolutePath());
                    modelManager.exportModel(new File(savingChooser.getSelectedFile().getAbsolutePath() + ".stl"), models.get(0));
                
                }
            }
        
        });
            
        
        
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        leftPanel = new javax.swing.JPanel();
        ListOfModelsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        informationPanel = new javax.swing.JPanel();
        infoLeftPanel = new javax.swing.JPanel();
        widthLabel = new javax.swing.JLabel();
        heightLabel = new javax.swing.JLabel();
        lengthLabel = new javax.swing.JLabel();
        widthValueLabel = new javax.swing.JLabel();
        heightValueLabel = new javax.swing.JLabel();
        lengthValueLabel = new javax.swing.JLabel();
        infoRightPanel = new javax.swing.JPanel();
        trianglesLabel = new javax.swing.JLabel();
        pointLabel = new javax.swing.JLabel();
        pointsCountLabel = new javax.swing.JLabel();
        trianglesCountLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        modelNameLabel = new javax.swing.JLabel();
        planePanel = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        typeOfPlanePanel = new javax.swing.JPanel();
        circleCheckBox = new javax.swing.JCheckBox();
        sqareCheckBox = new javax.swing.JCheckBox();
        rectangleCheckBox = new javax.swing.JCheckBox();
        positionOfPlanePanel = new javax.swing.JPanel();
        xPlanePositionSlider = new javax.swing.JSlider();
        yPlanePositionSlider = new javax.swing.JSlider();
        zPlanePositionSlider = new javax.swing.JSlider();
        xPlanePositionLabel = new javax.swing.JLabel();
        yPlanePositionLabel = new javax.swing.JLabel();
        zPlanePositionLabel = new javax.swing.JLabel();
        xPlanePositionSpinner = new javax.swing.JSpinner();
        yPlanePositionSpinner = new javax.swing.JSpinner();
        zPlanePositionSpinner = new javax.swing.JSpinner();
        rotationOfPlanePanel = new javax.swing.JPanel();
        xPlaneRotationSlider = new javax.swing.JSlider();
        yPlaneRotationSlider = new javax.swing.JSlider();
        zPlaneRotationSlider = new javax.swing.JSlider();
        xPlaneRotationLabel = new javax.swing.JLabel();
        yPlaneRotationLabel = new javax.swing.JLabel();
        zPlaneRotationLabel = new javax.swing.JLabel();
        xPlaneRotationSpinner = new javax.swing.JSpinner();
        yPlaneRotationSpinner = new javax.swing.JSpinner();
        zPlaneRotationSpinner = new javax.swing.JSpinner();
        sizeOfPlanePanel = new javax.swing.JPanel();
        rPlaneSizeSlider = new javax.swing.JSlider();
        xPlaneSizeSlider = new javax.swing.JSlider();
        zPlaneSizeSlider = new javax.swing.JSlider();
        rPlaneSizeLabel = new javax.swing.JLabel();
        xPlaneSizeLabel = new javax.swing.JLabel();
        zPlaneSizeLabel = new javax.swing.JLabel();
        rPlaneSizeSpinner = new javax.swing.JSpinner();
        xPlaneSizeSpinner = new javax.swing.JSpinner();
        yPlaneSizeSpinner = new javax.swing.JSpinner();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        openGlPanel = new javax.swing.JPanel();
        projection2DPanel = new javax.swing.JPanel();
        statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        toolBar = new javax.swing.JToolBar();
        cutButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        file = new javax.swing.JMenu();
        edit = new javax.swing.JMenu();
        help = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        leftPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        ListOfModelsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("List of 3D models"));

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        javax.swing.GroupLayout ListOfModelsPanelLayout = new javax.swing.GroupLayout(ListOfModelsPanel);
        ListOfModelsPanel.setLayout(ListOfModelsPanelLayout);
        ListOfModelsPanelLayout.setHorizontalGroup(
            ListOfModelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ListOfModelsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        ListOfModelsPanelLayout.setVerticalGroup(
            ListOfModelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ListOfModelsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        informationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Model information"));

        widthLabel.setText("Width:");

        heightLabel.setText("Height:");

        lengthLabel.setText("Length:");

        widthValueLabel.setText("jLabel1");

        heightValueLabel.setText("jLabel2");

        lengthValueLabel.setText("jLabel3");

        javax.swing.GroupLayout infoLeftPanelLayout = new javax.swing.GroupLayout(infoLeftPanel);
        infoLeftPanel.setLayout(infoLeftPanelLayout);
        infoLeftPanelLayout.setHorizontalGroup(
            infoLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoLeftPanelLayout.createSequentialGroup()
                .addGroup(infoLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(infoLeftPanelLayout.createSequentialGroup()
                        .addComponent(lengthLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lengthValueLabel))
                    .addGroup(infoLeftPanelLayout.createSequentialGroup()
                        .addGroup(infoLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(widthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(heightLabel))
                        .addGap(5, 5, 5)
                        .addGroup(infoLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(heightValueLabel)
                            .addComponent(widthValueLabel))))
                .addGap(0, 34, Short.MAX_VALUE))
        );
        infoLeftPanelLayout.setVerticalGroup(
            infoLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoLeftPanelLayout.createSequentialGroup()
                .addGroup(infoLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(widthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(widthValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(infoLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(heightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(heightValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(infoLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lengthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lengthValueLabel)))
        );

        trianglesLabel.setText("Triangles:");

        pointLabel.setText("Points:");

        pointsCountLabel.setText("jLabel4");

        trianglesCountLabel.setText("jLabel5");

        javax.swing.GroupLayout infoRightPanelLayout = new javax.swing.GroupLayout(infoRightPanel);
        infoRightPanel.setLayout(infoRightPanelLayout);
        infoRightPanelLayout.setHorizontalGroup(
            infoRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoRightPanelLayout.createSequentialGroup()
                .addGroup(infoRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(infoRightPanelLayout.createSequentialGroup()
                        .addComponent(trianglesLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                        .addComponent(trianglesCountLabel))
                    .addGroup(infoRightPanelLayout.createSequentialGroup()
                        .addComponent(pointLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pointsCountLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        infoRightPanelLayout.setVerticalGroup(
            infoRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoRightPanelLayout.createSequentialGroup()
                .addGroup(infoRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pointLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pointsCountLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(infoRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trianglesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(trianglesCountLabel))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        nameLabel.setText("Model name:");

        modelNameLabel.setText("jLabel1");

        javax.swing.GroupLayout informationPanelLayout = new javax.swing.GroupLayout(informationPanel);
        informationPanel.setLayout(informationPanelLayout);
        informationPanelLayout.setHorizontalGroup(
            informationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(informationPanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(informationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(informationPanelLayout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modelNameLabel))
                    .addGroup(informationPanelLayout.createSequentialGroup()
                        .addComponent(infoLeftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(infoRightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        informationPanelLayout.setVerticalGroup(
            informationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, informationPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(informationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modelNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(informationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(informationPanelLayout.createSequentialGroup()
                        .addComponent(infoRightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, informationPanelLayout.createSequentialGroup()
                        .addComponent(infoLeftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        planePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Plane settings"));

        circleCheckBox.setText(" Circle");

        sqareCheckBox.setText(" Square");

        rectangleCheckBox.setText(" Rectangle");

        javax.swing.GroupLayout typeOfPlanePanelLayout = new javax.swing.GroupLayout(typeOfPlanePanel);
        typeOfPlanePanel.setLayout(typeOfPlanePanelLayout);
        typeOfPlanePanelLayout.setHorizontalGroup(
            typeOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typeOfPlanePanelLayout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addGroup(typeOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rectangleCheckBox)
                    .addComponent(sqareCheckBox)
                    .addComponent(circleCheckBox))
                .addContainerGap(110, Short.MAX_VALUE))
        );
        typeOfPlanePanelLayout.setVerticalGroup(
            typeOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typeOfPlanePanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(circleCheckBox)
                .addGap(18, 18, 18)
                .addComponent(sqareCheckBox)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(rectangleCheckBox)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Type", typeOfPlanePanel);

        xPlanePositionSlider.setPreferredSize(new java.awt.Dimension(150, 20));

        yPlanePositionSlider.setPreferredSize(new java.awt.Dimension(150, 20));

        zPlanePositionSlider.setPreferredSize(new java.awt.Dimension(150, 20));

        xPlanePositionLabel.setText("X:");
        xPlanePositionLabel.setPreferredSize(new java.awt.Dimension(10, 20));

        yPlanePositionLabel.setText("Y:");
        yPlanePositionLabel.setMaximumSize(new java.awt.Dimension(10, 20));
        yPlanePositionLabel.setMinimumSize(new java.awt.Dimension(10, 20));
        yPlanePositionLabel.setPreferredSize(new java.awt.Dimension(10, 20));

        zPlanePositionLabel.setText("Z:");
        zPlanePositionLabel.setMaximumSize(new java.awt.Dimension(10, 20));
        zPlanePositionLabel.setMinimumSize(new java.awt.Dimension(10, 20));
        zPlanePositionLabel.setPreferredSize(new java.awt.Dimension(10, 20));

        xPlanePositionSpinner.setMinimumSize(new java.awt.Dimension(45, 20));
        xPlanePositionSpinner.setPreferredSize(new java.awt.Dimension(45, 20));

        yPlanePositionSpinner.setPreferredSize(new java.awt.Dimension(30, 20));

        javax.swing.GroupLayout positionOfPlanePanelLayout = new javax.swing.GroupLayout(positionOfPlanePanel);
        positionOfPlanePanel.setLayout(positionOfPlanePanelLayout);
        positionOfPlanePanelLayout.setHorizontalGroup(
            positionOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(positionOfPlanePanelLayout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addGroup(positionOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, positionOfPlanePanelLayout.createSequentialGroup()
                        .addComponent(xPlanePositionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(xPlanePositionSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(positionOfPlanePanelLayout.createSequentialGroup()
                        .addComponent(yPlanePositionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(yPlanePositionSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(positionOfPlanePanelLayout.createSequentialGroup()
                        .addComponent(zPlanePositionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(zPlanePositionSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(6, 6, 6)
                .addGroup(positionOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(zPlanePositionSpinner)
                    .addComponent(yPlanePositionSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(xPlanePositionSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        positionOfPlanePanelLayout.setVerticalGroup(
            positionOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(positionOfPlanePanelLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(positionOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(xPlanePositionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xPlanePositionSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xPlanePositionSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(positionOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(yPlanePositionSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yPlanePositionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yPlanePositionSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(positionOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zPlanePositionSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zPlanePositionSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zPlanePositionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Position", positionOfPlanePanel);

        xPlaneRotationSlider.setMinimumSize(new java.awt.Dimension(36, 20));
        xPlaneRotationSlider.setPreferredSize(new java.awt.Dimension(150, 20));

        yPlaneRotationSlider.setPreferredSize(new java.awt.Dimension(150, 20));

        zPlaneRotationSlider.setPreferredSize(new java.awt.Dimension(150, 20));

        xPlaneRotationLabel.setText("α (x):");
        xPlaneRotationLabel.setMaximumSize(new java.awt.Dimension(28, 20));
        xPlaneRotationLabel.setMinimumSize(new java.awt.Dimension(10, 20));
        xPlaneRotationLabel.setPreferredSize(new java.awt.Dimension(28, 20));

        yPlaneRotationLabel.setText("β (y):");
        yPlaneRotationLabel.setMaximumSize(new java.awt.Dimension(28, 16));
        yPlaneRotationLabel.setMinimumSize(new java.awt.Dimension(28, 16));
        yPlaneRotationLabel.setPreferredSize(new java.awt.Dimension(10, 20));

        zPlaneRotationLabel.setText("γ (z):");
        zPlaneRotationLabel.setPreferredSize(new java.awt.Dimension(10, 20));

        xPlaneRotationSpinner.setMinimumSize(new java.awt.Dimension(35, 20));
        xPlaneRotationSpinner.setPreferredSize(new java.awt.Dimension(35, 20));

        yPlaneRotationSpinner.setMinimumSize(new java.awt.Dimension(35, 20));
        yPlaneRotationSpinner.setPreferredSize(new java.awt.Dimension(35, 20));

        zPlaneRotationSpinner.setMinimumSize(new java.awt.Dimension(35, 20));
        zPlaneRotationSpinner.setPreferredSize(new java.awt.Dimension(35, 20));

        javax.swing.GroupLayout rotationOfPlanePanelLayout = new javax.swing.GroupLayout(rotationOfPlanePanel);
        rotationOfPlanePanel.setLayout(rotationOfPlanePanelLayout);
        rotationOfPlanePanelLayout.setHorizontalGroup(
            rotationOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rotationOfPlanePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(rotationOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(yPlaneRotationLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zPlaneRotationLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xPlaneRotationLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(rotationOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rotationOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(yPlaneRotationSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(zPlaneRotationSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(xPlaneRotationSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(rotationOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(yPlaneRotationSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(zPlaneRotationSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(xPlaneRotationSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        rotationOfPlanePanelLayout.setVerticalGroup(
            rotationOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rotationOfPlanePanelLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(rotationOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(rotationOfPlanePanelLayout.createSequentialGroup()
                        .addGroup(rotationOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(xPlaneRotationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(xPlaneRotationSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(xPlaneRotationSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(rotationOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(yPlaneRotationSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yPlaneRotationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(yPlaneRotationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(rotationOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zPlaneRotationSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zPlaneRotationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zPlaneRotationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Rotation", rotationOfPlanePanel);

        rPlaneSizeSlider.setMinimumSize(new java.awt.Dimension(36, 20));
        rPlaneSizeSlider.setPreferredSize(new java.awt.Dimension(150, 20));

        xPlaneSizeSlider.setPreferredSize(new java.awt.Dimension(150, 20));

        zPlaneSizeSlider.setPreferredSize(new java.awt.Dimension(150, 20));

        rPlaneSizeLabel.setText("r:");
        rPlaneSizeLabel.setPreferredSize(new java.awt.Dimension(10, 20));

        xPlaneSizeLabel.setText("x:");

        zPlaneSizeLabel.setText("y:");

        rPlaneSizeSpinner.setMinimumSize(new java.awt.Dimension(45, 20));
        rPlaneSizeSpinner.setPreferredSize(new java.awt.Dimension(45, 20));

        javax.swing.GroupLayout sizeOfPlanePanelLayout = new javax.swing.GroupLayout(sizeOfPlanePanel);
        sizeOfPlanePanel.setLayout(sizeOfPlanePanelLayout);
        sizeOfPlanePanelLayout.setHorizontalGroup(
            sizeOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sizeOfPlanePanelLayout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addGroup(sizeOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(xPlaneSizeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(zPlaneSizeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(rPlaneSizeLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(sizeOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rPlaneSizeSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(sizeOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(zPlaneSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(xPlaneSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sizeOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(yPlaneSizeSpinner)
                    .addComponent(xPlaneSizeSpinner)
                    .addComponent(rPlaneSizeSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        sizeOfPlanePanelLayout.setVerticalGroup(
            sizeOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sizeOfPlanePanelLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(sizeOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rPlaneSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rPlaneSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rPlaneSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(sizeOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(xPlaneSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xPlaneSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xPlaneSizeLabel))
                .addGap(18, 18, 18)
                .addGroup(sizeOfPlanePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zPlaneSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yPlaneSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zPlaneSizeLabel))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Size", sizeOfPlanePanel);

        javax.swing.GroupLayout planePanelLayout = new javax.swing.GroupLayout(planePanel);
        planePanel.setLayout(planePanelLayout);
        planePanelLayout.setHorizontalGroup(
            planePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );
        planePanelLayout.setVerticalGroup(
            planePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(planePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2))
        );

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ListOfModelsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(informationPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(planePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(planePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ListOfModelsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(informationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );

        javax.swing.GroupLayout openGlPanelLayout = new javax.swing.GroupLayout(openGlPanel);
        openGlPanel.setLayout(openGlPanelLayout);
        openGlPanelLayout.setHorizontalGroup(
            openGlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 574, Short.MAX_VALUE)
        );
        openGlPanelLayout.setVerticalGroup(
            openGlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 412, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("3D scene", openGlPanel);

        javax.swing.GroupLayout projection2DPanelLayout = new javax.swing.GroupLayout(projection2DPanel);
        projection2DPanel.setLayout(projection2DPanelLayout);
        projection2DPanelLayout.setHorizontalGroup(
            projection2DPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 574, Short.MAX_VALUE)
        );
        projection2DPanelLayout.setVerticalGroup(
            projection2DPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 412, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("2D projection", projection2DPanel);

        statusPanel.setPreferredSize(new java.awt.Dimension(226, 16));

        statusLabel.setText("Loading...");

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(statusLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        toolBar.setRollover(true);

        cutButton.setText("Cut");
        cutButton.setFocusPainted(false);
        cutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cutButtonActionPerformed(evt);
            }
        });
        toolBar.add(cutButton);

        file.setText("File");
        jMenuBar1.add(file);

        edit.setText("Edit");
        jMenuBar1.add(edit);

        help.setText("Help");
        jMenuBar1.add(help);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addComponent(statusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(leftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cutButtonActionPerformed
        List<Point3f> ringList = new ArrayList<>();
        Set<Long> intersectionTriangles = new HashSet<>();
        List<List<Long>> allParts;
        
        Plane plane = new Plane(planeCenter);
        //app.setComponent(modelWithMap);
        intersectionTriangles.addAll(app.getAllIntersectionTriangles(newModel, plane));
        app.divideIntersectingTriangles(intersectionTriangles, newModel, plane);
        //app.divideModel(modelWithMap, plane);
        app.setComponent(newModel);
        //allParts = app.getListsOfParts(intersectionTriangles, modelWithMap);
        //app.getDividedTriangleFromRing(allParts, modelWithMap, plane);
        models.add(newModel); // docasne riesenie, kym nemame rozne modely
        renderer = new Renderer(models, openGlPanel.getWidth(), openGlPanel.getHeight());
        
        glCanvas.addGLEventListener(renderer);
        
        System.out.println("Center 2 Cube" + this.newModel.getModelCenter());
        System.out.println("Center Cube" + this.newModel.getModelCenter());
        
        //ringList.addAll(app.getTriangleRing(this.modelWithMap, plane));
        System.out.println("kliknutie" + ringList.toString() + "velkost kolekcie" + ringList.size());
        
        //renderer.setRingList(ringList);
        renderer.setPlane(plane);
    }//GEN-LAST:event_cutButtonActionPerformed
    
    /*
    private void formComponentResized(java.awt.event.ComponentEvent evt) {                                      
        glPanel.setSize(openGlPanel.getWidth(), openGlPanel.getHeight());
    }*/
    
    // udalost: zmena velkosti okna (events -> component -> component resized)
    private void formComponentResized(java.awt.event.ComponentEvent evt) {                                      
        glCanvas.setSize(openGlPanel.getWidth(), openGlPanel.getHeight());
        this.renderer.setPanelWidth(this.openGlPanel.getWidth());
        this.renderer.setPanelHeight(this.openGlPanel.getHeight());
    } 
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ListOfModelsPanel;
    private javax.swing.JCheckBox circleCheckBox;
    private javax.swing.JButton cutButton;
    private javax.swing.JMenu edit;
    private javax.swing.JMenu file;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JLabel heightValueLabel;
    private javax.swing.JMenu help;
    private javax.swing.JPanel infoLeftPanel;
    private javax.swing.JPanel infoRightPanel;
    private javax.swing.JPanel informationPanel;
    private javax.swing.JList jList1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JLabel lengthLabel;
    private javax.swing.JLabel lengthValueLabel;
    private javax.swing.JLabel modelNameLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JPanel openGlPanel;
    private javax.swing.JPanel planePanel;
    private javax.swing.JLabel pointLabel;
    private javax.swing.JLabel pointsCountLabel;
    private javax.swing.JPanel positionOfPlanePanel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPanel projection2DPanel;
    private javax.swing.JLabel rPlaneSizeLabel;
    private javax.swing.JSlider rPlaneSizeSlider;
    private javax.swing.JSpinner rPlaneSizeSpinner;
    private javax.swing.JCheckBox rectangleCheckBox;
    private javax.swing.JPanel rotationOfPlanePanel;
    private javax.swing.JPanel sizeOfPlanePanel;
    private javax.swing.JCheckBox sqareCheckBox;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JLabel trianglesCountLabel;
    private javax.swing.JLabel trianglesLabel;
    private javax.swing.JPanel typeOfPlanePanel;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JLabel widthValueLabel;
    private javax.swing.JLabel xPlanePositionLabel;
    private javax.swing.JSlider xPlanePositionSlider;
    private javax.swing.JSpinner xPlanePositionSpinner;
    private javax.swing.JLabel xPlaneRotationLabel;
    private javax.swing.JSlider xPlaneRotationSlider;
    private javax.swing.JSpinner xPlaneRotationSpinner;
    private javax.swing.JLabel xPlaneSizeLabel;
    private javax.swing.JSlider xPlaneSizeSlider;
    private javax.swing.JSpinner xPlaneSizeSpinner;
    private javax.swing.JLabel yPlanePositionLabel;
    private javax.swing.JSlider yPlanePositionSlider;
    private javax.swing.JSpinner yPlanePositionSpinner;
    private javax.swing.JLabel yPlaneRotationLabel;
    private javax.swing.JSlider yPlaneRotationSlider;
    private javax.swing.JSpinner yPlaneRotationSpinner;
    private javax.swing.JSpinner yPlaneSizeSpinner;
    private javax.swing.JLabel zPlanePositionLabel;
    private javax.swing.JSlider zPlanePositionSlider;
    private javax.swing.JSpinner zPlanePositionSpinner;
    private javax.swing.JLabel zPlaneRotationLabel;
    private javax.swing.JSlider zPlaneRotationSlider;
    private javax.swing.JSpinner zPlaneRotationSpinner;
    private javax.swing.JLabel zPlaneSizeLabel;
    private javax.swing.JSlider zPlaneSizeSlider;
    // End of variables declaration//GEN-END:variables
}
