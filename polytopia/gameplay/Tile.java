package polytopia.gameplay;

import java.awt.*;
import java.awt.event.*;
import java.awt.Toolkit.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.imageio.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.InputMismatchException;

import polytopia.graphics.Texture;
import polytopia.graphics.Visualizable;
import polytopia.gameplay.Player.Faction;
import polytopia.gameplay.Player.Tech;


/** A tile on the game map. */
public class Tile implements Visualizable {

	private int x;
	private int y;

	/* Describes terrain of tiles. */
	public enum TerrainType {
		FIELD, FOREST, MOUNTAIN, SHORE, OCEAN
	}
	private TerrainType terrain;

	/* Describes how the tile varies from basic terrain. 
	   Generally, variation can be a Resource, Improvement or City. */
	private TileVariation variation; 

	/* Describes artstyle of this tile, for visualization. 
	   TODO: Change String to Faction. */
	private Faction style;

	/* Describes the unit (if exists) on this tile. */
	private Unit unit;

	/* Describes the owner city of this tile. */
	private City ownerCity;

	private Action[] actions;

	public Tile(int x, int y, TerrainType terrain, TileVariation variation, Faction style) {
		this.x = x;
		this.y = y;
		this.terrain = terrain;
		this.variation = variation;
		this.style = style;
		this.unit = null;
		this.ownerCity = null;

		this.actions = new Action[] {new ActionBuildLumberHut(this), new ActionBuildPort(this), 
									new ActionClearForest(this), new ActionBuildSawmill(this),
									new ActionBuildForge(this), new ActionBuildWindmill(this)};
	}

	public int getX() {return this.x;}
	public int getY() {return this.y;}
	public TerrainType getTerrainType() {return this.terrain;}
	public TileVariation getVariation() {return this.variation;}
	public Faction getStyle() {return this.style;}
	public Unit getUnit() {return this.unit;}
	public void setUnit(Unit unit) {this.unit = unit;}
	public City getOwnerCity() {return this.ownerCity;}
	public boolean hasEnemy(Player player) {
		return this.unit.getOwnerPlayer() != null && this.unit.getOwnerPlayer() != player;
	}
	public boolean hasAlly(Player player) {
		return this.unit.getOwnerPlayer() != null && this.unit.getOwnerPlayer() == player;
	}
	public Action[] getActions() {return this.actions;}

	public void setTerrainType(TerrainType type) {this.terrain = type;}
	public void setVariation(TileVariation variation) {this.variation = variation;}
	public void setStyle(Faction style) {this.style = style;}
	public void setOwnerCity(City city) {this.ownerCity = city;}

	// Selection response
	public void visualize(/* GUI component */) {
		System.out.printf("Tile (%d, %d) selected\n", this.x, this.y);
	}

	// Checks for ownership and unit occupation
	public boolean isOwnedBy(Player player) {
		if (ownerCity != null && ownerCity.getOwnerPlayer() == player) {
			return true;
		}
		return false;
	}

	public boolean hasTemple() {
		return ((Improvement)getVariation()).getImprovementType() == Improvement.ImprovementType.TEMPLE
				|| ((Improvement)getVariation()).getImprovementType() == Improvement.ImprovementType.FORESTTEMPLE
				|| ((Improvement)getVariation()).getImprovementType() == Improvement.ImprovementType.AQUATEMPLE
				|| ((Improvement)getVariation()).getImprovementType() == Improvement.ImprovementType.MOUNTAINTEMPLE;
	}

	public boolean isAccessibleTo(Player player) {
		if (this.isOwnedBy(player)) {
			if (unit == null || unit.getOwnerPlayer() == player)
				return true;
		}
		return false;
	}



