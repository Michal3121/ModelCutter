 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcutter;

import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.util.gl2.GLUT;
import java.awt.Color;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.media.opengl.GL;
import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LINES;
import static javax.media.opengl.GL.GL_NICEST;
import static javax.media.opengl.GL.GL_TRIANGLES;
import static javax.media.opengl.GL.GL_TRIANGLE_FAN;
import static javax.media.opengl.GL.GL_TRIANGLE_STRIP;
import javax.media.opengl.GL2;
import static javax.media.opengl.GL2.GL_COMPILE;
import static javax.media.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_COLOR_MATERIAL;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_NORMALIZE;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 *
 * @author MICHAL
 */
public class Renderer  implements GLEventListener {
    
    private GLU glu;
    private GLUT glut = new GLUT();
    private Model selectedModel;
    private double mouseZoom = 2;
    private boolean middleMouseButtonPressed = false;
    private boolean leftMouseButtonPressed = false;
    private int mouseX; 
    private int mouseY;
    private double prevTranslateX = 0; // pouzite pri transform
    private double currTranslateX = 0;
    private double prevTranslateY = 0;
    private double currTranslateY = 0;
    
    private List<Model> listOfModels;
    private Model model;
    private List<Point3f> ringList = new ArrayList<>();
    private Plane plane;
    private boolean isMouseFirstPressed = true;
    private double prevNormMouseX = /*0*/ -1;
    private double prevNormMouseY = /*0*/ -1;
    private Quaternion quatAllRot = new Quaternion(0.0f,0.0f,0.0f,1.0f);
    private Quaternion quatFinal = new Quaternion(0.0f,0.0f,0.0f,1.0f);
    private int panelWidth;
    private int panelHeight;
    
    public void setPanelWidth(int panelWidth) {
        this.panelWidth = panelWidth;
    }

    public void setPanelHeight(int panelHeight) {
        this.panelHeight = panelHeight;
    }
    
    public int getPanelWidth() {
        return panelWidth;
    }

    public int getPanelHeight() {
        return panelHeight;
    }
    
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
    
    public int getMouseX() {
        return mouseX;
    }

    public void setMouseX(int mouseX) {
        this.mouseX = mouseX;
    }

    public int getMouseY() {
        return  mouseY;
    }

