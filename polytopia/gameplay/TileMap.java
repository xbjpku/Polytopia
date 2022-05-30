package polytopia.gameplay;

import java.awt.*;
import java.awt.event.*;
import java.awt.Toolkit.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Random;
import java.util.Collections;

import polytopia.utils.SimplexNoise;
import polytopia.gameplay.Player.Faction;

/** A in-game map. */
public class TileMap {

	/* The grid of tiles. */
	private Tile[][] grid;

	private int size;
	private int seed; 				/* Seed for generating grid. */
	private String mapType;			/* Type of map generation. */
	private boolean needsRefresh; 	/* Whether the map is updated and needs refresh on screen. */


	public TileMap(int size, int seed, String mapType, Player[] players) {
		this.size = size;
		this.seed = seed;
		this.mapType = mapType;

		this.grid = MapGenerator.generate(size, seed, MapGenerator.MapType.valueOf(mapType), players);
	}

	public Tile[][] getGrid() {
		return this.grid;
	}
	public String getMapType() {
		return this.mapType.toString();
	}

	public int GetSize() {
		return this.size;
	}
	public static ArrayList<Tile> getInmostRing(Tile[][] grid, int x, int y) {
		ArrayList<Tile> tiles = new ArrayList<Tile>();
		for (int d = 0; d < inmostRing.length; d++) {
			if (isValid(grid, x + inmostRing[d][0], y + inmostRing[d][1]) 
				&& grid[x + inmostRing[d][0]][y + inmostRing[d][1]] != null)
				tiles.add(grid[x + inmostRing[d][0]][y + inmostRing[d][1]]);
		}
		return tiles;
	}

	public static ArrayList<Tile> getInnerRing(Tile[][] grid, int x, int y) {
		ArrayList<Tile> tiles = new ArrayList<Tile>();
		for (int d = 0; d < innerRing.length; d++) {
			if (isValid(grid, x + innerRing[d][0], y + innerRing[d][1])
				&& grid[x + innerRing[d][0]][y + innerRing[d][1]] != null)
				tiles.add(grid[x + innerRing[d][0]][y + innerRing[d][1]]);
		}
		return tiles;
	}

	public static ArrayList<Tile> getOuterRing(Tile[][] grid, int x, int y) {
		ArrayList<Tile> tiles = new ArrayList<Tile>();
		for (int d = 0; d < outerRing.length; d++) {
			if (isValid(grid, x + outerRing[d][0], y + outerRing[d][1])
				&& grid[x + outerRing[d][0]][y + outerRing[d][1]] != null)
				tiles.add(grid[x + outerRing[d][0]][y + outerRing[d][1]]);
		}
		return tiles;
	}

	public static boolean isValid(Tile[][] grid, int x, int y) {
		return x >= 0 && y >= 0 && x < grid.length && y < grid[0].length;
	}

	public static int getDistance(Tile a, Tile b) {
		return Integer.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getY() - b.getY()));
	}

	public static double getEuclideanDistance(Tile a, Tile b) {
		return Math.sqrt((a.getX() - b.getX())*(a.getX() - b.getX()) + 
						 (a.getY() - b.getY())*(a.getY() - b.getY()));
	}


	private static int[][] inmostRing = {{1,0},{-1,0},{0,1},{0,-1}};
	private static int[][] innerRing = {{1,0},{-1,0},{0,1},{0,-1}, {1,1},{-1,-1},{-1,1},{1,-1}};
	private static int[][] outerRing = {{-2,-2},{-2,-1},{-2,0},{-2,1},{-2,2},{-1,2},{0,2},{1,2},{2,2},
										{2,1},{2,0},{2,-1},{2,-2},{1,-2},{0,-2},{-1,-2}};
	
	
}


/** Implements map generation. 
	(1) Generate a height map using Simplex Noise. 
	    |- Place water according to height and MAPTYPE.
	    |- Place mountain according to height, proportional to land area.
	(2) Generate forest, using Simplex Noise, proportional to land area.
	(3) Generate villages:
		|- At least one on each continent.
		|- At least one on the coast on each continent.
		|- Spaced by 2~3 tiles.
	(4) Place resources.
		|- Around villages. At least a total of 2 population per city. 
		|- Inner ring more packed than outer ring.
	(5) Place ruins.
		|- Similar to villages, but only with spacing constraint.
	(6) Pick capitals. 
		|- Try at least 5 tiles spacing.
		|- Stylize tiles by Voronoi Diagram from capitals. */