	public static void main(String[] args) {
		TilesTest display = new TilesTest();

		/* Makeshift Interactive Commandline, for debugging. */

		/* Dump basic info. */
		for(Player player : Game.players) {
			System.out.println (player.getFaction().toString());
			City capital = player.getCapital();
			System.out.printf ("Capital %s at (%d, %d)\n", capital.getName(), 
								capital.getOwnerTile().getX(), capital.getOwnerTile().getY());

			System.out.println ("Territory:");
			ArrayList<Tile> territory = capital.getTerritory();
			for (Tile tile : territory) {
				System.out.printf ("(%d, %d), %s, with %s\n",
									tile.getX(), tile.getY(), 
									tile.getTerrainType().toString(),
									tile.getVariation() instanceof TileVariation ?
									tile.getVariation().toString() : "nothing");
			}
			System.out.println();
		}

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.printf ("debug> ");
			switch (scanner.nextLine().strip().toLowerCase()) {
				case "i":
				case "info":
					if (Game.currentPlayer == null)
						System.out.println("no current player");
					else {
						System.out.printf ("Current player: %s\n", Game.currentPlayer.getFaction().toString());
						System.out.printf ("\tcities: ");
						for (City city : Game.currentPlayer.getCities())
							System.out.printf ("(%d, %d), ", city.getOwnerTile().getX(), city.getOwnerTile().getY());
						System.out.println();
						System.out.printf ("\tstars: %d\n", Game.currentPlayer.getStars());
						System.out.printf ("\ttechs: ");
						for (Tech tech : Game.currentPlayer.getTechs())
							System.out.printf ("%s, ", tech.toString());
						System.out.println();
					}
					break;
				case "p":
				case "player":
					for (int i = 0; i < Game.players.length; i++) {
						System.out.printf ("%d: %s\n", i, Game.players[i].getFaction().toString());
					}
					System.out.printf("switch to: ");
					int nextPlayer = 0;
					try {nextPlayer = scanner.nextInt();}
					catch (InputMismatchException e){break;}
					if (nextPlayer >= 0 && nextPlayer < Game.players.length) {
						Game.currentPlayer = Game.players[nextPlayer];
						System.out.printf ("current player switched to %s\n", Game.currentPlayer.getFaction().toString());
					}
					else
						System.out.println ("invalid player number");
					break;
				case "s":
				case "star":
					if (Game.currentPlayer == null)
						System.out.println("no current player");
					else {
						System.out.printf ("new star count: ");
						int newStar = 0;
						try {newStar = scanner.nextInt();}
						catch (InputMismatchException e){break;}
						Game.currentPlayer.setStars (newStar);
					}
					break;
				case "t":
				case "tech":
					if (Game.currentPlayer == null)
						System.out.println("no current player");
					else {
						System.out.printf ("new tech (ALL for all): ");
						String newTech = scanner.next().strip();
						System.out.println(newTech);
						if (newTech.equals("ALL")) {
							for (Tech tech : Tech.values())
								if (!Game.currentPlayer.getTechs().contains(tech))
									Game.currentPlayer.addTech(tech);
						}
						else {
							try {
								Tech tech = Tech.valueOf(newTech);
								if (!Game.currentPlayer.getTechs().contains(tech))
									Game.currentPlayer.addTech(tech);
							} catch (IllegalArgumentException e) {
								System.out.println ("invalid Tech name");
							}
						}
					}
					break;
				case "q":
				case "query":
					if (Game.currentPlayer == null) {
						System.out.println("no current player");
						break;
					}

					System.out.printf("(x,y) of query: ");
					int x,y;
					try {x = scanner.nextInt(); y = scanner.nextInt();}
					catch (InputMismatchException e) {break;}
					if (!TileMap.isValid(Game.map.getGrid(), x, y))
						System.out.printf ("(%d, %d) is not on map", x, y);
					else {
						Tile tile = Game.map.getGrid()[x][y];
						System.out.printf ("(%d, %d), %s, with %s\n",
									tile.getX(), tile.getY(), 
									tile.getTerrainType().toString(),
									tile.getVariation() != null ?
									tile.getVariation().toString() : "nothing");
						if (tile.getVariation() instanceof Improvement) {
							Improvement improvement = (Improvement) (tile.getVariation());
							System.out.printf ("%s at level %d\n", improvement.toString(), improvement.getLevel());
						}
						else if (tile.getVariation() instanceof City) {
							City city = (City)(tile.getVariation());
							System.out.printf ("City %s at (%d, %d)\n", city.getName(), 
												tile.getX(), tile.getY());
							System.out.printf ("Level: %d\t Population: %d\n", city.getLevel(), city.getPopulation());

							System.out.println ("Territory:");
							ArrayList<Tile> territory = city.getTerritory();
							for (Tile t : territory) {
								System.out.printf ("(%d, %d), %s, with %s\n",
													t.getX(), t.getY(), 
													t.getTerrainType().toString(),
													t.getVariation() instanceof TileVariation ?
													t.getVariation().toString() : "nothing");
							}
							System.out.println();
						}

						ArrayList<Action> actions = new ArrayList<>();
						if (tile.getVariation() != null) {
							for (Action action : tile.getVariation().getActions()) {
								if (action.isVisibleTo (Game.currentPlayer))
									actions.add(action);
								
							}
						}
						for (Action action : tile.getActions()) {
							if (action.isVisibleTo (Game.currentPlayer))
								actions.add(action);
						}
						if (actions.size() == 0) {
							System.out.printf ("no visible actions\n");
							break;
						}
						
						System.out.printf ("list of actions: \n");
						for (int idx = 0; idx < actions.size(); idx++) {
							Action action = actions.get(idx);
							System.out.printf ("(%d) %s, %s\n", idx, 
												action.isPerformableTo (Game.currentPlayer) ? "√" : "x", 
												action.toString());
							ArrayList<Consequence> conseqs = action.getConsequences(Game.currentPlayer);
							System.out.printf ("\t");
							for (Consequence c : conseqs) {
								System.out.printf ("-> %s ", c.toString());
							}
							System.out.printf ("\n");
						}

						System.out.printf ("pick action (-1 to skip): ");
						int actionIdx = -1;
						try {actionIdx = scanner.nextInt();}
						catch (InputMismatchException e) {break;}
						if (actionIdx >= 0 && actionIdx < actions.size()) {
							if (actions.get(actionIdx).isPerformableTo (Game.currentPlayer))
								actions.get(actionIdx).apply(Game.currentPlayer);
							else
								System.out.println ("action not performable");
						}
					}
					break;
				case "":
					break;
				default:
					System.out.println ("unknown command\n"+
										"Usage:\n"+
										"\t info(i) for dumping current player info\n"+
										"\t player(p) for changing current player\n"+
										"\t star(s) for setting current player star count\n"+
										"\t tech(t) for setting current player tech\n"+
										"\t query(q) for selecting tile, and more actions\n");
			}

			// refresh the graphics
			display.update();
		}
	}
}

