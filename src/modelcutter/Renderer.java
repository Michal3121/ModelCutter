/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.math.VectorUtil;
import static com.jogamp.opengl.math.VectorUtil.VEC3_ONE;
import com.jogamp.opengl.util.gl2.GLUT;
import com.vividsolutions.jts.geom.Coordinate;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import static java.lang.Math.abs;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL;
import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_LINES;
import static javax.media.opengl.GL.GL_NICEST;
import static javax.media.opengl.GL.GL_TRIANGLES;
import static javax.media.opengl.GL.GL_TRIANGLE_STRIP;
import static javax.media.opengl.GL.GL_VIEWPORT;
import javax.media.opengl.GL2;
import static javax.media.opengl.GL2.GL_COMPILE;
import static javax.media.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import static javax.media.opengl.GLProfile.GL2;
import javax.media.opengl.awt.GLJPanel;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_COLOR_MATERIAL;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_NORMALIZE;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 *
 * @author MICHAL
 */
public class Renderer  implements GLEventListener {
    
    private GLU glu;
    private GLUT glut = new GLUT();
    private float angleTriangle = 0.0f;
    private static final float ANGLE_SPEED = 0.5f;
    private double mouseZoom = 2;
    private boolean middleMouseButtonPressed = false;
    private boolean leftMouseButtonPressed = false;
    private double mouseX;
    private double mouseY;
    private double lastX = 0;

    private double lastY = 0;
    private Quat4f kvad = new Quat4f(1,2,3,5);
    private Quaternion kvad2 = new Quaternion(1,2,3,4);
    private Quaternion quadA = new Quaternion(2,2,0,0);
    private Quaternion quadB = new Quaternion(0,0,0,0);
    private Quaternion quadC = new Quaternion(2,-2,0,0);
    private Quaternion quadX = new Quaternion(0,0, (float) -(1/Math.sqrt(2)),(float) (1/Math.sqrt(2)));
    //private float[] euler = {0, 0, 0};
    private float[] euler = new float [4];
    private float[] euler2;
    private float[] matrix = new float [16];
    private Vector3d vector1;
    private Vector3d vector2;
    private List<Model> listOfModels;
    private List<Point3f> ringList= new ArrayList<>();
    private Plane plane;
    
    
    private float[] vectorA = {2,2,0};
    private float[] vectorC = {2,-2,0};
    
    private VectorUtil vectorUtil = new VectorUtil();
            
    private Vector3f vec;
     
    public Vector3d getVector1() {
        return vector1;
    }

    public void setVector1(double x, double y, double z) {
        this.vector1 = new Vector3d(x,y,z);
    }

    public Vector3d getVector2() {
        return vector2;
    }

    public void setVector2(double x, double y, double z) {
        this.vector2 = new Vector3d(x,y,z);
    }
    
    
    //private Model liver; 
    
    public boolean isLeftMouseButtonPressed() {
        return leftMouseButtonPressed;
    }

    public void setLeftMouseButtonPressed(boolean leftMouseButtonPressed) {
        this.leftMouseButtonPressed = leftMouseButtonPressed;
    }
    
    public boolean isMiddleMouseButtonPressed() {
        return middleMouseButtonPressed;
    }

    public void setMiddleMouseButtonPressed(boolean middleMouseButton) {
        this.middleMouseButtonPressed = middleMouseButton;
    }
    
    public double getMouseX() {
        return mouseX;
    }

    public void setMouseX(double mouseX) { 
        if(isMiddleMouseButtonPressed() || isLeftMouseButtonPressed()){
            
            double deltaX = mouseX - this.lastX;
                      
            if(abs(deltaX) > 40.0f){ //50
                this.lastX = mouseX;
                return;
            }
            this.mouseX += deltaX;
            this.lastX = mouseX;
        }
        
    }

    public double getMouseY() {
        return mouseY;
    }

    public void setMouseY(double mouseY) {
        if(isMiddleMouseButtonPressed() || isLeftMouseButtonPressed()){
            double deltaY = mouseY - this.lastY;
            
            if(abs(deltaY) > 40.0f){ //50
                this.lastY = mouseY;
                return;
            }
            this.mouseY += deltaY;
            this.lastY = mouseY;          
        }  
    }

