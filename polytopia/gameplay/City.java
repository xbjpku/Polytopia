package polytopia.gameplay;


import java.util.ArrayList;


import polytopia.gameplay.Player.Faction;
import polytopia.gameplay.Unit.UnitType;
import polytopia.graphics.Render;
import polytopia.graphics.Render.BoundaryLine;
import polytopia.utils.RandomName;

public class City implements TileVariation {

	private Tile ownerTile;
	private Player ownerPlayer;
	private Action[] actions;

	public Tile getOwnerTile() {return this.ownerTile;}
	public Player getOwnerPlayer() {return this.ownerPlayer;}
    public void setOwnerPlayer(Player player) {this.ownerPlayer = player;}
	public Faction getStyle() {
		return ownerPlayer.getFaction();
	}

	public Action[] getActions() {return this.actions;}
	public City(Tile[][] grid, Tile ownerTile, Player ownerPlayer) {
		this.ownerTile = ownerTile;
		this.ownerPlayer = ownerPlayer;
		this.actions = new Action[] {
            new ActionTrainUnit(this, UnitType.ARCHER), new ActionTrainUnit(this, UnitType.CATAPULT), 
            new ActionTrainUnit(this, UnitType.DEFENDER), new ActionTrainUnit(this, UnitType.KNIGHT), 
            new ActionTrainUnit(this, UnitType.MINDBENDER), new ActionTrainUnit(this, UnitType.RIDER), 
            new ActionTrainUnit(this, UnitType.SWORDSMAN), new ActionTrainUnit(this, UnitType.WARRIOR), 
        };

		this.name = RandomName.roll();
		this.level = 1;
		this.population = 0;
		this.hasWall = false;
		this.hasWorkshop = false;

		ArrayList<Tile> adjTiles = TileMap.getInnerRing(grid, ownerTile.getX(), ownerTile.getY());
		this.territory = new ArrayList<Tile>();
		ownerTile.setOwnerCity(this);
		this.territory.add(ownerTile);
		for (Tile tile : adjTiles)
			if (tile.getOwnerCity() == null) {
				tile.setOwnerCity(this);
				this.territory.add(tile);
			}

		this.units = new ArrayList<Unit>();
	}

    public ArrayList<BoundaryLine> getBoundary(Player viewingPlayer){
        ArrayList<BoundaryLine> lines = new ArrayList<BoundaryLine>();
        int size = Game.map.getSize();
        boolean[][] terri = new boolean[size][size];
        for(Tile t : territory){
            terri[t.getY()][t.getX()] = true;
        }
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(terri[i][j] == false
					|| !viewingPlayer.getVision().contains(Game.map.getGrid()[i][j])) 
					continue;
                if(i - 1 < 0 || terri[i - 1][j] == false) lines.add(new BoundaryLine(i, j, BoundaryLine.Side.LEFTUP));
                if(i + 1 >= size || terri[i + 1][j] == false) lines.add(new BoundaryLine(i, j, BoundaryLine.Side.RIGHTDOWN));
                if(j - 1 < 0 || terri[i][j - 1] == false) lines.add(new BoundaryLine(i, j, BoundaryLine.Side.RIGHTUP));
                if(j + 1 >= size || terri[i][j + 1] == false) lines.add(new BoundaryLine(i, j, BoundaryLine.Side.LEFTDOWN));
            }
        }
        return lines;
    }
	private String name;
	private int level;
	private int population;
	private boolean hasWall;
	private boolean hasWorkshop;
	private ArrayList<Tile> territory;
	private ArrayList<Unit> units;

	public String getName() {return this.name;}
	public int getLevel() {return this.level;}
	public int getPopulation() {return this.population;}
	public boolean hasWall() {return this.hasWall;}
	public boolean hasWorkshop() {return this.hasWorkshop;}
	public ArrayList<Tile> getTerritory() {return this.territory;}
	public ArrayList<Unit> getUnits() {return this.units;}

    public int getStarsPerTurn() {
        return level + (hasWorkshop ? 1 : 0) + (this == ownerPlayer.getCapital() ? 1 : 0);
    }

	public void setLevel(int level) {this.level = level;}
	public void setPopulation(int population) {this.population = population;}
	public void addWall() {this.hasWall = true;}
	public void addWorkshop() {this.hasWorkshop = true;}


	@Override
	public String toString() {
		return "CITY";
	}

}
