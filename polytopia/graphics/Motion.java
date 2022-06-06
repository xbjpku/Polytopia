package polytopia.graphics;

import java.awt.geom.*;
import java.awt.*;
import java.awt.image.*;

import polytopia.gameplay.Tile;

public class Motion {
    final static int shakeBias = 8;
    final static int shakeCycle = 100;
    final static int g = 40;
    Tile departureTile;
    Tile destinationTile;

    double startTime;
    double lastTime;
    double deadline;

    int maxHeight;
    int accelerate = 20;

    public enum MotionType{
        RISE, SHAKE, JUMP, PRESSED, TRANSLATE, ROTATE
    } 
    MotionType type;

    public Motion(Tile t, double start, double deadline) {
        departureTile = t;
        destinationTile = t;
        startTime = start;
        this.deadline = deadline;
        type = MotionType.SHAKE;
    }

    public Motion(Tile t, double start, double deadline, int accelerate) {
        departureTile = t;
        destinationTile = t;
        startTime = start;
        this.deadline = deadline;
        this.accelerate = accelerate;
        type = MotionType.RISE;
    }

    public Motion(Tile departureTile, Tile destinationTile, double start, double deadline, MotionType type) {
        this.departureTile = departureTile;
        this.destinationTile = destinationTile;
        this.startTime = start;
        this.deadline = deadline;
        this.type = type;
        if (type == MotionType.JUMP) {
            this.maxHeight = g * (int)(deadline - start) * (int)(deadline - start) / 10000 / 8;;
        }
    }
    

    synchronized public boolean update(double currentTime){
        lastTime = currentTime - startTime;
        if (currentTime > deadline){
            this.notify();
            return true;
        }
        return false;
    }

    public Point2D getOffset(){
        Point2D translation = getTranslation();
        switch(type){
            case RISE: return new Point2D.Double(0, -getRiseHeight());
            case SHAKE: return getShakeTranslation();
            case JUMP: return new Point2D.Double(translation.getX(), translation.getY() - getJumpHeight());
            case PRESSED: return new Point2D.Double(0, getPressedHeight());
            case TRANSLATE: return translation;
            default: return null;
        }
    }

    public static Motion getInstanceOfMovableMotion(Movable subject, Tile t, double start, double deadline){
        return (Motion)new MovableMotion(subject, t, start, deadline);
    }
    public static Motion getInstanceOfMovableMotion(Movable subject, Tile departureTile, Tile destinationTile, double start, double deadline) {
        return (Motion)new MovableMotion(subject, departureTile, destinationTile, start, deadline);
    }
    public static Motion getInstanceOfTextureMotion(String name, Tile t, double start, double deadline){
        return (Motion)new TextureMotion(name, t, start, deadline);
    }
    public static Motion getInstanceOfTextureMotion(String name, Tile departureTile, Tile destinationTile, double start, double deadline) {
        return (Motion)new TextureMotion(name, departureTile, destinationTile, start, deadline);
    }
    public static Motion getInstanceOfTextureMotion(String name, Tile t, double start, double deadline, int accelerate){
        return (Motion)new TextureMotion(name, t, start, deadline, accelerate);
    }
    public static Motion getInstanceOfStringMotion(String string, Tile t, double start, double deadline, Color color){
        return (Motion)new StringMotion(string, t, start, deadline, color);
    }

    public void setMotionType(MotionType type){
        this.type = type;
    }

    public int getJumpHeight(){
        int heightOffset = maxHeight - (int)Math.round((1 - (2 * lastTime) / (deadline - startTime)) * 
        (1 - (2 * lastTime) / (deadline - startTime)) * maxHeight);
        return heightOffset;
    }

    public int getRiseHeight(){
        int heightOffset = (int)Math.round(lastTime * 
        lastTime  / 10000) * accelerate / 2;
        return heightOffset;
    }

    public int getPressedHeight(){
        int heightOffset = (int)((deadline - startTime) / shakeCycle * shakeBias * 
        (Math.sin((2 * Math.PI / (deadline - startTime) * lastTime) - Math.PI / 6) + 0.5f));
        return heightOffset;
    }

