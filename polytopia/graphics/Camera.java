package polytopia.graphics;

import java.awt.*;
import java.awt.geom.*;

public class Camera {
    
    private int cameraX;
    private int cameraY;
    private double cameraScale = 0; 
    private int cameraZoom = -10;
    
    private final int cameraMinZoom = -80;
    private final int cameraMaxZoom = 80;
    private final double cameraMinScale = 0.2;
    private final double cameraMaxScale = 0.5;
    private static final long focusCycle = 500;
    
    private int lastX = 0;
    private int laxtY = 0;
    private int destinationX;
    private int destinationY;
    private long lastTime;
    private long startTime;

    private int velocityX = 0;
    private int velocityY = 0;

    // used for transforming the point
    final private AffineTransform pointTrans = new AffineTransform();
    private AffineTransform trans = new AffineTransform();


    public Camera (int posX, int posY, double rotate, double sx, double sy, double theta, double scale) 
    {
        if (Math.abs(theta - Math.PI / 2) < 0.01) 
            System.out.println ("theta is too large!!!\n");
        pointTrans.rotate(rotate, 0, 0);
        pointTrans.shear(1 / Math.tan(theta), 0);
        pointTrans.scale(sx, sy * (Math.sin(theta)));
        velocityX = 0;
        velocityY = 0;
        setCamera(posX, posY, scale);
    }

    private void setCamera(){
        trans.setToTranslation(cameraX, cameraY);
        trans.scale(cameraScale, cameraScale);
    }

    public void setCamera(int posX, int posY){
        cameraX = posX;
        cameraY = posY;
        setCamera();
    }

    public void setCamera(double scale){
        cameraScale = scale;
        setCamera();
    }

    public void setCamera(int posX, int posY, double scale){
        
        cameraX = posX;
        cameraY = posY;
        cameraScale = scale;
        setCamera();
    }

    public void changePos (int endX, int endY){
        setCamera(cameraX + endX - lastX, cameraY + endY - laxtY);
        lastX = endX;
        laxtY = endY;
    }

    public void setGraphics2D(Graphics2D g2d) {
        g2d.transform(trans);
    }

    public void changeScale (int detaZoom) {
        cameraZoom -= detaZoom * 10;
        if (cameraZoom < cameraMinZoom)
            cameraZoom = cameraMinZoom;
        if (cameraZoom > cameraMaxZoom)
            cameraZoom = cameraMaxZoom;
        double scale = (cameraMaxScale + cameraMinScale) / 2 + (cameraMaxScale - cameraMinScale) / 2 
        * Math.sin((Math.PI / (cameraMaxZoom - cameraMinZoom)) * cameraZoom);
        setCamera(scale);
    }


    public Point2D transPoint (Point2D point){
        
        Point2D dst = new Point2D.Double();
        return pointTrans.transform(point, dst);
    }

    public void cameraFocus(int focusX, int focusY, int width, int height){
        if(focusX > 3 * width / 4 ||  focusX < width / 4 || focusY > 3 * height / 4 || focusY < height /4){
            destinationX = width / 2 - focusX + cameraX;
            destinationY = height / 2 - focusY + cameraY;
            startTime = System.currentTimeMillis();
            lastTime = 0;
        }
    }
    
    public void updateCamera(){
        lastTime = System.currentTimeMillis() - startTime;
        if (lastTime < focusCycle) {
            setCamera((int)((destinationX - cameraX) * ((double)lastTime / focusCycle)) + cameraX, 
            (int)((destinationY - cameraY) * ((double)lastTime / focusCycle)) + cameraY);
        }
    }
    public Point2D inverseTransPoint(Point2D point) {
        try{
            Point2D tmp = trans.inverseTransform(point, null);
            tmp.setLocation(tmp.getX(), tmp.getY() + Render.detaHeight);
            return pointTrans.inverseTransform(tmp, null);
        }
        catch(Exception e){
            System.out.println(e + "can't be inversed");
            return null;
        }
    }

    public void setMousePos(int x, int y) {
        
    }

    public void setStart(int x, int y){
        lastX = x;
        laxtY = y;
    }
}