    public double getMouseZoom() {
        return mouseZoom;
    }

    public void setMouseZoom(double zoom) {
        this.mouseZoom = this.mouseZoom + zoom;
    }

    public List<Point3f> getRingList() {
        return ringList;
    }

    public void setRingList(List<Point3f> ringList) {
        this.ringList = ringList;
    }

    public Plane getPlane() {
        return plane;
    }

    public void setPlane(Plane plane) {
        this.plane = plane;
    }
    
    public Renderer(){
        listOfModels = new ArrayList<>();
    }
    
    public Renderer(Collection<Model> models){
        listOfModels = new ArrayList<>();
        System.out.println("Pred pridanim");
        this.listOfModels.addAll(models);
        System.out.println("Po pridani");
    }
    
    
    @Override
    public void init(GLAutoDrawable drawable) {
        /*
        System.out.println(kvad2.toString());
        System.out.println(kvad2.normalize().toString());
        
        System.out.println("Kvaternion A " + quadA.toString());
        System.out.println("Kvaternion B " + quadB.toString());
        System.out.println("Kvaternion C " + quadC.toString());
        
        System.out.println("Kvaternion normalizovany A " + quadA.normalize().toString());
        System.out.println("Kvaternion normalizovany B " + quadB.normalize().toString());
        System.out.println("Kvaternion normalizovany C " + quadC.normalize().toString());
        
        System.out.println("Kvaternion C " + quadC.normalize().toString());
        */
        
        System.out.println("Kvaternion X " + quadX.toString());
        System.out.println("Kvaternion X norm " + quadX.normalize().toString());
        euler2 = quadX.toEuler(euler);
        System.out.println("Euler X " + euler2[0] + " Y =" + euler2[1] + " z=" + 90* Math.atan(euler2[2]));
        
        System.out.println("Euler X " + euler2[0] + " Y =" + euler2[1] + " z2=" + Math.toDegrees(euler2[2]));
        
        matrix = quadX.toMatrix(matrix, 0);
        
        System.out.println( matrix[0] + " " + matrix[1] + " " + matrix[2] + " " + matrix[1]);
        System.out.println( matrix[4] + " " + matrix[5] + " " + matrix[6] + " " + matrix[7]);
        System.out.println( matrix[8] + " " + matrix[9] + " " + matrix[10] + " " + matrix[12]);
        System.out.println( matrix[12] + " " + matrix[13] + " " + matrix[14] + " " + matrix[15]);
        
        //rotate(1,2,3);
        //rotate();
         
        
        GL2 gl = drawable.getGL().getGL2();    
        
        //------------ INIT POINT -----------------------
         gl.glPointSize( 6.0f );
         
        
        //-----------------------------------------------
       
        
        glu = new GLU();
        gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f); 
        gl.glClearDepthf(1.0f);
        
        gl.glEnable(GL_COLOR_MATERIAL);
        gl.glEnable(GL_NORMALIZE);
        gl.glEnable(GL_LIGHTING);
        gl.glEnable(GL_LIGHT0);
        gl.glEnable(GL_DEPTH_TEST); 
        //gl.glDepthFunc(GL_LEQUAL);
        
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        gl.glShadeModel(GL_SMOOTH);
        
        
        
        gl.glNewList(1,GL_COMPILE);
        //gl.glTranslatef(-1.0f, -1.0f, 0.0f);
        //gl.glScalef(0.04f,0.04f,0.04f);
        