    public Point2D getTranslation(){
        Point2D pointDeparture = Render.camera.transPoint(new Point2D.Double((double)departureTile.getY(), (double)departureTile.getX()));
        Point2D pointDestination = Render.camera.transPoint(new Point2D.Double((double)destinationTile.getY(), (double)destinationTile.getX()));
        return new Point2D.Double((pointDestination.getX() - pointDeparture.getX()) * lastTime / (deadline - startTime), 
        (pointDestination.getY() - pointDeparture.getY()) * lastTime / (deadline - startTime));
    }

    private Point2D getShakeTranslation(){
        return new Point2D.Double(0, -2 * shakeBias * Math.sin((2 * Math.PI / shakeCycle) * lastTime));
    }
    public double getRotation(){
        return  Math.PI / 2 * lastTime / (deadline - startTime);
    }

}

class MovableMotion extends Motion{

    private Movable subject;

    public MovableMotion(Movable subject, Tile t, double start, double deadline) {
        super(t, start, deadline);
        this.subject = subject;
    }
    public MovableMotion(Movable subject, Tile departureTile, Tile destinationTile, double start, double deadline) {
        super(departureTile, destinationTile, start, deadline, MotionType.TRANSLATE);
        this.subject = subject;
    }

    public Movable getSubject() {
        return subject;
    }
}

class TextureMotion extends Motion{

    private String name;
    private BufferedImage texture;

    public TextureMotion(String name, Tile t, double start, double deadline) {
        super(t, start, deadline);
        this.name = name;
        this.texture = Texture.getTextureByName(name);
        setMotionType(MotionType.RISE);
    }

    public TextureMotion(String name, Tile t, double start, double deadline, int accelerate) {
        super(t, start, deadline, accelerate);
        this.name = name;
        this.texture = Texture.getTextureByName(name);
    }

