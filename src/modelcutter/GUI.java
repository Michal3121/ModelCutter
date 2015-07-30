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
    private Aplication app;
    
    private HashSet<Intersections> set = new HashSet<Intersections>();
    private Intersections i1;
    private Intersections i2;
    
    private ModelManagerImpl modelManager;
    private List<Model> models;
    private Model modelWithMap;
   
    
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
                if(e.isAltDown()){
                    renderer.setMouseX(e.getX());
                    renderer.setMouseY(e.getY()); 
                }    
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
        
        
        renderer = new Renderer(); 
        app = new Aplication();
        
        
        animator = new FPSAnimator(glCanvas, FPS, true);
        animator.start();      
        
        glCanvas.setSize(openGlPanel.getWidth(), openGlPanel.getHeight());
        //System.out.println("Rozmery canvasu width: " + openGlPanel.getWidth() + " height: " + openGlPanel.getHeight());
        
        //setActions();
        glCanvas.addGLEventListener(renderer);
        //glPanel.addKeyListener(openGlJPanel);
        glCanvas.requestFocusInWindow();
        
        
        i1 = new Intersections(new Coordinate(0.0,0.0,0.0), new Coordinate(1.0,1.0,0.0));
        i2 = new Intersections(new Coordinate(0.0,0.0,0.0), new Coordinate(1.0,1.0,0.0));
        System.out.println(i1.toString()); 
        
        set.add(i1);
        set.add(i2);
        
        System.out.println("Set ma " + set.size() + " prvkov");
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
                    //Model model = new Model(new File(openingChooser.getSelectedFile().getAbsolutePath()));
                    //renderer.drawModel(openingChooser.getSelectedFile().getAbsolutePath(), null);
                    //System.out.println(openingChooser.getSelectedFile().getAbsolutePath()); 
                    //model.loadModel();
                    //File vypis = new File(openingChooser.getSelectedFile().getAbsolutePath() + ".STL"));
                    //models.add(modelManager.loadModel2(new File(openingChooser.getSelectedFile().getAbsolutePath())));
                    //models.add(modelManager.loadModel2(new File(openingChooser.getSelectedFile().getAbsolutePath())));
                    modelWithMap = modelManager.loadModel(new File(openingChooser.getSelectedFile().getAbsolutePath()));
                    models.add(modelWithMap);
                    renderer = new Renderer(models);
                    
                    glCanvas.addGLEventListener(renderer);
                    
                    float planeX = modelWithMap.getModelCenter().x / 2;
                    float planeY = modelWithMap.getModelCenter().y / 2;
                    float planeZ = modelWithMap.getModelCenter().z / 2;

                    Point3f planeCenter = new Point3f(planeX, planeZ, planeY);
        
                    Plane plane = new Plane(planeCenter);
                    
                    renderer.setPlane(plane);
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

        openGlPanel = new javax.swing.JPanel();
        leftPanel = new javax.swing.JPanel();
        cutButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        file = new javax.swing.JMenu();
        edit = new javax.swing.JMenu();
        help = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout openGlPanelLayout = new javax.swing.GroupLayout(openGlPanel);
        openGlPanel.setLayout(openGlPanelLayout);
        openGlPanelLayout.setHorizontalGroup(
            openGlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 586, Short.MAX_VALUE)
        );
        openGlPanelLayout.setVerticalGroup(
            openGlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        cutButton.setText("Cut");
        cutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cutButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cutButton)
                .addContainerGap(93, Short.MAX_VALUE))
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cutButton)
                .addContainerGap(277, Short.MAX_VALUE))
        );

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
                .addContainerGap()
                .addComponent(openGlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(leftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(openGlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(41, 41, 41))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cutButtonActionPerformed
        List<Point3f> ringList = new ArrayList<>();
        Set<Long> intersectionTriangles = new HashSet<>();
        List<List<Long>> allParts;
        
        float planeX = this.modelWithMap.getModelCenter().x;
        float planeY = this.modelWithMap.getModelCenter().y;
        float planeZ = this.modelWithMap.getModelCenter().z;
        
        Point3f planeCenter = new Point3f(planeX, planeY, planeZ);
        
        Plane plane = new Plane(planeCenter);
        intersectionTriangles.addAll(app.getAllIntersectionTriangles(modelWithMap, plane));
        allParts = app.getListsOfParts(intersectionTriangles, modelWithMap);
        renderer = new Renderer(models);
        glCanvas.addGLEventListener(renderer);
        
        System.out.println("Center 2 Cube" + this.modelWithMap.getModelCenter());
        System.out.println("Center Cube" + this.modelWithMap.getModelCenter());
        
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
    private javax.swing.JButton cutButton;
    private javax.swing.JMenu edit;
    private javax.swing.JMenu file;
    private javax.swing.JMenu help;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JPanel openGlPanel;
    // End of variables declaration//GEN-END:variables
}
