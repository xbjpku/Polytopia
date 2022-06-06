package polytopia.utils;

import javax.swing.JButton;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.*;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.RenderingHints;
import java.awt.image.*;
import java.util.ArrayList;


import polytopia.gameplay.Action;
import polytopia.gameplay.Player;
import polytopia.gameplay.Player.Tech;
import polytopia.gameplay.Game;
import polytopia.window.GameWindow;
import polytopia.graphics.Render;
import polytopia.graphics.Texture;

public class CircleButton extends JButton{
	
	private boolean mouseOver = false;
	private boolean mousePressed = false;

	private Action action;
	private GameWindow window;

	public CircleButton(GameWindow window, String text, Action action) {
		super(text);
		setOpaque(false);
		setFocusPainted(false);
		setBorderPainted(false);

		this.action = action;
		this.window = window;
		
		MouseAdapter mouseListener = new MouseAdapter(){
			
			@Override
			public void mousePressed(MouseEvent me){
				if(contains(me.getX(), me.getY())){
					mousePressed = true;
					repaint();
					if (getText().equals("End Turn")) {
						if (Game.getHumanPlayer() != Game.getCurrentPlayer())
							return;
					}
					if (getText().equals("Exit Game")) {
						System.exit(0);
					}
					if (getText().equals("Tech Tree")) {
						if (Game.getHumanPlayer() != Game.getCurrentPlayer())
							return;
						Render.setSelected(null);
						window.cancelSelection();
						ArrayList<Action> actions = new ArrayList<Action>();
						for (Tech tech : Tech.getVisibleTechs(Game.getHumanPlayer())) {
							Action action = Action.getActionByTech(tech);
							action.cost = tech.getCost(Game.getHumanPlayer());
							actions.add(action);
						}
						window.showActions(actions);
					}
					if (action != null) {
						if (!window.isInAction() && action.isPerformableTo(Game.getHumanPlayer())) {
							window.setInAction(true);
							new Thread(()->{
								Render.setSelected(null);
								window.cancelSelection();
								action.apply(Game.getHumanPlayer());
								window.setInAction(false);
							}).start();
						}
					}
					
					
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent me){
				mousePressed = false;
				repaint();
			}
			
		};
		
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);		
	}
	
	private int getDiameter(){
		int diameter = Math.min(getWidth(), getHeight());
		return diameter;
	}
	
	@Override
	public Dimension getPreferredSize(){
		FontMetrics metrics = getGraphics().getFontMetrics(getFont());
		int minDiameter = 10 + Math.max(metrics.stringWidth(getText()), metrics.getHeight());
		return new Dimension(minDiameter, minDiameter);
	}
	
	@Override
	public boolean contains(int x, int y){
		int radius = getDiameter()/2;
		return Point2D.distance(x, y, getWidth()/2, getHeight()/2) < radius;
	}
	
	@Override
	public void paintComponent(Graphics g){

		Graphics2D g2d = (Graphics2D) g;
		
		int diameter = getDiameter();
		int radius = diameter/2;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (action != null) {
			if (action.isPerformableTo(Game.getHumanPlayer()))
				g2d.setColor(new Color(156,220,254, 200));
			else if (action.isVisibleTo(Game.getHumanPlayer()))
				g2d.setColor(new Color(0,0,0, 200));
			else
			// should not be shown
				g2d.setColor(new Color(255,0,0, 200));
		}
		if (this.getText() == "Tech Tree" || this.getText() == "End Turn") {
			if (Game.getHumanPlayer() == Game.getCurrentPlayer())
				g2d.setColor(new Color(156,220,254, 200));
			else
				g2d.setColor(new Color(0,0,0, 200));
		}
		if (this.getText() == "Exit Game")
			g2d.setColor(new Color(156,220,254, 200));
			
		g2d.fillOval(getWidth()/2 - radius, getHeight()/2 - radius, diameter, diameter);
		
		if(mousePressed){
			g2d.setColor(Color.BLUE);
		}
		else{
			g2d.setColor(Color.WHITE);
		}
		g2d.setStroke(new BasicStroke(2));
		g2d.drawOval(getWidth()/2 - radius+1, getHeight()/2 - radius+1, diameter-2, diameter-2);
		
		/*
		g2d.setColor(Color.WHITE);
		Font font = new Font("Avenir", Font.ITALIC, 9);
		g2d.setFont(font);
		FontMetrics metrics = g2d.getFontMetrics(font);
		int stringWidth = metrics.stringWidth(getText());
		int stringHeight = metrics.getHeight();
		g2d.drawString(getText(), getWidth()/2 - stringWidth/2, getHeight()/2 + stringHeight/4);*/

		String textureName = String.join("-", getText(), Game.getHumanPlayer().getFaction().toString());
		BufferedImage texture = Texture.getTextureByName(textureName);
		if (texture != null) {
			int width = texture.getWidth();
			int height = texture.getHeight();
			float diagonal =(float)(Math.sqrt(width*width + height*height));

			float scale = 0.8f * getHeight() / diagonal;
			AffineTransformOp op = new AffineTransformOp(new AffineTransform(scale, 0, 0, scale, 0, 0),null);
			g2d.drawImage(texture, op, 
						(int)(getWidth()/2-width*scale/2),
						(int)(getHeight()/2-height*scale/2));

		}

	}
}