package polytopia.window;

import java.awt.Graphics;
import java.awt.Image;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class MainPanel extends JPanel {
    Image background;

    public MainPanel(String path) {
        try {
            background = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g) {
        ImageIcon icon = new ImageIcon(background);
        icon.setImage(background.getScaledInstance(this.getWidth(), this.getHeight(),
                Image.SCALE_AREA_AVERAGING));
        g.drawImage(icon.getImage(), 0, 0, null);

    }
}