    public TextureMotion(String name, Tile departureTile, Tile destinationTile, double start, double deadline) {
        super(departureTile, destinationTile, start, deadline, MotionType.JUMP);
        this.name = name;
        this.texture = Texture.getTextureByName(name);
    }
    public void drawTexture(Graphics2D g2d){
        Point2D offset = getOffset();
        Point2D pointDeparture = Render.camera.transPoint(new Point2D.Double((double)departureTile.getY(), (double)departureTile.getX()));
       
        final int fogOffset = 100;
        AffineTransform tmp;
        AlphaComposite ac2;
        switch(name){
            case "FOG": ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - (float)(lastTime / (deadline - startTime)));
                g2d.setComposite(ac2);
                tmp =  AffineTransform.getTranslateInstance(
                offset.getX() + pointDeparture.getX() - 0.2f * texture.getWidth() / 2, offset.getY() 
                + pointDeparture.getY() - fogOffset - Render.detaHeight - Render.tileHeight * 1.5f);
                tmp.scale(0.2f, 0.2f);
                g2d.drawRenderedImage(texture, tmp);

                tmp = AffineTransform.getTranslateInstance(
                    offset.getX() + pointDeparture.getX() - Render.tileWidth / 2 + 0.11f * texture.getWidth(),
                    offset.getY() + pointDeparture.getY() - fogOffset - Render.detaHeight - Render.tileHeight);
                tmp.scale(0.22f, 0.22f);
                g2d.drawRenderedImage(texture, tmp);

                tmp = AffineTransform.getTranslateInstance(
                    offset.getX() + pointDeparture.getX() + Render.tileWidth / 2 - 0.33f * texture.getWidth(),
                    offset.getY() + pointDeparture.getY() - fogOffset - Render.detaHeight - Render.tileHeight);
                tmp.scale(0.22f, 0.22f);
                g2d.drawRenderedImage(texture, tmp);

                tmp =  AffineTransform.getTranslateInstance(
                offset.getX() + pointDeparture.getX() - 0.25f * texture.getWidth() / 2, offset.getY() 
                + pointDeparture.getY() - fogOffset - Render.detaHeight - Render.tileHeight * 0.5f);
                tmp.scale(0.25f, 0.25f);
                g2d.drawRenderedImage(texture, tmp);
                break;
            case "POPULATION-Bardur":
            case "POPULATION-Imperius":
            case "POPULATION-Oumaji":
            case "POPULATION-Xinxi":
                ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
                g2d.setComposite(ac2);
                tmp =  AffineTransform.getTranslateInstance(
                offset.getX() + pointDeparture.getX() - 0.15f * texture.getWidth() / 2, offset.getY() 
                + pointDeparture.getY() - Render.detaHeight - Render.tileHeight - 0.15f* texture.getHeight());
                tmp.scale(0.15f, 0.15f);
                g2d.drawRenderedImage(texture, tmp);
                break;
            case "STAR":
                ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
                g2d.setComposite(ac2);
                tmp =  AffineTransform.getTranslateInstance(
                offset.getX() + pointDeparture.getX() - 0.25f * texture.getWidth() / 2, offset.getY() 
                + pointDeparture.getY() - Render.detaHeight - Render.tileHeight);
                tmp.scale(0.25f, 0.25f);
                g2d.drawRenderedImage(texture, tmp);
            case "ARROW":
                ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
                g2d.setComposite(ac2);
                tmp =  AffineTransform.getTranslateInstance(
                offset.getX() + pointDeparture.getX(), offset.getY() + pointDeparture.getY() - Render.detaHeight - Render.tileHeight);
                tmp.rotate(offset.getX(), offset.getY());
                tmp.translate(-0.25f * texture.getWidth(), - 0.25f * texture.getHeight() / 2);
                tmp.scale(0.25f, 0.25f);
                g2d.drawRenderedImage(texture, tmp);
                break;
            case "BULLET":
                ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
                g2d.setComposite(ac2);
                tmp =  AffineTransform.getTranslateInstance(
                offset.getX() + pointDeparture.getX() - 0.25f * texture.getWidth() / 2, offset.getY() 
                + pointDeparture.getY() - Render.detaHeight - Render.tileHeight);
                tmp.scale(0.25f, 0.25f);
                g2d.drawRenderedImage(texture, tmp);
                break;
            case "SWORD":
                ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
                g2d.setComposite(ac2);
                tmp =  AffineTransform.getTranslateInstance(
                pointDeparture.getX(), pointDeparture.getY() - Render.detaHeight - Render.tileHeight);
                if (departureTile.getUnit().isFlipped()) tmp.scale(-1.0f, 1.0f);
                tmp.rotate(getRotation());
                tmp.translate(0.f, - 0.80f * texture.getHeight());
                tmp.scale(0.80f, 0.80f);
                g2d.drawRenderedImage(texture, tmp);
                break;
            default:
                return;
        }
    }
}

class StringMotion extends Motion{

    private String string;
    private Color color;

    public StringMotion(String string, Tile t, double start, double deadline, Color color) {
        super(t, start, deadline);
        this.string = string;
        this.color = color;
        setMotionType(MotionType.RISE);
    }

    public void drawString(Graphics2D g2d){

        Point2D offset = getOffset();
        Point2D pointDeparture = Render.camera.transPoint(new Point2D.Double((double)departureTile.getY(), (double)departureTile.getX()));
        AlphaComposite ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - (float)(lastTime / (deadline - startTime)));
        g2d.setComposite(ac2);
        g2d.setFont(new Font("Avenir", Font.BOLD, 64));
        g2d.setColor(Color.black);
        g2d.drawString(string, (int)(offset.getX() + pointDeparture.getX() - string.length() * 16 + 4),
        (int)(offset.getY() + pointDeparture.getY() - Render.detaHeight - Render.tileHeight * 2));
        g2d.setColor(color);
        g2d.drawString(string, (int)(offset.getX() + pointDeparture.getX() - string.length() * 16),
        (int)(offset.getY() + pointDeparture.getY() - Render.detaHeight - Render.tileHeight * 2));
    }
}