    public void setMouseY(int mouseY) {
        this.mouseY = mouseY;
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

    public Model getSelectedModel() {
        return selectedModel;
    }

    public void setSelectedModel(Model selectedModel) {
        this.selectedModel = selectedModel;
    }

    public Quaternion getQuatFinal() {
        return quatFinal;
    }

    public void setQuatFinal(Quaternion quatFinal) {
        this.quatFinal = quatFinal;
    }

    public Quaternion getQuatAllRot() {
        return quatAllRot;
    }

    public void setQuatAllRot(Quaternion quatAllRot) {
        this.quatAllRot = quatAllRot;
    }
    
    public Renderer(int panelWidth, int panelHeight){ //Musi byt bezparametricky kostruktor, inak sa nic nevykresli po zapnuti
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
    }
    
    public Renderer(Collection<Model> models, int panelWidth, int panelHeight){
       this.listOfModels = new ArrayList<>(models);
       this.panelWidth = panelWidth;
       this.panelHeight = panelHeight;
    }
    
    private double transformCoordToNormalizedDeviceCoords(int coord, int sideLength){
        return (coord - (sideLength/2.0))/(sideLength/2.0) ;
    }
    
    private void renewCurrTranslateX(int x){   
        double deltaX = x - this.prevTranslateX;

        if(abs(deltaX) > 40.0f){ //50//40//20 netrha pri malych posunoch
            this.prevTranslateX = x;
            return;
        }
        this.currTranslateX += deltaX;
        this.prevTranslateX = x;
    }
    
    private void renewCurrTranslateY(int y){
        double deltaY = y - this.prevTranslateY;
            
        if(abs(deltaY) > 40.0f /*40.0f*/){ //50
            this.prevTranslateY = y;
            return;
        }
        this.currTranslateY += deltaY;
        this.prevTranslateY = y;
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        
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
        
        if(this.model != null)
        {
            gl.glNewList(1,GL_COMPILE);
            
            double translatedX = model.getModelCenter().x;
            double translatedY = model.getModelCenter().y;
            double translatedZ = model.getModelCenter().z;
                
            gl.glTranslated(- translatedX, - translatedY,  - translatedZ);
            this.drawModel(model, gl);
            //this.drawSquarePlane(this.getPlane(), 100, gl);
            
            gl.glEndList();
        }
        
        if(this.listOfModels != null && !listOfModels.isEmpty()) // POZOR vyhodnocovanie zlava
        {
            ModelManagerImpl modelManager = new ModelManagerImpl();
            Point3f centerOfModels = modelManager.findCenterOfModels(listOfModels);
            
            for(int i = 0; i < listOfModels.size(); i++){
                gl.glNewList(i + 1,GL_COMPILE);
                
                gl.glPushMatrix();
                gl.glTranslated(- centerOfModels.x, - centerOfModels.y,  - centerOfModels.z);          
                drawModel(listOfModels.get(i), gl);
                gl.glPopMatrix();
                
                gl.glEndList();
            }
        }
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
        glu.gluLookAt(0.0, 0.0, getMouseZoom() /*0.0*/, 0.0, 0.0, /*getMouseZoom() - 1*/ 0.0, 0.0, 1.0, 0.0);
        
        if(this.middleMouseButtonPressed) //stlacili sme stredne koliesko
        {
            double currNormMouseX = this.transformCoordToNormalizedDeviceCoords(this.getMouseX(), this.getPanelWidth());
            double currNormMouseY = this.transformCoordToNormalizedDeviceCoords(this.getMouseY(), this.getPanelHeight());
            
            if(this.prevNormMouseX != currNormMouseX || 
               this.prevNormMouseY != currNormMouseY) // zmenila sa aspon jedna suradnica
            {
                if(isMouseFirstPressed) // podmienka, aby nepreskocila kamera, ked kliknem na novu pozicou
                { 
                    this.prevNormMouseX = currNormMouseX;
                    this.prevNormMouseY = currNormMouseY;
                    this.isMouseFirstPressed = false;
                }else if(Math.abs(this.prevNormMouseX - currNormMouseX) > 0.4) // podmienky, aby netrhalo kameru, pri rychlych presunoch mysou
                {
                    this.prevNormMouseX = currNormMouseX;
                }else if(Math.abs(this.prevNormMouseY - currNormMouseY) > 0.4)
                {
                    this.prevNormMouseY = currNormMouseY;
                }else
                {
                    Vector2d newVector = new Vector2d(currNormMouseX, /*-*/currNormMouseY);
                    Quaternion quatCurrent = this.rotate(new Vector2d(this.prevNormMouseX, /*-*/this.prevNormMouseY), newVector, /*this.getMouseZoom()*/1);

                    quatFinal = quatCurrent.mult(quatAllRot);
                    quatAllRot = quatFinal;
                    this.prevNormMouseX = currNormMouseX;
                    this.prevNormMouseY = currNormMouseY;
                }
            }
        }else
        {
            this.isMouseFirstPressed = true;
        }
        
        gl.glMultMatrixf(quatFinal.toMatrix(new float[16], 0), 0); // umiestnene tu, otaca sa cela scena
        /*
        if(leftMouseButtonPressed){
            this.renewCurrTranslateX(this.getMouseX());
            this.renewCurrTranslateY(this.getMouseY());
        }
        
        gl.glTranslatef((float) this.currTranslateX/15.0f, 0.0f, 0.0f); // translacia mysou 
        gl.glTranslatef(0.0f, (float) - this.currTranslateY/15, 0.0f);*/
        
        this.drawAxes(15.0f, gl); // otacanie spolu s objektom
        
        // vykreslenie aktualneho modelu
        if(this.listOfModels != null && !listOfModels.isEmpty()){
            // vykreslenie objektov osobitne
            for(int i = 1; i <= listOfModels.size(); i++){
                Model currModel = listOfModels.get(i - 1);
                Color modelColor = currModel.getColor();
                
                if(currModel.equals(this.selectedModel)){ // vykreslenie oznaceneho
                    gl.glColor3ub((byte)modelColor.getRed(), (byte)modelColor.getGreen(), (byte)modelColor.getBlue());
                    Color reducedSatur = this.increaseSaturation(modelColor, 0.6f);
                    gl.glColor3ub((byte) reducedSatur.getRed(), (byte) reducedSatur.getGreen(), (byte) reducedSatur.getBlue());
                    if(gl.glIsList(i)){
                        gl.glCallList(i);
                    }
                }else{
                    gl.glColor3ub((byte)modelColor.getRed(), (byte)modelColor.getGreen(), (byte)modelColor.getBlue());
                    if(gl.glIsList(i)){
                        gl.glCallList(i);
                    }
                }
            }
        }
        
        // vykreslenie roviny
        if(this.plane != null){
            Color planeColor = Color.LIGHT_GRAY;
            gl.glColor3ub((byte) planeColor.getRed(), (byte) planeColor.getGreen(), (byte) planeColor.getBlue());
            
            gl.glPushMatrix();
            Point3f center = this.plane.getCenterPoint();
            gl.glTranslatef(center.x, center.y, center.z);
            
            this.drawPlane(plane, gl);
            if(this.plane instanceof CircularPlane){
                CircularPlane circularPlane = (CircularPlane) this.plane;
                float radius = circularPlane.getRadius();
                this.drawAxes(radius * 1.25f, gl);
            }
            if(this.plane instanceof SquaredPlane){
                SquaredPlane squaredPlane = (SquaredPlane) this.plane;
                float width = squaredPlane.getWidth();
                this.drawAxes(width / 2 * 1.25f, gl);
            }
            if(this.plane instanceof RectangularPlane){
                RectangularPlane rectangularPlane = (RectangularPlane) this.plane;
                float width = rectangularPlane.getWidth();
                this.drawAxes(width / 2 * 1.25f, gl);
            }
            
            gl.glPopMatrix();
        }
        
        
        //this.drawRing(gl);
        
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glColor3f(0, 1, 0);
        gl.glVertex2f(-1, -1);
        gl.glColor3f(0, 1, 0);
        gl.glVertex2f(0, 1);
        gl.glColor3f(0, 0, 1);
        gl.glVertex2f(1, -1);
        gl.glEnd();
        
        gl.glColor3f(1, 0, 0);
        
        // vykreslenie cajnika
        gl.glPushMatrix();
        //gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, 10.0f);
        glut.glutSolidTeapot(1);
        gl.glPopMatrix();
        
        // pokusna vykreslenie cajnika 
        gl.glPushMatrix();
        gl.glTranslatef(22.2f, 19.2f, 39.5f);
        glut.glutSolidTeapot(1);
        gl.glPopMatrix();
        
        
        
        gl.glPushMatrix();
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
    }
    
    private void drawAxes(float length, GL2 gl){
        gl.glLineWidth(2.5f); 
        
        gl.glBegin(GL_LINES);
            gl.glColor3f(1.0f, 0.0f, 0.0f); // X axe
            gl.glVertex3f(0.0f, 0.0f, 0.0f);
            gl.glVertex3f(length, 0, 0);
            
            gl.glColor3f(0.0f, 1.0f, 0.0f); // Y axe
            gl.glVertex3f(0.0f, 0.0f, 0.0f);
            gl.glVertex3f(0, length, 0);
            
            gl.glColor3f(0.0f, 0.0f, 1.0f); // Z axe
            gl.glVertex3f(0.0f, 0.0f, 0.0f);
            gl.glVertex3f(0, 0, length);            
        gl.glEnd();
        
        GLUquadric gluquad = glu.gluNewQuadric();
        // X cone
        gl.glPushMatrix();
        gl.glTranslatef(length, 0, 0);
        gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        glu.gluCylinder(gluquad, 0.5f, 0.0f, 0.1f * length, 150, 1);
        gl.glPopMatrix();
        
        // Y cone 
        gl.glPushMatrix();
        gl.glTranslatef(0, length, 0);
        gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
        gl.glColor3f(0.0f, 1.0f, 0.0f);
        glu.gluCylinder(gluquad, 0.5f, 0.0f, 0.1f * length, 150, 1);
        gl.glPopMatrix();
        
        // Z cone
        gl.glPushMatrix();
        gl.glTranslatef(0, 0, length);
        gl.glColor3f(0.0f, 0.0f, 1.0f);
        glu.gluCylinder(gluquad, 0.5f, 0.0f, 0.1f * length, 150, 1);
        gl.glPopMatrix();
            
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
        //glu.gluPerspective(45, width/(float)height, 0.1, 50);
        glu.gluPerspective(45, width/(float)height, 0.1, 500);
        gl.glMatrixMode(GL_MODELVIEW); 
            
    }
    
    public void drawModel(Model model, GL2 gl)
    {
        HashMap<Long, MVertex> vertices = new HashMap<>(model.getVertices());
        HashMap<Long, MTriangle> triangles = new HashMap<>(model.getTriangleMesh());
        
        for(long key : triangles.keySet())
        {
            MTriangle currentTriangle = triangles.get(key);
            long[] verticesKeys =  currentTriangle.getTriangleVertices();
            
            Vector3f normal = currentTriangle.getTriangleNormal();
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
    
    private Quaternion rotate(Vector2d lastVector2d, Vector2d nextVector2d, double radius)
    {
        Vector3d lastVector3d = this.transformToHolroyds3D(lastVector2d, radius);
        Vector3d nextVector3d = this.transformToHolroyds3D(nextVector2d, radius);

        lastVector3d.normalize();
        nextVector3d.normalize();
        
        Quaternion quatLast = new Quaternion((float) lastVector3d.x, (float) lastVector3d.y, (float) lastVector3d.z, 0);
        Quaternion quatNext = new Quaternion((float) nextVector3d.x, (float) nextVector3d.y, (float) nextVector3d.z, 0);

        quatNext.invert();
        quatNext.mult(quatLast);
        
        return quatNext;
    }
    
    private Vector3d transformToHolroyds3D(Vector2d vector, double r)
    {
        Vector2d vector2d = new Vector2d(vector);
        double rPow2 = Math.pow(r,2);
        float xPow2 = (float) Math.pow(vector2d.x,2);
        float yPow2 = (float) Math.pow(vector2d.y,2);
        double z;
        
        if(xPow2 + yPow2 <= (rPow2/2.0f)){
            z = Math.sqrt(rPow2 - (xPow2 + yPow2));
        }else{
            z = rPow2/(2*Math.sqrt(xPow2 + yPow2));
        }
        
        return new Vector3d(vector2d.x, vector2d.y, z);
    }
    
    public void drawPlane(Plane plane, GL2 gl){
        if(plane instanceof CircularPlane){
            CircularPlane circularPlane = (CircularPlane) plane;
            this.drawCircularPlane(circularPlane, gl);
        }
        if(plane instanceof SquaredPlane){
            SquaredPlane squaredPlane = (SquaredPlane) plane;
            this.drawSquaredPlane(squaredPlane, gl);
        }
        if(plane instanceof RectangularPlane){
            RectangularPlane rectangularPlane = (RectangularPlane) plane;
            this.drawRectangularPlane(rectangularPlane, gl);
        }
    }
    
    private void drawCircularPlane(CircularPlane circularPlane, GL2 gl){
        float radius = circularPlane.getRadius();
        int numberOfTriangles = 80;
        float twoPi = 2 * (float) Math.PI;
        float delta = twoPi / numberOfTriangles;
        
        gl.glBegin(GL_TRIANGLE_FAN);
            gl.glVertex3f(0.0f, 0.0f, 0.0f); // center
            for(int i = 0; i <= numberOfTriangles; i++){
                float x = radius * (float) Math.cos(i * delta);
                float z = radius * (float) Math.sin(i * delta);
                gl.glVertex3f(x, 0.0f, z);
            }
        gl.glEnd();
    }
    
    private void drawSquaredPlane(SquaredPlane squaredPlane, GL2 gl){
        int width = (int) squaredPlane.getWidth();
        float startValue = - width /2.0f; 
        
        for(int xCounter = 0; xCounter < width; xCounter++){
            for(int yCounter = 0; yCounter < width; yCounter++){
                float x = startValue + xCounter;
                float y = startValue + yCounter;
                
                gl.glBegin(GL_TRIANGLE_STRIP);
                    gl.glVertex3f(x, 0, y);
                    gl.glVertex3f(x, 0, y+1);
                    gl.glVertex3f(x+1, 0, y);
                    gl.glVertex3f(x+1,0,y+1);
                gl.glEnd();
            }
        }
    }
    
    private void drawRectangularPlane(RectangularPlane rectangularPlane, GL2 gl){
        int width = (int) rectangularPlane.getWidth();
        int length = (int) rectangularPlane.getLength();
        float startX = - width / 2.0f;
        float startY = - length /2.0f;
        
        for(int xCounter = 0; xCounter < width; xCounter++){
            for(int yCounter = 0; yCounter < length; yCounter++){
                float x = startX + xCounter;
                float y = startY + yCounter; 
                
                gl.glBegin(GL_TRIANGLE_STRIP);
                    gl.glVertex3f(x, 0, y);
                    gl.glVertex3f(x, 0, y+1);
                    gl.glVertex3f(x+1, 0, y);
                    gl.glVertex3f(x+1,0,y+1);
                gl.glEnd();
            }
        }
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
    
    private Color increaseSaturation(Color currColor, float magnitudeOfChange){
        float[] hsbcolor = new float[3];
        
        Color.RGBtoHSB(currColor.getRed(), currColor.getGreen(), currColor.getBlue(), hsbcolor);
        hsbcolor[1] = hsbcolor[1] + magnitudeOfChange;
        int newColorInt = Color.HSBtoRGB(hsbcolor[0], hsbcolor[1], hsbcolor[2]);
        
        return new Color(newColorInt); 
    }
}
