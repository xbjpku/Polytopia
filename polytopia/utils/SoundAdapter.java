package polytopia.utils;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class SoundAdapter extends JSlider {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SoundAdapter(int x, int y, int width, int height) {
        // 设定布局器
        // 设定监听器
        ChangeListener listener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof JSlider) {
                    System.out.println("刻度: "
                            + ((JSlider) e.getSource()).getValue());
                }
            }
        };
        // 设定JSlider1
        // 注入自定义ui
        setUI(new MySliderUI(this));
        // 主刻度
        setMajorTickSpacing(10);
        // 次刻度
        setMinorTickSpacing(5);
        // 设定为显示
        setPaintTicks(true);
        setPaintLabels(true);
        // 监听slider1
        addChangeListener(listener);
        // 设定JSlider2
        // 使用盒式容器
        // 设定窗体大小
        setBounds(x, y, width, height);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("音量刻度设置");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setSize(480, 270);
        frame.setResizable(false);
        // 居中
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}