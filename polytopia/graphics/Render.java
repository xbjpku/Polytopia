package polytopia.graphics;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import polytopia.gameplay.*;

public class Render {

    static final int tileWidth = 292;
    static final int tileHeight = 87;
    final static int detaHeight = 104;
    static final double tileSideLength = Math.sqrt(tileWidth * tileWidth / 4 + tileHeight * tileHeight);
    static final double theta = Math.acos(1 - (tileWidth * tileWidth) / (2 * tileSideLength * tileSideLength));

    public static Camera camera = new Camera(600, 100, (Math.PI - theta) / 2, tileSideLength, tileSideLength, theta, 0.3f);;
    private static Tile selected = null;
    
    private static Vector<Motion> motions = new Vector<Motion>();

    public enum Decoration {
        UNIT_MOVE, UNIT_ATTACK;
    }
    private static Decoration[][] decorationMap;
    static {
        decorationMap = new Decoration[Game.map.getSize()][Game.map.getSize()];
    }

    public static void setSelected(Tile tile){
        selected = tile;
    }
    public static Tile getSelected(){
        return selected;
    }

    public static void render(Graphics2D g2d){
        Tile[][] grid = Game.map.getGrid();
        camera.updateCamera();
        camera.setGraphics2D(g2d);
        double currentTime = System.currentTimeMillis();
        synchronized(motions) {
            Iterator<Motion> iterator = motions.iterator();
            while(iterator.hasNext()){
                Motion m = iterator.next();
                if (m.update(currentTime)){
                    iterator.remove();
                    if (m instanceof MovableMotion) {
                        MovableMotion vm = (MovableMotion) m;
                        vm.getSubject().setMotion(null);
                    }
                }
            }
        }
        for (int d = 0; d <= grid.length+grid[0].length-2; d++) {
            for (int x = 0; x < grid.length && x <= d; x++) {
                int y = d - x;
                if (y < grid[x].length) {
                    Tile tile = grid[y][x];
                    Player humanPlayer = Game.getHumanPlayer();

                    if (!humanPlayer.getVision().contains(tile)) {
                        // Draw FOG instead
                        BufferedImage fog = Texture.getTextureByName("FOG");
                        Point2D point = camera.transPoint(new Point2D.Double((double)x, (double)y));
                        if (tile.getMotion() != null) {
                            Point2D motionOffset = tile.getMotion().getOffset();
                            point.setLocation(point.getX() + motionOffset.getX(), point.getY() + motionOffset.getY());
                        }

                        g2d.drawImage (fog, null, (int)point.getX() - fog.getWidth() / 2, (int)point.getY() - fog.getHeight());
                        continue;
                    }

                    // Draw terrain texture
                    BufferedImage terrain = Texture.getTerrainTexture(tile);
                    Point2D point = camera.transPoint(new Point2D.Double((double)x, (double)y));
                    Tile.TerrainType type = tile.getTerrainType();
                
                    if (tile.getMotion() != null) {
                        Point2D motionOffset = tile.getMotion().getOffset();
                        point.setLocation(point.getX() + motionOffset.getX(), point.getY() + motionOffset.getY());
                        
                    }
                    
                    g2d.drawImage (terrain, null, (int)point.getX() - terrain.getWidth() / 2, (int)point.getY() - terrain.getHeight());

                    
                    // Draw variation texture
                    if(tile.getVariation() != null) {
                        int voffset = 210;
                        if (type == Tile.TerrainType.SHORE || type == Tile.TerrainType.OCEAN)
                            voffset = 180;
                        
                        BufferedImage variation = Texture.getVariationTexture (tile.getVariation());
                        g2d.drawImage (variation, null, (int)point.getX() - variation.getWidth()/2, (int)point.getY() - variation.getHeight() - voffset + Integer.min(tileHeight, variation.getHeight()/2));
                    }
                }
            }
        }


        if(selected != null) {
            Tile.TerrainType type = selected.getTerrainType();
            BufferedImage terrain = Texture.getTerrainTexture(selected);
            Point2D point = camera.transPoint(new Point2D.Double((double)selected.getY(), (double)selected.getX()));
            g2d.setStroke(new BasicStroke(10.0f));
            g2d.setColor(Color.gray);
            int voffset = 0;
            if (type == Tile.TerrainType.SHORE || type == Tile.TerrainType.OCEAN)
                voffset = 20;
            int downPointX = (int)point.getX();
            int downPointY = (int)point.getY() - detaHeight + voffset;
            g2d.drawLine(downPointX, downPointY, downPointX + terrain.getWidth() / 2, downPointY - tileHeight);
            g2d.drawLine(downPointX, downPointY, downPointX - terrain.getWidth() / 2, downPointY - tileHeight);
            g2d.drawLine(downPointX, downPointY - 2 * tileHeight, downPointX + terrain.getWidth() / 2, downPointY - tileHeight);
            g2d.drawLine(downPointX, downPointY - 2 * tileHeight, downPointX - terrain.getWidth() / 2, downPointY - tileHeight);

            if(selected.getVariation() instanceof City){
                City c = (City)selected.getVariation();
                ArrayList<BoundaryLine> boundary = c.getBoundary(Game.getHumanPlayer());
                for(BoundaryLine line : boundary){
                    line.draw(g2d, Color.gray, 15.0f);
                }
            }
        }

        for(Player player : Game.players){
            presentPlayerCities(g2d, player);
            ArrayList<Unit> units = player.getUnits();
            for(Unit u : units){
                Tile tile = u.getPosition();
                if (!Game.getHumanPlayer().getVision().contains(tile))
                    continue;
                Point2D point = camera.transPoint(new Point2D.Double((double)tile.getY(), (double)tile.getX()));
                Tile.TerrainType type = tile.getTerrainType();

                if (u.getMotion() != null) {
                    Point2D motionOffset = u.getMotion().getOffset();
                    point.setLocation(point.getX() + motionOffset.getX(), point.getY() + motionOffset.getY());
                }

                int voffset = 210;
                if (type == Tile.TerrainType.SHORE || type == Tile.TerrainType.OCEAN)
                    voffset = 180;
                
                BufferedImage unitTexture = Texture.getTextureByName(u.toString());
                AffineTransformOp op = u.isFlipped() ? new AffineTransformOp(new AffineTransform(-1, 0, 0, 1, unitTexture.getWidth(), 0),null) : null;
                g2d.drawImage (unitTexture, op, (int)point.getX() - unitTexture.getWidth()/2, 
                (int)point.getY() - unitTexture.getHeight() - voffset + Integer.min(tileHeight, unitTexture.getHeight()/2));
            }
        }

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        for(int i = 0; i < Game.map.getSize(); i++){
            for(int j = 0; j < Game.map.getSize(); j++){
                if(decorationMap[i][j] != null){
                    Point2D point = camera.transPoint(new Point2D.Double((double)i, (double)j));
                    BufferedImage markTexture;
                    markTexture = Texture.getTextureByName("MOVEMENT");
                    if(decorationMap[i][j] == Decoration.UNIT_MOVE){
                        markTexture = Texture.getTextureByName("MOVEMENT");
                    }
                    if(decorationMap[i][j] == Decoration.UNIT_ATTACK){
                        markTexture = Texture.getTextureByName("ATTACK");
                    }
                    g2d.drawImage (markTexture, null, (int)point.getX() - markTexture.getWidth()/2, 
                    (int)point.getY() - markTexture.getHeight() / 2 - tileHeight - detaHeight);
                }
            }
        }
        synchronized(motions) {
            for(Motion m : motions){
                if(m instanceof TextureMotion){
                    TextureMotion tm = (TextureMotion) m;
                    tm.drawTexture(g2d);
                }
            }
        }
    }

