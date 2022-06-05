package polytopia.utils;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

class RButton extends JButton {
    public RButton(String label) {
        super(label);
        // 这些声明把按钮扩展为一个圆而不是一个椭圆。
        Dimension size = getPreferredSize();
        size.width = size.height = 100;
        setPreferredSize(size);
        setBorderPainted(false);
        setContentAreaFilled(false);
        this.setBackground(Color.WHITE);
    }

    // 画圆的背景和标签
    protected void paintComponent(Graphics g) {
        if (getModel().isArmed()) {
            g.setColor(Color.WHITE);
        } else {
            g.setColor(getBackground());
        }
        g.fillOval(0, 0, getSize().width - 1,
                getSize().height - 1);
        super.paintComponent(g);
    }

    protected void paintBorder(Graphics g) {
        g.setColor(getForeground());
        g.drawOval(0, 0, getSize().width - 1,
                getSize().height - 1);
    }

    Shape shape;

    public boolean contains(int x, int y) {
        if (shape == null ||
                !shape.getBounds().equals(getBounds())) {
            shape = new Ellipse2D.Float(0, 0,
                    getWidth(), getHeight());
        }
        return shape.contains(x, y);
    }

    // 测试程序
    public static void main(String[] args) {
        JButton button = new RButton(null);
        ImageIcon ic = new ImageIcon("E://clientForMssql//Icons//item_group.gif");
        JButton button2 = new JButton(ic);
        button.setBackground(Color.GRAY);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(button);
        frame.getContentPane().add(button2);
        frame.getContentPane().setLayout(new FlowLayout());
        frame.setSize(200, 200);
        frame.setVisible(true);
    }
}