class TilesTest {

	private Object cond = new Object();
	private boolean loaded = false;
	private TestCanvas canvas = null;

	public void update() {
		this.canvas.repaint();
	}

	class TestCanvas extends JPanel {

			/* Camera related. */
			private int cameraX = 400;
			private int cameraY = 200;
			private int cameraZoom = 0; 
			
			private final int cameraMinZoom = -70;
			private final int cameraMaxZoom = 140;
			private final double cameraMinScale = 0.2;
			private final double cameraMaxScale = 0.5;

			private int dragStartX = 0;
			private int dragStartY = 0;
			private int dragEndX = 0;
			private int dragEndY = 0;


			public TestCanvas() {
				/* Makeshift camera. */
				addMouseListener(new MouseAdapter() {
            		public void mousePressed(MouseEvent e) {
                		dragStartX = e.getX();
						dragStartY = e.getY();
						dragEndX = dragStartX;
						dragEndY = dragStartY;
            		}
        		});

				addMouseListener(new MouseAdapter() {
            		public void mouseReleased(MouseEvent e) {
						cameraX += dragEndX - dragStartX;
						cameraY += dragEndY - dragStartY;
                		dragStartX = 0;
						dragStartY = 0;
						dragEndX = 0;
						dragEndY = 0;
						repaint();
            		}
        		});

				addMouseWheelListener(new MouseAdapter() {
            		public void mouseWheelMoved(MouseWheelEvent e) {
						cameraZoom += e.getWheelRotation();
						if (cameraZoom < cameraMinZoom)
							cameraZoom = cameraMinZoom;
						if (cameraZoom > cameraMaxZoom)
							cameraZoom = cameraMaxZoom;
						repaint();
            		}
        		});

        		addMouseMotionListener(new MouseAdapter() {
            		public void mouseDragged(MouseEvent e) {
                		dragEndX = e.getX();
						dragEndY = e.getY();
						repaint();
            		}
       			});
			}