abstract class MapGenerator {
	public enum MapType {
		DRYLAND, LAKES, CONTINENTS, ARCHIPELAGO, RANDOM, 
	}

	public static Tile[][] generate(int size, int seed, MapType mapType, Player[] players) {
		Tile[][] grid = new Tile[size][size];
		Random rnd = new Random(seed);

		/* Step 1 */
		generateHeightTerrain (grid, seed, mapType);

		/* Step 2 */
		generateMoistureTerrain (grid, rnd.nextInt());

		/* Step 3 */
		ArrayList<Tile> villages;
		do {
			villages= generateVillages (grid, seed);
		} while (villages.size() < players.length);

		/* Step 4 */
		generateResources (grid, seed, villages);

		/* Step 5 */
		generateRuins (grid, seed, villages);

		/* Step 6 */
		stylize (grid, seed, players, villages);

		return grid;
	}

	private static void generateHeightTerrain(Tile[][] grid, int seed, MapType mapType) {

		int mapHeight = grid.length;
		int mapWidth = grid[0].length;
		int mapArea = mapHeight * mapWidth;
		Random rnd = new Random(seed);

		double waterRatio;
		final double mountainRatio = 0.15;

		if (mapType == MapType.RANDOM)
			mapType = MapType.values()[rnd.nextInt(4)];
		switch (mapType) {
			case DRYLAND: waterRatio = 0.16; break;
			case LAKES: waterRatio = 0.32; break;
			case CONTINENTS: waterRatio = 0.6; break;
			case ARCHIPELAGO: waterRatio = 0.8; break;
			default: waterRatio = 0.32; break;
		}

		SimplexNoise.seed(seed);
		double[][] heightMap = new double[mapHeight][mapWidth];
		double lowb = 9999999, uppb = -9999990;
		for (int i = 0; i < mapHeight; i++) {
			for (int j = 0; j < mapWidth; j++) {
				double value = SimplexNoise.noise((double)i/5, (double)j/5);
				heightMap[i][j] = value;
				lowb = Double.min(lowb, value);
				uppb = Double.max(uppb, value);
			}
		}

		double l ,r ,mid;
		final double err = 0.01;
		
		// Pick a height, so that waterRatio is satisfied
		l = lowb; r = uppb; mid = (l + r) / 2;
		while (r - l > err) {
			int cnt = 0;
			for (int i = 0; i < mapHeight; i++) 
				for (int j = 0; j < mapWidth; j++) 
					if (heightMap[i][j] < mid)
						cnt++;
			
			if ((double)cnt / mapArea < waterRatio)
				l = mid;
			else
				r = mid;
			mid = (l + r) / 2;
		}

		double waterLevel = mid;
		int landArea = mapArea;
		for (int i = 0; i < mapHeight; i++) 
			for (int j = 0; j < mapWidth; j++) 
				if (heightMap[i][j] < waterLevel)
					landArea--;

		// For non-water tiles, pick a height so that mountainRatio is satisfied
		l = waterLevel; r = uppb; mid = (l + r) / 2;
		while (r - l > err) {
			int cnt = 0;
			for (int i = 0; i < mapHeight; i++) 
				for (int j = 0; j < mapWidth; j++) 
					if (heightMap[i][j] > mid)
						cnt++;
			
			if ((double)cnt / landArea < mountainRatio)
				r = mid;
			else
				l = mid;
			mid = (l + r) / 2;
		}
		double mountainLevel = mid;

		// Finally, create Tiles
		for (int i = 0; i < mapHeight; i++) {
			for (int j = 0; j < mapWidth; j++) {
				if (heightMap[i][j] > mountainLevel) {
					// Drop MOUNTAIN if too many mountains in vicinity
					int adjMountainCnt = 0;
					ArrayList<Tile> tiles = TileMap.getInnerRing(grid, i, j);
					for (Tile tile : tiles)
						if (tile.getTerrainType() == Tile.TerrainType.MOUNTAIN)
							adjMountainCnt++;
					
					if (rnd.nextInt(5) < adjMountainCnt)
						grid[i][j] = new Tile(i, j, Tile.TerrainType.FIELD, null, null);
					else
						grid[i][j] = new Tile(i, j, Tile.TerrainType.MOUNTAIN, null, null);
				}
				else if (heightMap[i][j] >= waterLevel)
					grid[i][j] = new Tile(i, j, Tile.TerrainType.FIELD, null, null);
			}
		}
		for (int i = 0; i < mapHeight; i++) {
			for (int j = 0; j < mapWidth; j++) {
				if (heightMap[i][j] < waterLevel) {
					boolean isShore = false;
					ArrayList<Tile> tiles = TileMap.getInmostRing(grid, i, j);
					for (Tile tile : tiles)
						if (tile.getTerrainType() == Tile.TerrainType.FIELD 
							|| tile.getTerrainType() == Tile.TerrainType.MOUNTAIN)
							isShore = true;
					
					if(isShore)
						grid[i][j] = new Tile(i, j, Tile.TerrainType.SHORE, null, null);
					else
						grid[i][j] = new Tile(i, j, Tile.TerrainType.OCEAN, null, null);
				}
					
			}
		}
	}

