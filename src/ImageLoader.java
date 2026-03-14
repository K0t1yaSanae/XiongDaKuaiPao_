import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageLoader {

    public static Image createDefaultImage(Color color, int size) {
        return createDefaultImage(color, size, size);
    }

    public static Image createDefaultImage(Color color, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(color);
        if (width == height) {
            g2.fillOval(0, 0, width, height); // 圆形（玩家、追逐者）
        } else {
            g2.fillRect(0, 0, width, height); // 矩形（背景、障碍物）
        }
        g2.dispose();
        return image;
    }

    public static void customImage(JFrame parent, String type, java.util.function.Consumer<Image> imageConsumer) {
        JFileChooser fileChooser = new JFileChooser();
        // 限制文件类型为PNG/JPG
        FileFilter filter = new FileNameExtensionFilter("Image Files (PNG/JPG)", "png", "jpg", "jpeg");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            Image image = new ImageIcon(file.getAbsolutePath()).getImage();
            imageConsumer.accept(image); // 回调赋值
            JOptionPane.showMessageDialog(parent, "Custom " + type + " image set successfully!");
        }
    }
}