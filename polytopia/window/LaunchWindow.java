package polytopia.window;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import polytopia.window.MainPanel;
import polytopia.gameplay.Game;

public class LaunchWindow extends JFrame {

    JLabel NewGame, LoadGame, ExitGame;

    JLabel Title, SubTitle;

    static OptionWindow optionWindow;

    public LaunchWindow() {

        this.getGraphicsConfiguration().getDevice().setFullScreenWindow(this);

        
        //this.setSize((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(), 
        //           (int)(0.5625 * Toolkit.getDefaultToolkit().getScreenSize().getWidth()));

        int width = this.getWidth(), height = this.getHeight();
        System.out.println(Toolkit.getDefaultToolkit().getScreenSize());

        Title = new JLabel("POLYTOPIA", SwingConstants.CENTER);
        Title.setFont(new Font("Arial", Font.PLAIN, 52));
        Title.setForeground(Color.WHITE);
        Title.setBounds(3 * width / 8, 17 * height / 72, width / 4, height / 12);
        this.add(Title);

        SubTitle = new JLabel("—— THE BATTLE OF ——", SwingConstants.CENTER);
        SubTitle.setFont(new Font("Arial", Font.PLAIN, 24));
        SubTitle.setForeground(Color.WHITE);
        SubTitle.setBounds(3 * width / 8, 14 * height / 72, width / 4, height / 18);
        this.add(SubTitle);

        int dx = 7 * width / 16, dy = (int)(8 * height / 18);
        int dw = width / 8, dh = (int)(dw / 4);

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (e.getSource().equals(NewGame)) {
                    NewGame.setBackground(Color.WHITE);
                    NewGame.setForeground(Color.BLACK);
                } else if (e.getSource().equals(LoadGame)) {
                    LoadGame.setBackground(Color.WHITE);
                    LoadGame.setForeground(Color.BLACK);
                } else if (e.getSource().equals(ExitGame)) {
                    ExitGame.setBackground(Color.WHITE);
                    ExitGame.setForeground(Color.BLACK);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (e.getSource().equals(NewGame)) {
                    NewGame.setBackground(new Color(47, 148, 238));
                    NewGame.setForeground(Color.WHITE);
                } else if (e.getSource().equals(LoadGame)) {
                    LoadGame.setBackground(new Color(47, 148, 238));
                    LoadGame.setForeground(Color.WHITE);
                } else if (e.getSource().equals(ExitGame)) {
                    ExitGame.setBackground(new Color(47, 148, 238));
                    ExitGame.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getSource().equals(NewGame)) {
                    new Thread(()->{
                        new GameWindow();
                        Game.debugCommandLine();
                    }).start();
                    setVisible(false);
                } else if (e.getSource().equals(LoadGame)) {
                    // new LoadWindow();
                    setVisible(false);
                } else if (e.getSource().equals(ExitGame)) {
                    dispose();
                }
            }
        };

        NewGame = new ButtonLabel("NEW GAME", dx, dy, dw, dh);
        NewGame.addMouseListener(adapter);
        this.add(NewGame);

        LoadGame = new ButtonLabel("LOAD GAME", dx, dy + dh + dh / 8, dw, dh);
        LoadGame.setBackground(Color.LIGHT_GRAY);
        //LoadGame.addMouseListener(adapter);
        this.add(LoadGame);

        ExitGame = new ButtonLabel("EXIT GAME", dx, dy + 2 * (dh + dh / 8), dw, dh);
        ExitGame.addMouseListener(adapter);
        this.add(ExitGame);

        MainPanel panel = new MainPanel("./resources/window/main.png");
        this.add(panel);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        // makeshift
        /*
        new Thread(()->{
            new GameWindow();
            Game.debugCommandLine();
        }).start();
        this.setVisible(false);*/
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(()->{
            new LaunchWindow();
        });
    }

    class ButtonLabel extends JLabel {
        private Font labelFont;

        public void init() {
            labelFont = new Font("Meiryo UI", Font.PLAIN, 28);
        }

        public ButtonLabel(String text, int x, int y, int width, int height) {
            init();
            this.setText(text);
            this.setHorizontalAlignment(SwingConstants.CENTER);
            this.setFont(labelFont);
            this.setForeground(Color.WHITE);
            this.setBackground(new Color(47, 148, 238));
            this.setOpaque(true);
            this.setBounds(x, y, width, height);
        }
    }

}