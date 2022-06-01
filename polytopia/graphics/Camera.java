package polytopia.graphics;

import java.awt.*;
import java.awt.event.*;
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
    
			
    private int dragStartX = 0;
    private int dragStartY = 0;
    private int mouseX = 0;
    private int mouseY = 0;


    private int velocity;

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
        velocity = 0;
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
    
        setCamera(cameraX + endX - dragStartX, cameraY + endY - dragStartY);
        dragStartX = endX;
        dragStartY = endY;
        
    }
    public void setGraphics2D(Graphics2D g2d) {
        g2d.transform(trans);
    }
    public void changeScale (int detaZoom) {
        cameraZoom -= detaZoom;
        if (cameraZoom < cameraMinZoom)
            cameraZoom = cameraMinZoom;
        if (cameraZoom > cameraMaxZoom)
            cameraZoom = cameraMaxZoom;
        double scale = (cameraMaxScale + cameraMinScale) / 2 + (cameraMaxScale - cameraMinScale) / 2 
        * Math.sin((Math.PI / (cameraMaxZoom - cameraMinZoom)) * cameraZoom);
        try{
            Point2D focus = trans.inverseTransform(new Point2D.Double(mouseX, mouseY), null);
            setCamera(cameraX - (int)(focus.getX() * (scale - cameraScale)), cameraY - (int)(focus.getY() * (scale - cameraScale)), scale);
        }
        catch(Exception e){
            System.out.println(e + "can't be inversed");
            setCamera(scale);
        }
    }
    public Point2D transPoint (Point2D point){
        
        Point2D dst = new Point2D.Double();
        return pointTrans.transform(point, dst);
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
        mouseX = x;
        mouseY = y;
    }
    public void setStart(int x, int y){
        dragStartX = x;
        dragStartY = y;
    }
}