    public static class BoundaryLine{
        public static enum Side{
            LEFTUP, LEFTDOWN, RIGHTUP, RIGHTDOWN
        } 
        int x;
        int y;
        Side side;

        public BoundaryLine(int a, int b, Side s) {
            x = a;
            y = b;
            side = s;
        }

        void draw(Graphics2D g2d, Color c, float strokeWidth){
            Point2D point = camera.transPoint(new Point2D.Double((double)x, (double)y));
            g2d.setStroke(new BasicStroke(strokeWidth));
            g2d.setColor(c);
            int downPointX = (int)point.getX();
            int downPointY = (int)point.getY() - detaHeight;
            switch (side){
                case LEFTUP:
                g2d.drawLine(downPointX, downPointY - 2 * tileHeight, downPointX - tileWidth / 2, downPointY - tileHeight);
                break;
                case RIGHTUP:
                g2d.drawLine(downPointX, downPointY - 2 * tileHeight, downPointX + tileWidth / 2, downPointY - tileHeight);
                break;
                case LEFTDOWN:
                g2d.drawLine(downPointX, downPointY, downPointX - tileWidth / 2, downPointY - tileHeight);
                break;
                case RIGHTDOWN:
                g2d.drawLine(downPointX, downPointY, downPointX + tileWidth / 2, downPointY - tileHeight);
                break;
            }
        }

