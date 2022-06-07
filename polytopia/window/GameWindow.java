package polytopia.window;

import java.awt.*;
import java.awt.event.*;
import java.awt.Toolkit.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.imageio.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
import java.util.InputMismatchException;

import polytopia.gameplay.Action;
import polytopia.graphics.Visualizable;
import polytopia.graphics.Render;
import polytopia.graphics.Motion;
import polytopia.graphics.Movable;
import polytopia.graphics.Texture;
import polytopia.gameplay.Player.Faction;
import polytopia.gameplay.Player.Tech;
import polytopia.gameplay.*;
import polytopia.utils.CircleButton;

public class GameWindow {

	private Object cond = new Object();
	private boolean loaded = false;
	private GameCanvas canvas = null;
    private JFrame frame = null;

    private JPanel stats = null;
    private JLabel labelStarsText;
    private JLabel labelTurnText;
    private JLabel labelStarsNumber;
    private JLabel labelTurnNumber;
    private JPanel actionPanel;
    private JPanel buttonPanel;

	private Tile selectedTile = null;
	private Unit selectedUnit = null;
	private boolean preferUnit = true;
	
    private boolean inAction = false;

    public boolean isInAction() {return inAction;}
    public void setInAction(boolean value) {inAction = value;}

    public void cancelSelection() {
        selectedUnit = null;
        selectedTile = null;
        preferUnit = true;
        Render.clearDecorationMap();

        buttonPanel.setVisible(true);
        actionPanel.setVisible(false);
    }

    public void showActions(ArrayList<Action> actions) {

        /* Action Panel */
        actionPanel.setVisible(false);
        actionPanel.removeAll();

        if (actions.size() == 0) {
            actionPanel.setLayout(new GridLayout(1, 1));
            actionPanel.setSize(frame.getWidth(), 150);
            actionPanel.setLocation(0, frame.getHeight()-150);

            String hintText = "No Action on ";
            if (selectedUnit != null) {
                hintText = hintText + "Unit " + selectedUnit.toString();
            }
            else if (selectedTile != null) {
                hintText = hintText + String.format("Tile (%d, %d)", selectedTile.getX(), selectedTile.getY());
            }
            else
                hintText = hintText + "Player " + Game.getHumanPlayer().getFaction().toString();
            hintText = hintText + ". Tap empty space to exit.";
            JLabel hint = new JLabel(hintText, JLabel.CENTER);
            hint.setFont(new Font("Avenir", Font.ITALIC, 15));
            hint.setForeground(Color.WHITE);
            hint.setBackground(new Color(0,0,0,0));
            hint.setOpaque(true);
            actionPanel.add(hint);

            buttonPanel.setVisible(false);
            actionPanel.setVisible(true);
            return;
        }

        actionPanel.setLayout(new GridLayout(2, actions.size()));
        actionPanel.setSize(125 * actions.size(), 120);
        actionPanel.setLocation(frame.getWidth()/2 - actionPanel.getWidth()/2, frame.getHeight()-120);

        for (Action action : actions) {
            actionPanel.add(new CircleButton(this, action.toString(), action));
        }
        for (Action action : actions) {
            JLabel label = new JLabel(action.toString(), JLabel.CENTER);
            if (action.getCost() > 0) {
                label.setIcon(new ImageIcon(Texture.getTextureByName("SMALL_STAR")));
                
                if (Action.isTechAction(action) && Action.actionToTech(action).prerequisite != null)              
                    label.setText(String.format("<html> %d %s<br/>Needs: %s</html>", action.getCost(), action.toString(), Action.actionToTech(action).prerequisite.toString()));
                else
                    label.setText(String.valueOf(action.getCost()) + " " + action.toString());
            }
            label.setVerticalAlignment(JLabel.TOP);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setFont(new Font("Avenir", Font.ITALIC, 11));
            label.setForeground(Color.WHITE);
            label.setBackground(new Color(0,0,0,0));
            label.setOpaque(true);
            actionPanel.add(label);
        }

        buttonPanel.setVisible(false);
        actionPanel.setVisible(true);
        
        System.out.printf("%d\n", actions.size());
    }