	private static void generateMoistureTerrain(Tile[][] grid, int seed) {

		int mapHeight = grid.length;
		int mapWidth = grid[0].length;
		int landArea = 0;
		final double forestRatio = 0.4;

		SimplexNoise.seed(seed);
		double[][] moistureMap = new double[mapHeight][mapWidth];
		double lowb = 9999999, uppb = -9999990;
		for (int i = 0; i < mapHeight; i++) {
			for (int j = 0; j < mapWidth; j++) {
				if(grid[i][j].getTerrainType() == Tile.TerrainType.FIELD)
					landArea++;
				double value = SimplexNoise.noise((double)i/3, (double)j/3);
				moistureMap[i][j] = value;
				lowb = Double.min(lowb, value);
				uppb = Double.max(uppb, value);
			}
		}

		double l ,r ,mid;
		final double err = 0.01;
		
		// Pick a moisture, so that forestRatio is satisfied
		l = lowb; r = uppb; mid = (l + r) / 2;
		while (r - l > err) {
			int cnt = 0;
			for (int i = 0; i < mapHeight; i++) 
				for (int j = 0; j < mapWidth; j++) 
					if (moistureMap[i][j] > mid && grid[i][j].getTerrainType() == Tile.TerrainType.FIELD)
						cnt++;
			if ((double)cnt / landArea < forestRatio)
				r = mid;
			else
				l = mid;
			mid = (l + r) / 2;
		}

		// Finally, convert FIELD to FOREST
		double forestLevel = mid;
		for (int i = 0; i < mapHeight; i++) 
			for (int j = 0; j < mapWidth; j++) 
				if (moistureMap[i][j] > forestLevel && grid[i][j].getTerrainType() == Tile.TerrainType.FIELD)
					grid[i][j].setTerrainType(Tile.TerrainType.FOREST);

	}