        //liver.loadModel();
        /*
        try {
            drawModel(new File(getClass().getResource("/models/Computer3.stl").toURI()),gl);
        } catch (URISyntaxException ex) {
            Logger.getLogger(Renderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
        
        
        
        if(!listOfModels.isEmpty()){
            
            for(int i=0; i< listOfModels.size(); i++){
                double translatedX = listOfModels.get(i).getModelCenter().x;
                double translatedY = listOfModels.get(i).getModelCenter().y;
                double translatedZ = listOfModels.get(i).getModelCenter().z;
                
                gl.glBegin(GL.GL_POINTS);
                gl.glPointSize( 6.0f );
                gl.glColor3f(0,1,0);
                gl.glVertex3f((float) translatedX/2, (float) translatedY/2, (float) translatedZ/2);
                gl.glPointSize( 6.0f );
                //gl.glColor3f(1.0f, 1.0f, 1.0f);
                gl.glEnd();

                
                System.out.println("x = " + translatedX);
                System.out.println("y = " + translatedY);
                System.out.println("z = " + translatedZ);
                
                System.out.println("Velkost ----------");
                System.out.println("x:" + listOfModels.get(i).getSizeX());
                System.out.println("y:" + listOfModels.get(i).getSizeY());
                System.out.println("z:" + listOfModels.get(i).getSizeZ());
                
                gl.glTranslated(- translatedX, - translatedY,  - translatedZ);
                //drawModel(listOfModels.get(i), gl);
                
                drawModel2(listOfModels.get(i), gl);
                //gl.glTranslated(- translatedX, 0,  0);
                System.out.println("kreslime!");
            }
            this.drawSquarePlane(this.getPlane(), 100, gl);
            
        }
        gl.glEndList();
        
        gl.glNewList(2, GL_COMPILE);
        drawFloor(50,gl);
        gl.glEndList();
           
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2(); 
        //gl.glClear(GL_COLOR_BUFFER_BIT); // vymaze color buffer na zaciatku vykreslovania
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color and depth buffers
        
        gl.glLoadIdentity();
        
        //glu.gluLookAt(0.0, 0.0, 1.5, 0.0, 0.0, 0.0, 0.0, 7.0, 0.0); //povodna gluLookAt
        //glu.gluLookAt(0.0, 0.0, mouseZoom, 0.0, 0.0, mouseZoom - 1, 0.0, 1.0, 0.0);
       
        glu.gluLookAt(0.0, 0.0, getMouseZoom() /*0.0*/, 0.0, 0.0, /*getMouseZoom() - 1*/ 0.0, 0.0, 1.0, 0.0);
        
        
        
        /*
        rotate(getMouseX()/2, getMouseY()/2, getMouseZoom());
        gl.glRotatef((float) Math.toDegrees(euler2[0]), 1, 0, 0);
        gl.glRotatef((float) Math.toDegrees(euler2[1]), 0, 1, 0);
        gl.glRotatef((float) Math.toDegrees(euler2[2]), 0, 0, 1);
        */
        //System.out.println("Mouse X " + getMouseX());
        
        gl.glTranslatef((float) getMouseX()/15, 0.0f, 0.0f); // translacia mysou 
        gl.glTranslatef(0.0f, (float) -getMouseY()/15, 0.0f);
        
        
        this.drawAxes(gl);
        
        // vykreslenie aktualneho modelu
        gl.glPushMatrix();
        gl.glCallList(1); 
        gl.glPopMatrix();
        
        this.drawRing(gl);
        
        
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        
        
        //glu.gluLookAt(0.0, 0.0, 1.5, 0.0, 0.0, 0.0, 0.0, 7.0, 0.0);
        
        //gl.glMultMatrixf(matrix, 0);
        //gl.glMultMatrixf(rotate(getMouseX()/2, getMouseY()/2, getMouseZoom()), 0);
        //gl.glMultMatrixf(rotate(),0);
        
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glColor3f(0, 1, 0);
        gl.glVertex2f(-1, -1);
        gl.glColor3f(0, 1, 0);
        gl.glVertex2f(0, 1);
        gl.glColor3f(0, 0, 1);
        gl.glVertex2f(1, -1);
        gl.glEnd();
        
        gl.glColor3f(1, 0, 0);
        
        //gl.glRotatef(angleTriangle, 0.0f, 1.0f, 0.0f);
        //draw a triangle filling the window
        
        // vykreslenie cajnika
        gl.glPushMatrix();
        gl.glTranslatef(0.0f, 0.0f, 10.0f);
        glut.glutSolidTeapot(1);
        gl.glPopMatrix();
        
        // pokusna vykreslenie cajnika 
        gl.glPushMatrix();
        gl.glTranslatef(22.2f, 19.2f, 39.5f);
        glut.glutSolidTeapot(1);
        gl.glPopMatrix();
        
        
        
        gl.glPushMatrix();
        //gl.glMultMatrixf(matrix, 0);
        glut.glutSolidTeapot(1);
        gl.glPopMatrix();
        
        //gl.glCallList(2);
        
        
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glColor3f(1, 0, 0);
        gl.glVertex2f(-1, -1);
        gl.glColor3f(0, 1, 0);
        gl.glVertex2f(0, 1);
        gl.glColor3f(0, 0, 1);
        gl.glVertex2f(1, -1);
        gl.glEnd();
          
        
        /*
         gl.glBegin(GL.GL_TRIANGLES); // draw using triangles
         gl.glVertex3f(0.0f, 1.0f, 0.0f);
         gl.glVertex3f(-1.0f, -1.0f, 0.0f);
         gl.glVertex3f(1.0f, -1.0f, 0.0f);
         gl.glEnd();
         */
        angleTriangle += ANGLE_SPEED; 
    }
    
    private void drawAxes(GL2 gl){
        gl.glLineWidth(2.5f); 
        gl.glBegin(GL_LINES);
        gl.glColor3f(1.0f, 0.0f, 0.0f); // X axe
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(15, 0, 0);
        gl.glColor3f(0.0f, 1.0f, 0.0f); // Y axe
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(0, 15, 0);
        gl.glColor3f(0.0f, 0.0f, 1.0f); // Z axe
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(0, 0, 15);
        gl.glEnd();
    }
    
    private void drawRing(GL2 gl){
        gl.glBegin(GL.GL_POINTS);
        gl.glColor3f(1,0,0);
        for (int i = 0; i < this.getRingList().size(); i++){
            
            gl.glVertex3f(this.getRingList().get(i).x, this.getRingList().get(i).y, this.getRingList().get(i).z);
        }
        gl.glColor3f(1, 0, 0);
        gl.glEnd();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        //gl.glViewport(0, 0, width, height); // update the viewport
        gl.glViewport(x, y, width, height);
         
        gl.glMatrixMode(GL_PROJECTION); // nastavi sa typ projekcie 
        gl.glLoadIdentity();
        //glu.gluPerspective(45, width/(float)height, 1, 50); 
        glu.gluPerspective(45, width/(float)height, 0.1, 50);
        gl.glMatrixMode(GL_MODELVIEW); 
        
        
         
    }
    
    public void drawModel(Model model, GL2 gl){
        
        double maxX;
        double minY;
        
        //Model model = new Model(path);
        //model.loadModel();
        //Model model = listOfModels.get(1);
        
        maxX = model.getFacet(0).getTriangleCoord0().x;
        
        for(int i=0; i<model.getNumberOfFacet(); i++){
            ModelFacet triangle =  model.getFacet(i);
            Coordinate n = triangle.getNormal();
            
            
            gl.glBegin(GL_TRIANGLES);
            gl.glNormal3d(n.x, n.y, n.z);
            gl.glVertex3d(triangle.getTriangleCoord0().x, triangle.getTriangleCoord0().y, triangle.getTriangleCoord0().z);
            gl.glVertex3d(triangle.getTriangleCoord1().x, triangle.getTriangleCoord1().y, triangle.getTriangleCoord1().z);
            gl.glVertex3d(triangle.getTriangleCoord2().x, triangle.getTriangleCoord2().y, triangle.getTriangleCoord2().z);
            gl.glEnd();
            
        }
    }
    
    public void drawModel2(Model model, GL2 gl)
    {
        HashMap<Long, MVertex> vertices = new HashMap<>(model.getVertices());
        HashMap<Long, MTriangle> triangles = new HashMap<>(model.getTriangleMesh());
        
        for(long key : triangles.keySet())
        {
            //System.out.println("kreslim z mapy");
            MTriangle currentTriangle = triangles.get(key);
            long[] verticesKeys =  currentTriangle.getTriangleVertices();
            
            Point3f normal = currentTriangle.getTriangleNormal();
            Point3f vertex0 = vertices.get(verticesKeys[0]).getVertex();
            Point3f vertex1 = vertices.get(verticesKeys[1]).getVertex();
            Point3f vertex2 = vertices.get(verticesKeys[2]).getVertex();
            
            gl.glBegin(GL_TRIANGLES);
            gl.glNormal3f(normal.x, normal.y, normal.z);
            gl.glVertex3f(vertex0.x, vertex0.y, vertex0.z);
            gl.glVertex3f(vertex1.x, vertex1.y, vertex1.z);
            gl.glVertex3f(vertex2.x, vertex2.y, vertex2.z);
            gl.glEnd();
        }
    }
    
    
    
    
    public float[] rotate(double x, double y, double r){
        /*
        double x = 2;
        double y = 0;
        double r = -3;
        */
        //Vector3d first = new Vector3d(0,0,r);
        Vector3d first;
        Vector3d last;
        Vector3d crossVec = new Vector3d(0,0,0);
        double dotProduct = 0.0;
        double angle;
        double zFirst = 0;
        double z = 0;
            
        double xLastPow2 = Math.pow(x,2);
        double yLastPow2 = Math.pow(y,2);
        double rPow2 = Math.pow(r,2);
            
        float[] rotMatrix = new float [16];
        
        if(xLastPow2 + yLastPow2 <= (rPow2/2)){
            z = Math.sqrt(rPow2 - (xLastPow2 + yLastPow2));
        }else{
            z = (rPow2/2)/Math.sqrt(xLastPow2 + yLastPow2);
        }
        
        first = new Vector3d(0,0,z);
        last = new Vector3d(x,y,z);
        System.out.println("first" + first.toString());
        System.out.println("last" + last.toString());
        
        //System.out.println("Dlzka pred normalizaciou " + last.length());
        //System.out.println("Dlzka pred normalizaciou " + last.length());
        
        first.normalize();
        last.normalize();
        
        //System.out.println("Dlzka po normalizacii " + last.length());
        
        crossVec.cross(first, last);
        dotProduct = first.dot(last);
        
        angle = Math.acos(dotProduct);
        
        Quaternion quad = new Quaternion((float) (crossVec.x * Math.sin(angle/2)), (float) (crossVec.y * Math.sin(angle/2)), (float) (crossVec.z * Math.sin(angle/2)), (float) Math.cos(angle/2));
        
        //System.out.println("Kvaternion " + quad.toString());
        
        euler2 = quad.toEuler(euler);
        return quad.toMatrix(rotMatrix, 0);
    }
    
    public void drawFloor(int size, GL2 gl){
        
        
        for(int x=-size/2; x<size/2; x++){
            for(int z=-size/2; z<size/2; z++){
               gl.glBegin(GL_LINES); 
               gl.glLineWidth(2.0f);
               gl.glColor3f(1.0f, 0.0f, 0.0f);
               gl.glVertex3i(x, 0, z);
               gl.glVertex3i(x,0,z+1);
               gl.glVertex3i(x,0,z+1);
               gl.glVertex3i(x+1,0,z+1);
               gl.glVertex3i(x+1,0,z+1);
               gl.glVertex3i(x+1,0,z);
               gl.glVertex3i(x+1, 0,z);
               gl.glVertex3i(x,0,z);
               gl.glEnd();
            }
        }
    }
    
    public void drawSquarePlane(Plane plane, int size, GL2 gl){
        
        Point3f centerOfPlane = plane.getCenterPoint();
        gl.glTranslatef(centerOfPlane.x, centerOfPlane.y, centerOfPlane.z);
        
        for(int x = -(size/2); x<(size/2); x++){
            for(int z= -(size/2); z<(size/2); z++){
                gl.glBegin(GL_TRIANGLE_STRIP);
                gl.glColor3f(1.0f, 0.0f, 0.0f);
                gl.glVertex3i(x, 0, z);
                gl.glVertex3i(x, 0, z+1);
                gl.glVertex3i(x+1, 0, z);
                gl.glVertex3i(x, 0, z+1);
                gl.glVertex3i(x+1, 0, z);
                gl.glVertex3i(x+1,0,z+1);
                gl.glEnd();     
            }
        }   
    }

  
    
    
}