	public void update() {
        this.labelStarsText.setText(String.format ("Stars (+%d)", Game.getHumanPlayer().getStarsPerTurn()));
        this.labelStarsNumber.setText(String.valueOf(Game.getHumanPlayer().getStars()));
        this.labelTurnNumber.setText(String.valueOf(Game.getTurn()));
        this.canvas.repaint();
	}

	class GameCanvas extends JPanel {
			public GameCanvas() {
				/* Makeshift camera. */
				addMouseListener(new MouseAdapter() {
            		public void mousePressed(MouseEvent e) {
                		Render.camera.setStart(e.getX(), e.getY());
					}
        		});

				addMouseListener(new MouseAdapter() {
            		public void mousePressed(MouseEvent e) {
						
						Point2D des = Render.camera.inverseTransPoint(new Point2D.Double((double)e.getX(), (double)e.getY()));
						int x = (int)Math.ceil(des.getX());
						int y = (int)Math.ceil(des.getY());
						
						Render.clearDecorationMap();
                        Render.setSelected(null);
                        
						if (!TileMap.isValid(Game.map.getGrid(), x, y)) {
							System.out.printf ("(%d, %d) is not on map\n", x, y);

							cancelSelection();
						}
						else {
							if (selectedUnit != null) {
								boolean actionPerformed = false;
								Tile tile = Game.map.getGrid()[y][x];

                                Action unitAction = selectedUnit.pickAction(Game.getCurrentPlayer(), tile);
                                if (unitAction != null && !inAction) {
                                    actionPerformed = true;
					                inAction = true;
                                    new Thread(()->{
                                        unitAction.apply(Game.getCurrentPlayer());
                                        inAction = false;
                                    }).start();
                                }			
								if (actionPerformed) {
                                    cancelSelection();
                                    return;
                                }
							}

							Tile tile = Game.map.getGrid()[y][x];
							selectedTile = tile;
							
							if (tile.getUnit() == null || !preferUnit) {
								preferUnit = true;
								selectedUnit = null;
								tile.visualize();
								if (Game.getHumanPlayer().getVision().contains(tile)){
									Render.setSelected(tile);
									System.out.printf ("(%d, %d), %s, with %s\n",
												tile.getX(), tile.getY(), 
												tile.getTerrainType().toString(),
												tile.getVariation() != null ?
												tile.getVariation().toString() : "nothing");
								}
								else{
                                    cancelSelection();
									System.out.printf ("(%d, %d), FOG\n", tile.getX(), tile.getY());
								}
							}
							else {
								if (Game.getHumanPlayer().getVision().contains(tile)){
									preferUnit = false;
									selectedUnit = tile.getUnit();
									Render.setSelected(tile);
									System.out.printf ("%s at %d health\n", selectedUnit.toString(), selectedUnit.getHealth());
									tile.getUnit().visualize();
								}
								else{
									cancelSelection();
									System.out.printf ("(%d, %d), FOG\n", tile.getX(), tile.getY());
								}
							}
						}
                        
					}
        		});


				addMouseWheelListener(new MouseAdapter() {
            		public void mouseWheelMoved(MouseWheelEvent e) {
						Render.camera.changeScale(e.getWheelRotation());
            		}
        		});

				
        		addMouseMotionListener(new MouseAdapter() {
            		public void mouseDragged(MouseEvent e) {
						Render.camera.changePos(e.getX(), e.getY());
            		}
       			});

				addMouseMotionListener(new MouseAdapter() {
					public void mouseMoved(MouseEvent e) {
						Render.camera.setMousePos(e.getX(), e.getY());
					}
				});
			}

			@Override
        	public void paintComponent(Graphics g) {
				super.paintComponent(g);  

				Graphics2D g2d = (Graphics2D) g;
				Render.render(g2d);
        	}
		}


