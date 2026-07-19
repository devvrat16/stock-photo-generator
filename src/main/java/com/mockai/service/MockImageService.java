package com.mockai.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

@Service
public class MockImageService {

    private final Random random = new Random();

    private static final Color[] PALETTE = {
        new Color(66, 133, 244),
        new Color(234, 67, 53),
        new Color(251, 188, 4),
        new Color(52, 168, 83),
        new Color(171, 71, 188),
        new Color(255, 112, 67),
        new Color(0, 172, 193),
        new Color(124, 179, 66),
    };

    public byte[] generateMockImage(String prompt, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        Color bgColor1 = PALETTE[random.nextInt(PALETTE.length)];
        Color bgColor2 = PALETTE[random.nextInt(PALETTE.length)];
        GradientPaint gradient = new GradientPaint(0, 0, bgColor1, width, height, bgColor2);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        for (int i = 0; i < 15; i++) {
            Color shapeColor = PALETTE[random.nextInt(PALETTE.length)];
            g2d.setColor(shapeColor);
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int size = 30 + random.nextInt(100);
            if (random.nextBoolean()) {
                g2d.fillOval(x - size / 2, y - size / 2, size, size);
            } else {
                g2d.fillRoundRect(x - size / 2, y - size / 2, size, size, 20, 20);
            }
        }

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        String label = "AI Generated";
        int labelWidth = fm.stringWidth(label);
        g2d.drawString(label, (width - labelWidth) / 2, height / 2 - 20);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
        fm = g2d.getFontMetrics();
        String wrappedPrompt = wrapText(g2d, prompt, width - 80);
        String[] lines = wrappedPrompt.split("\n");
        int startY = height / 2 + 10;
        for (String line : lines) {
            int lineWidth = fm.stringWidth(line);
            g2d.drawString(line, (width - lineWidth) / 2, startY);
            startY += fm.getHeight();
        }

        g2d.setFont(new Font("SansSerif", Font.BOLD, 48));
        g2d.setColor(new Color(255, 255, 255, 40));
        fm = g2d.getFontMetrics();
        String watermark = "MOCK";
        int wmWidth = fm.stringWidth(watermark);
        g2d.drawString(watermark, (width - wmWidth) / 2, height - 60);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    private String wrapText(Graphics2D g2d, String text, int maxWidth) {
        FontMetrics fm = g2d.getFontMetrics();
        StringBuilder wrapped = new StringBuilder();
        StringBuilder line = new StringBuilder();

        for (String word : text.split(" ")) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(testLine) > maxWidth && line.length() > 0) {
                wrapped.append(line).append("\n");
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }
        wrapped.append(line);
        return wrapped.toString();
    }

    public int[] parseSize(String size) {
        try {
            String[] parts = size.split("x");
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (Exception e) {
            return new int[]{1024, 1024};
        }
    }
}
