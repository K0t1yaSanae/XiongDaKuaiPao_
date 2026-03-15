import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageLoader {
    public static void customImage(Frame parent, String type, java.util.function.Consumer<Image> callback) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".png") || f.getName().toLowerCase().endsWith(".jpg");
            }

            @Override
            public String getDescription() {
                return "图片文件 (*.png, *.jpg)";
            }
        });

        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            Image image = Toolkit.getDefaultToolkit().getImage(file.getAbsolutePath());
            callback.accept(image);
            JOptionPane.showMessageDialog(parent, type + "图标自定义成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static Image createDefaultImage(Color color, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(color);
        g2.fillOval(0, 0, size, size);
        g2.dispose();
        return img;
    }

    public static Image createDefaultImage(Color color, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(color);
        g2.fillRect(0, 0, width, height);
        g2.dispose();
        return img;
    }
}