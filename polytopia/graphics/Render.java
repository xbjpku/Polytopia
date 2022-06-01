package polytopia.graphics;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.ArrayList;

import polytopia.gameplay.Tile;
import polytopia.gameplay.TileVariation;
import polytopia.gameplay.City;
import polytopia.gameplay.Game;

public class Render {

    static final int tileWidth = 292;
    static final int tileHeight = 87;
    final static int detaHeight = 104;
    static final double tileSideLength = Math.sqrt(tileWidth * tileWidth / 4 + tileHeight * tileHeight);
    static final double theta = Math.acos(1 - (tileWidth * tileWidth) / (2 * tileSideLength * tileSideLength));

    public static Camera camera = new Camera(600, 100, (Math.PI - theta) / 2, tileSideLength, tileSideLength, theta, 0.3f);;
    private static Tile selected;
    private static ArrayList<Motion> motions = new ArrayList<Motion>();

    public static void setSelcected(Tile tile){
        selected = tile;
    }

    public static void render(Graphics2D g2d){
        Tile[][] grid = Game.map.getGrid();
        camera.setGraphics2D(g2d);
        for (int d = 0; d <= grid.length+grid[0].length-2; d++) {
            for (int x = 0; x < grid.length && x <= d; x++) {
                int y = d - x;
                if (y < grid[x].length) {
                    Tile tile = grid[y][x];

                    // Draw terrain texture
                    BufferedImage terrain = Texture.getTerrainTexture(tile);
                    Point2D point = camera.transPoint(new Point2D.Double((double)x, (double)y));
                    Tile.TerrainType type = tile.getTerrainType();

                    
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
            g2d.setColor(Color.pink);
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
                presentCity(g2d, (City)selected.getVariation());
            }
        }


        for (Motion m : motions){

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
    }
    
    static private void presentCity(Graphics2D g2d, City c){
        ArrayList<BoundaryLine> boundary = c.getBoundary();
        for(BoundaryLine line : boundary){
            line.draw(g2d, Color.pink, 20.0f);
        }
    }
}


abstract class Motion {
    Visualizable subject;
    Tile departureTile;
    Tile destinationTile;
    public enum MotionType{
        RISE, SHAKE, JUMP
    } 
}
