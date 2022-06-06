package polytopia.window;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.awt.Toolkit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import polytopia.window.MainPanel;
import polytopia.utils.SoundAdapter;

public class OptionWindow extends JFrame implements MouseListener {

    static boolean hasSoundEffect = true, hasAmbience = true, hasTribeMusic = true;

    static int volume = 50;

    JLabel MainMenu, SoundEffect, Ambience, TribeMusic;

    JLabel SoundEffectText, AmbienceText, TribeMusicText;

    SoundAdapter volumeAdapter;

    JLabel volumeText;

    public JFrame parent = null;

    public OptionWindow(JFrame parent) {

        this.parent = parent;

        this.getGraphicsConfiguration().getDevice().setFullScreenWindow(this);
        this.setLayout(null);
        //this.setSize(Toolkit.getDefaultToolkit().getScreenSize());

        int width = this.getWidth(), height = this.getHeight();

        MainMenu = new ButtonLabel(null, width / 256, width / 256, width / 24, width / 24);
        Image MainMenuImage;
        try {
            MainMenuImage = ImageIO.read(new File("./resources/window/exitbutton.png"));
            MainMenu.setIcon(new ImageIcon(MainMenuImage));
        } catch (IOException e) {}
        MainMenu.setOpaque(false);
        MainMenu.addMouseListener(this);
        this.add(MainMenu);

        int dx = 3 * width / 8, dy = 7 * height / 24;
        int dw = 5 * width / 96, dh = dw / 2;

        SoundEffect = new ButtonLabel(hasSoundEffect ? "ON" : "OFF", dx, dy, dw, dh);
        SoundEffect.addMouseListener(this);
        this.add(SoundEffect);

        Ambience = new ButtonLabel(hasAmbience ? "ON" : "OFF", dx + dw + 9 * dw / 10, dy, dw, dh);
        Ambience.addMouseListener(this);
        this.add(Ambience);

        TribeMusic = new ButtonLabel(hasTribeMusic ? "ON" : "OFF", dx + 2 * (dw + 9 * dw / 10), dy, dw, dh);
        TribeMusic.addMouseListener(this);
        this.add(TribeMusic);

        dy = dy - height / 36;
        dw = 7 * width / 96;
        dh = dw / 7;

        SoundEffectText = new TextLabel("Sound Effects", SoundEffect.getX() - width / 96, dy, dw, dh);
        this.add(SoundEffectText);

        AmbienceText = new TextLabel("Ambience", Ambience.getX() - width / 96, dy, dw, dh);
        this.add(AmbienceText);

        TribeMusicText = new TextLabel("Tribe Music", TribeMusic.getX() - width / 96, dy, dw, dh);
        this.add(TribeMusicText);

        dx = 3 * width / 8;
        dy = 7 * height / 48;
        dw = width / 4;
        dh = dw / 6;

        volumeAdapter = new SoundAdapter(dx, dy, dw, dh);
        this.add(volumeAdapter);

        volumeText = new TextLabel("Audio volume", dx, dy - height / 16, dw, dh);
        this.add(volumeText);

        MainPanel panel = new MainPanel("./resources/window/darkmain.png");
        panel.setSize(this.getSize());

        this.add(panel);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(false);
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

    class TextLabel extends JLabel {
        private Font labelFont;

        public void init() {
            labelFont = new Font("Meiryo UI", Font.ITALIC, 16);
        }

        public TextLabel(String text, int x, int y, int width, int height) {
            init();
            this.setText(text);
            this.setHorizontalAlignment(SwingConstants.CENTER);
            this.setFont(labelFont);
            this.setForeground(Color.WHITE);
            this.setBounds(x, y, width, height);
        }
    }

    private String getText(boolean b) {
        return b ? "ON" : "OFF";
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource().equals(SoundEffect)) {
            hasSoundEffect = !hasSoundEffect;
            SoundEffect.setText(getText(hasSoundEffect));
        } else if (e.getSource().equals(Ambience)) {
            hasAmbience = !hasAmbience;
            Ambience.setText(getText(hasAmbience));
        } else if (e.getSource().equals(TribeMusic)) {
            hasTribeMusic = !hasTribeMusic;
            TribeMusic.setText(getText(hasTribeMusic));
        } else if (e.getSource().equals(MainMenu)) {
            parent.setVisible(true);
            this.setVisible(false);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (e.getSource().equals(SoundEffect)) {
            SoundEffect.setBackground(Color.LIGHT_GRAY);
            SoundEffect.setForeground(Color.BLACK);
        } else if (e.getSource().equals(Ambience)) {
            Ambience.setBackground(Color.LIGHT_GRAY);
            Ambience.setForeground(Color.BLACK);
        } else if (e.getSource().equals(TribeMusic)) {
            TribeMusic.setBackground(Color.LIGHT_GRAY);
            TribeMusic.setForeground(Color.BLACK);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (e.getSource().equals(SoundEffect)) {
            SoundEffect.setBackground(new Color(47, 148, 238));
            SoundEffect.setForeground(Color.WHITE);
        } else if (e.getSource().equals(Ambience)) {
            Ambience.setBackground(new Color(47, 148, 238));
            Ambience.setForeground(Color.WHITE);
        } else if (e.getSource().equals(TribeMusic)) {
            TribeMusic.setBackground(new Color(47, 148, 238));
            TribeMusic.setForeground(Color.WHITE);
        }
    }
}