        void draw(Graphics2D g2d, Color c, float strokeWidth, float alpha){
            Point2D point = camera.transPoint(new Point2D.Double((double)x, (double)y));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setStroke(new BasicStroke(strokeWidth));
            g2d.setColor(c);
            int downPointX = (int)point.getX();
            int downPointY = (int)point.getY() - detaHeight;
            switch (side){
                case LEFTUP:
                g2d.drawLine(downPointX, downPointY - 2 * tileHeight, downPointX - tileWidth / 2, downPointY - tileHeight);
                break;
                case RIGHTUP:
                g2d.drawLine(downPointX, downPointY - 2 * tileHeight, downPointX + tileWidth / 2, downPointY - tileHeight);
                break;
                case LEFTDOWN:
                g2d.drawLine(downPointX, downPointY, downPointX - tileWidth / 2, downPointY - tileHeight);
                break;
                case RIGHTDOWN:
                g2d.drawLine(downPointX, downPointY, downPointX + tileWidth / 2, downPointY - tileHeight);
                break;
            }
        }
    }
    
    static private void presentPlayerCities(Graphics2D g2d, Player p){
        ArrayList<BoundaryLine> boundary = p.getBoundary(Game.getHumanPlayer());
        for(BoundaryLine line : boundary){
            line.draw(g2d, p.getFaction().themeColor, 20.0f);
        }
        ArrayList<City> cities = p.getCities();
        for(City c : cities){
            if (Game.getHumanPlayer().getVision().contains(c.getOwnerTile()))
                presentCity(g2d, c);
        }
    }

    static private void presentCity(Graphics2D g2d, City c){
        Point2D point = camera.transPoint(new Point2D.Double((double)c.getOwnerTile().getY(), (double)c.getOwnerTile().getX()));
        int level = c.getLevel();
        int population = c.getPopulation();
        String nameOfCity = c.getName();
        
        int bias = 5;
        Color color = c.getOwnerPlayer().getFaction().themeColor;
        //AlphaComposite ac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 0.5f);
        //g2d.setComposite(ac2);
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
        g2d.fill3DRect((int)point.getX() - tileWidth / 2, (int)point.getY() - tileHeight - 30, tileWidth - bias, 40,false);

        g2d.setFont(new Font("Avenir", Font.BOLD, 32));
        g2d.setColor(Color.black);
        g2d.drawString(nameOfCity, (int)point.getX() - nameOfCity.length() * 8+2, (int)point.getY() - tileHeight+2);
        g2d.setColor(Color.white);
        g2d.drawString(nameOfCity, (int)point.getX() - nameOfCity.length() * 8, (int)point.getY() - tileHeight);
        

        int recLength = tileWidth / (level + 1);
        for(int i = 0; i < level + 1; i++){
            if (population >= 0) {
                if(i < population) 
                    g2d.setColor(Color.cyan);
                else
                    g2d.setColor(Color.white);
            } 
            else {
                if(i < -population)
                    g2d.setColor(Color.red);
                else
                    g2d.setColor(Color.white);
            }
            
            g2d.fill3DRect((int)point.getX() - tileWidth / 2 + i * recLength, (int)point.getY() - tileHeight + 15, recLength - bias, 40,false);
        }
    }

    static public void addMotion(Motion m){
        synchronized(motions){
            motions.add(m);
        }
    }
    static public void removeMotion(Motion m){
        synchronized(motions){
            motions.remove(m);
        }
    }

    static public void clearDecorationMap(){
        for (int i = 0; i < Game.map.getSize(); i++)
            for (int j = 0; j < Game.map.getSize(); j++)
                decorationMap[i][j] = null;
    }

    static public void setDecorationMap(int x, int y, Decoration type){
        if (TileMap.isValid(Game.map.getGrid(), x, y))
            decorationMap[x][y] = type;
    }
}