	public GameWindow() {
		
		class GameFrame extends JFrame {

            /*  Layer 0: Game Canvas 
                Layer 1: Gradient 1
                Layer 2: Gradient 2
                Layer 3: Game stats
                Layer 4: Buttons 
                Layer 5: Action panel. */
			
			public GameFrame() {
				frame = new JFrame("The Battle of Polytopia");
				frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
				canvas = new GameCanvas();
				canvas.setSize(Toolkit.getDefaultToolkit().getScreenSize());
				canvas.setBackground (Color.BLACK);

                frame.setLayout(null);
                frame.getLayeredPane().add(canvas, 0);

                initHUD();
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
                frame.setVisible(true);

                ArrayList<String> draw = new ArrayList<>();
                draw.add("Xinxi");draw.add("Imperius");draw.add("Bardur");draw.add("Oumaji");
                Collections.shuffle(draw, new Random(System.currentTimeMillis()));
                
				Game.start(18, (int) (System.currentTimeMillis()), "RANDOM",
							new String[]{draw.get(0), draw.get(1), draw.get(2)}, GameWindow.this);

				synchronized (cond) {
					loaded = true;
					cond.notify();
				}
                
			}

            private void initHUD() {
                /* Gradient */
                JPanel gradient1 = new JPanel(){
                    @Override
                    public void paint(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g;

                        Color startColor = Color.BLACK;
                        Color endColor = new Color(0,0,0,0);

                        GradientPaint gp = new GradientPaint(frame.getWidth()/2, 0, startColor, frame.getWidth()/2, 200, endColor);
                        g2d.setPaint(gp);
                        g2d.fillRect(0,0,frame.getWidth(),200);
                    }
                };
                gradient1.setSize(frame.getWidth(), 200);
                
                frame.getLayeredPane().setLayer(gradient1, 1);
                frame.getLayeredPane().add(gradient1, 1);

                JPanel gradient2 = new JPanel(){
                    @Override
                    public void paint(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g;

                        Color startColor = new Color(0,0,0,0);
                        Color endColor = Color.BLACK;

                        GradientPaint gp = new GradientPaint(frame.getWidth()/2, 0, startColor, frame.getWidth()/2, 300, endColor);
                        g2d.setPaint(gp);
                        g2d.fillRect(0,0,frame.getWidth(),300);
                    }
                };
                gradient2.setSize(frame.getWidth(), 300);
                gradient2.setLocation(0, frame.getHeight()-300);
                frame.getLayeredPane().setLayer(gradient2, 2);
                frame.getLayeredPane().add(gradient2, 2);

                /* Stats */
                stats = new JPanel(new GridLayout(2, 2));

                labelStarsText = new JLabel("Stars", JLabel.CENTER);
                labelStarsText.setVerticalAlignment(JLabel.BOTTOM);
                labelStarsText.setFont(new Font("Avenir", Font.ITALIC, 12));
                labelStarsText.setForeground(Color.WHITE);
                labelStarsText.setBackground(new Color(0,0,0,0));
                labelStarsText.setOpaque(true);
                stats.add(labelStarsText);

                labelTurnText = new JLabel("Turn", JLabel.CENTER);
                labelTurnText.setVerticalAlignment(JLabel.BOTTOM);
                labelTurnText.setFont(new Font("Avenir", Font.ITALIC, 12));
                labelTurnText.setForeground(Color.WHITE);
                labelTurnText.setBackground(new Color(0,0,0,0));
                labelTurnText.setOpaque(true);
                stats.add(labelTurnText);

                labelStarsNumber = new JLabel("", JLabel.CENTER);
                labelStarsNumber.setIcon(new ImageIcon(Texture.getTextureByName("SMALL_STAR")));
                labelStarsNumber.setFont(new Font("Avenir", Font.ITALIC, 18));
                labelStarsNumber.setForeground(Color.WHITE);
                labelStarsNumber.setBackground(new Color(0,0,0,0));
                labelStarsNumber.setOpaque(true);
                stats.add(labelStarsNumber);

                labelTurnNumber = new JLabel("", JLabel.CENTER);
                labelTurnNumber.setFont(new Font("Avenir", Font.ITALIC, 18));
                labelTurnNumber.setForeground(Color.WHITE);
                labelTurnNumber.setBackground(new Color(0,0,0,0));
                labelTurnNumber.setOpaque(true);
                stats.add(labelTurnNumber);

                stats.setBackground(new Color(0, 0, 0, 0));
                stats.setOpaque(true);

                stats.setSize(200, 80);
                stats.setLocation(frame.getWidth()/2 - stats.getWidth()/2, 0);

                frame.getLayeredPane().setLayer(stats, 3);
                frame.getLayeredPane().add(stats, 3);

                /* Buttons */
                buttonPanel = new JPanel(new GridLayout(2, 3));
                CircleButton buttonSettings = new CircleButton(GameWindow.this, "Exit Game", null);
                CircleButton buttonTech = new CircleButton(GameWindow.this, "Tech Tree", null);
                CircleButton buttonNextTurn = new CircleButton(GameWindow.this, "End Turn", Action.getActionByName("ActionEndTurn"));
                
                buttonPanel.add(buttonSettings);
                buttonPanel.add(buttonTech);
                buttonPanel.add(buttonNextTurn);

                JLabel label1 = new JLabel("Exit Game", JLabel.CENTER);
                label1.setVerticalAlignment(JLabel.TOP);
                label1.setFont(new Font("Avenir", Font.ITALIC, 12));
                label1.setForeground(Color.WHITE);
                label1.setBackground(new Color(0,0,0,0));
                label1.setOpaque(true);
                buttonPanel.add(label1);
                JLabel label2 = new JLabel("Tech Tree", JLabel.CENTER);
                label2.setVerticalAlignment(JLabel.TOP);
                label2.setFont(new Font("Avenir", Font.ITALIC, 12));
                label2.setForeground(Color.WHITE);
                label2.setBackground(new Color(0,0,0,0));
                label2.setOpaque(true);
                buttonPanel.add(label2);
                JLabel label3 = new JLabel("End Turn", JLabel.CENTER);
                label3.setVerticalAlignment(JLabel.TOP);
                label3.setFont(new Font("Avenir", Font.ITALIC, 12));
                label3.setForeground(Color.WHITE);
                label3.setBackground(new Color(0,0,0,0));
                label3.setOpaque(true);
                buttonPanel.add(label3);

                buttonPanel.setSize(300, 120);
                buttonPanel.setLocation(frame.getWidth()/2 - buttonPanel.getWidth()/2, frame.getHeight()-120);
                buttonPanel.setOpaque(true);
                buttonPanel.setBackground(new Color(0,0,0,0));
                frame.getLayeredPane().setLayer(buttonPanel, 4);
                frame.getLayeredPane().add(buttonPanel, 4);

                /* Action Panel */
                /*
                background = new JPanel();
                background.setSize(frame.getWidth(), 150);
                background.setLocation(0, frame.getHeight()-150);
                background.setBackground(Color.BLACK);
                background.setVisible(false);
                frame.getLayeredPane().setLayer(background, 5);
                frame.getLayeredPane().add(background, 5);*/

                actionPanel = new JPanel();
                actionPanel.setBackground(new Color(0,0,0,0));
                actionPanel.setOpaque(true);
                actionPanel.setVisible(false);
                frame.getLayeredPane().setLayer(actionPanel, 5);
                frame.getLayeredPane().add(actionPanel, 5);

            }
		}
		javax.swing.SwingUtilities.invokeLater(() -> {
            new GameFrame();
        });

		synchronized (cond) {
			while (!loaded) {
				// Wait for the map to be loaded
				try {cond.wait();}
				catch (InterruptedException e) {}
			}
		}


		int delay = 15; //milliseconds
  		new Timer(delay, (ActionEvent evt) -> {
			  this.update();
		  }).start();
        
		
		// This starts the game cycle
		Game.getCurrentPlayer().play();
        
	}
}
