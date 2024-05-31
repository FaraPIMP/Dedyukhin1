import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ImageViewerApp extends JFrame {

    private JLabel imageLabel;
    private BufferedImage image;
    private double scale = 1.0;

    public ImageViewerApp() {
        setTitle("Image Viewer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        imageLabel = new JLabel();
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton zoomInButton = new JButton("Zoom In");
        zoomInButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scale *= 1.1;
                scaleImage();
            }
        });
        controlPanel.add(zoomInButton);

        JButton zoomOutButton = new JButton("Zoom Out");
        zoomOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scale /= 1.1;
                scaleImage();
            }
        });
        controlPanel.add(zoomOutButton);

        JButton rotateButton = new JButton("Rotate");
        rotateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rotateImage();
            }
        });
        controlPanel.add(rotateButton);

        getContentPane().add(controlPanel, BorderLayout.SOUTH);

        JFileChooser fileChooser = new JFileChooser();
        JButton openButton = new JButton("Open Image");
        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        image = ImageIO.read(selectedFile);
                        scale = 1.0;
                        scaleImage();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        controlPanel.add(openButton);
    }

    private void scaleImage() {
        if (image != null) {
            int newWidth = (int) (image.getWidth() * scale);
            int newHeight = (int) (image.getHeight() * scale);
            Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        }
    }

    private void rotateImage() {
        if (image != null) {
            BufferedImage rotatedImage = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
            Graphics2D g2d = rotatedImage.createGraphics();
            g2d.rotate(Math.toRadians(90), rotatedImage.getWidth() / 2, rotatedImage.getHeight() / 2);
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            image = rotatedImage;
            scaleImage();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ImageViewerApp().setVisible(true);
            }
        });
    }
}