	private static ArrayList<Tile> generateVillages(Tile[][] grid, int seed) {

		ArrayList<Tile> villages = new ArrayList<Tile>();
		Random rnd = new Random(seed); 
		
		// search for continents
		boolean vis[][] = new boolean[grid.length][grid[0].length];
		boolean infeasible[][] = new boolean[grid.length][grid[0].length];
		for (int i = 0; i < grid.length; i++) {
			infeasible[i][0] = true;
			infeasible[i][grid[0].length-1] = true;
		}
		for (int i = 0; i < grid[0].length; i++) {
			infeasible[0][i] = true;
			infeasible[grid.length-1][i] = true;
		}
		
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0;j < grid[0].length; j++) {

				vis[i][j] = true;
				if (grid[i][j].getTerrainType() == Tile.TerrainType.SHORE ||
					grid[i][j].getTerrainType() == Tile.TerrainType.OCEAN) 
					continue;
				
				// BFS for a continent

				ArrayList<Tile> continent = new ArrayList<Tile>();
				ArrayList<Tile> coastline = new ArrayList<Tile>();
				LinkedList<Tile> queue = new LinkedList<Tile>();
				queue.add(grid[i][j]);
				while (!queue.isEmpty()) {
					Tile tile = queue.removeFirst();
					boolean isCoast = false;
					ArrayList<Tile> vicinity = TileMap.getInnerRing(grid, tile.getX(), tile.getY());
					for (Tile adjTile : vicinity) {
						if (adjTile.getTerrainType() == Tile.TerrainType.SHORE || 
							adjTile.getTerrainType() == Tile.TerrainType.OCEAN)
						{
							isCoast = true;
							continue;
						}
						if (!vis[adjTile.getX()][adjTile.getY()]) {
							queue.add(adjTile);
							vis[adjTile.getX()][adjTile.getY()] = true;
						}
					}
					if (isCoast && !infeasible[tile.getX()][tile.getY()])
						coastline.add(tile);
				}

				// Start by picking one tile along coastline
				if (!coastline.isEmpty()) {
					Tile vill = coastline.get(rnd.nextInt(coastline.size()));
					vill.setVariation(new Resource(vill, Resource.ResourceType.VILLAGE));
					vill.setTerrainType(Tile.TerrainType.FIELD);
					villages.add(vill);
					// villages should not have adjacent OCEAN tile
					for (Tile t : TileMap.getInnerRing(grid, vill.getX(), vill.getY()))
						if (t.getTerrainType() == Tile.TerrainType.OCEAN)
							t.setTerrainType(Tile.TerrainType.SHORE);

					// mark its vicinity infeasible
					int rad = Integer.max(1 + rnd.nextInt(3), 2);
					for (int x = vill.getX()-rad; x <= vill.getX()+rad; x++) 
						for (int y = vill.getY()-rad; y <= vill.getY()+rad; y++) 
							if (TileMap.isValid(grid, x, y)) 
								infeasible[x][y] = true;
				}

				// Then go through the rest of the continent
				Collections.shuffle(continent, rnd);
				for (Tile tile : continent) {
					if (infeasible[tile.getX()][tile.getY()])
						continue;
					tile.setVariation(new Resource(tile, Resource.ResourceType.VILLAGE));
					tile.setTerrainType(Tile.TerrainType.FIELD);
					villages.add(tile);
					for (Tile t : TileMap.getInnerRing(grid, tile.getX(), tile.getY()))
						if (t.getTerrainType() == Tile.TerrainType.OCEAN)
							t.setTerrainType(Tile.TerrainType.SHORE);
					int rad =  Integer.min(1 + rnd.nextInt(3), 2);
					for (int x = tile.getX()-rad; x <= tile.getX()+rad; x++) 
						for (int y = tile.getY()-rad; y <= tile.getY()+rad; y++) 
							if (TileMap.isValid(grid, x, y)) 
								infeasible[x][y] = true;
				}
			}
		}

		return villages;
	}

	private static void generateResources(Tile[][] grid, int seed, ArrayList<Tile> villages) {
		Random rnd = new Random(seed);
		final int innerMinValue = 2;
		final int totalMinValue = 5;
		final int[] excess = {1, 1, 2, 2, 2, 3};
		for (Tile vill : villages) {

			// Place resources on inner ring
			int totalValue = 0;
			int innerMaxValue = innerMinValue + excess[rnd.nextInt(excess.length)];
			ArrayList<Tile> inner = TileMap.getInnerRing (grid, vill.getX(), vill.getY());
			Collections.shuffle(inner, rnd);
			for (Tile tile : inner) {
				if (totalValue < innerMaxValue) {
					switch(tile.getTerrainType()) {
						case FIELD:
							if (rnd.nextInt(2) == 0) {
								tile.setVariation(new Resource(tile, Resource.ResourceType.FRUIT));
								totalValue += 1;
							} else {
								tile.setVariation(new Resource(tile, Resource.ResourceType.CROP));
								totalValue += 2;
							}
							break;
						case FOREST:
							tile.setVariation(new Resource(tile, Resource.ResourceType.ANIMAL));
							totalValue += 1;
							break;
						case MOUNTAIN:
							tile.setVariation(new Resource(tile, Resource.ResourceType.METAL));
							totalValue += 2;
							break;
						case SHORE:
							tile.setVariation(new Resource(tile, Resource.ResourceType.FISH));
							totalValue += 1;
							break;
						default:
							break;
					}
				}
			}

			// Place resources on outer ring
			ArrayList<Tile> outer = TileMap.getOuterRing (grid, vill.getX(), vill.getY());
			for (Tile tile : outer)
				if (tile.getVariation() != null)
					totalValue++;
			Collections.shuffle(outer, rnd);
			for (Tile tile : outer) {
				if (totalValue < totalMinValue + rnd.nextInt(5)) {
					switch(tile.getTerrainType()) {
						case FIELD:
							if (rnd.nextInt(2) == 0) {
								tile.setVariation(new Resource(tile, Resource.ResourceType.FRUIT));
								totalValue += 1;
							} else {
								tile.setVariation(new Resource(tile, Resource.ResourceType.CROP));
								totalValue += 2;
							}
							break;
						case FOREST:
							tile.setVariation(new Resource(tile, Resource.ResourceType.ANIMAL));
							totalValue += 1;
							break;
						case MOUNTAIN:
							tile.setVariation(new Resource(tile, Resource.ResourceType.METAL));
							totalValue += 2;
							break;
						case SHORE:
							tile.setVariation(new Resource(tile, Resource.ResourceType.FISH));
							totalValue += 1;
							break;
						case OCEAN:
							if (rnd.nextInt(5) == 0) {
								tile.setVariation(new Resource(tile, Resource.ResourceType.WHALE));
								totalValue += 1;
							}
							break;
					}
				}
			}
		}
	}

	private static void generateRuins(Tile[][] grid, int seed, ArrayList<Tile> villages) {
		ArrayList<Tile> spots = new ArrayList<Tile>(villages);
		Random rnd = new Random(seed);

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				boolean feasible = true;
				for (Tile tile : spots) {
					if (TileMap.getDistance (tile, grid[i][j]) <= 2 + rnd.nextInt(3)) {
						feasible = false;
						break;
					}
				}

				if(feasible) {
					grid[i][j].setVariation(new Resource(grid[i][j], Resource.ResourceType.RUINS));
					spots.add(grid[i][j]);
				}
			}
		}
	}

	private static void stylize(Tile[][] grid, int seed, Player[] players, ArrayList<Tile> villages) {
		// Run k-means on villages
		// the closest village to each center is picked as capital

		Random rnd = new Random(seed);
		int numPlayers = players.length;
		Collections.shuffle(villages, rnd);

		Tile[] centers = new Tile[numPlayers];
		int[] tags = new int[villages.size()];
		for (int i = 0; i < numPlayers; i++)
			centers[i] = villages.get(i);
			
		boolean converge = false;
		while (!converge) {
			converge = true;

			// reassign
			for (int k = 0; k < villages.size(); k++) {
				int pick = -1;
				double minDist = 2 * grid.length;
				for (int i = 0; i < numPlayers; i++) {
					double dist = TileMap.getEuclideanDistance(villages.get(k), centers[i]);
					if (dist < minDist) {
						minDist = dist;
						pick = i;
					}
				}
				tags[k] = pick;
			}

			// update
			for (int i = 0; i< numPlayers; i++) {
				int cnt = 0;
				double avgX = 0, avgY = 0;
				for (int k = 0; k < villages.size(); k++) {
					if (tags[k] == i) {
						cnt++;
						avgX += villages.get(k).getX();
						avgY += villages.get(k).getY();
					}
				}
				avgX /= cnt;
				avgY /= cnt;

				Tile newCenter = null;
				double minDist = 2 * grid.length;
				for (int k = 0; k < villages.size(); k++) {
					Tile t = villages.get(k);
					double dist = Math.sqrt((avgX-t.getX())*(avgX-t.getX()) + (avgY-t.getY())*(avgY-t.getY()));
					if (dist < minDist) {
						minDist = dist;
						newCenter = t;
					}
				}
				if (centers[i] != newCenter) 
					converge = false;
				centers[i] = newCenter;
			}
		}

		// TODO: mark centers as capitals
		for (int i = 0; i < numPlayers; i++) {
			Tile t = centers[i];
			City capital = new City(grid, t, players[i]);
			t.setVariation(capital);
			players[i].setCapital(capital);
			players[i].addCity(capital);
		}

		// stylize tiles based on capitals
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {

				if (grid[i][j].getTerrainType() == Tile.TerrainType.SHORE ||
					grid[i][j].getTerrainType() == Tile.TerrainType.OCEAN)
					continue;

				int pick = -1;
				double minDist = 2 * grid.length;
				for (int k = 0; k < numPlayers; k++) {
					double dist = TileMap.getEuclideanDistance(centers[k], grid[i][j]);
					if (dist < minDist) {
						minDist = dist;
						pick = k;
					}
				}
				grid[i][j].setStyle(players[pick].getFaction());
			}
		}

	}
	
}