			@Override
        	public void paintComponent(Graphics g) {
				super.paintComponent(g);  

				Graphics2D g2d = (Graphics2D) g;

				g2d.translate(cameraX + (dragEndX - dragStartX), cameraY + (dragEndY - dragStartY));
				double cameraScale = cameraMinScale + (cameraMaxScale - cameraMinScale) * (cameraZoom - cameraMinZoom) 
													/ (cameraMaxZoom - cameraMinZoom);

				g2d.scale(cameraScale, cameraScale);

				final int tileWidth = 292;
				final int tileHeight = 87;
				Tile[][] grid = Game.map.getGrid();

				for (int d = 0; d <= grid.length+grid[0].length-2; d++) {
					for (int x = 0; x < grid.length && x <= d; x++) {
						int y = d - x;
						if (y < grid[x].length) {
							Tile tile = grid[x][y];

							// Draw terrain texture
							BufferedImage terrain = Texture.getTerrainTexture(tile);
							int posX = (y - x) * tileWidth/2 - terrain.getWidth()/2;
							int posY = d * tileHeight - terrain.getHeight();
							g2d.drawImage (terrain, null, posX, posY);

							// Draw variation texture
							if(tile.getVariation() != null) {
								int voffset = 210;
								Tile.TerrainType type = tile.getTerrainType();
								if (type == Tile.TerrainType.SHORE || type == Tile.TerrainType.OCEAN)
									voffset = 180;
								
								BufferedImage variation = Texture.getVariationTexture (tile.getVariation());
								posX = (y - x) * tileWidth/2 - variation.getWidth()/2;
								posY = d * tileHeight - variation.getHeight() - voffset + Integer.min(tileHeight, variation.getHeight()/2);
								g2d.drawImage (variation, null, posX, posY);
							}
						}
					}
				}
        	}
		}

	/** For testing the tiling visualization. */
	public TilesTest() {
		
		class TestWindow extends JFrame {
			private JFrame frame = null;

			public TestWindow() {
				frame = new JFrame("Tiles::Test");
				frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
				/* Makeshift: a fixed JPanel to draw on. */
				canvas = new TestCanvas();
				canvas.setSize(3000, 3000);
				canvas.setBackground (Color.BLACK);

				Random rnd = new Random(0);


				/* Use TileMap::MapGenerator::generate() */
				Game.players = new Player[] {new Player("Oumaji")};
				Game.map = new TileMap(12, (int)System.currentTimeMillis(), "CONTINENTS", Game.players);


				frame.setLayout(null);
				frame.add(canvas);
				frame.setVisible(true);

				synchronized (cond) {
					loaded = true;
					cond.notify();
				}
			}
		}
		javax.swing.SwingUtilities.invokeLater(() -> {
            new TestWindow();
        });

		synchronized (cond) {
			while (!loaded) {
				// Wait for the map to be loaded
				try {cond.wait();}
				catch (InterruptedException e) {}
			}
		}
	}
}
