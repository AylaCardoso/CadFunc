package com.empresa.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SemImagem {
    public static byte[] gerar() {
        BufferedImage bi = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();

        // Fundo azul claro
        g.setColor(new Color(230, 230, 255));
        g.fillRect(0, 0, 200, 200);

        // Texto "Sem Imagem"
        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.BOLD, 18));

        FontMetrics fm = g.getFontMetrics();
        String texto1 = "Sem";
        String texto2 = "Imagem";

        int xTexto1 = (200 - fm.stringWidth(texto1)) / 2;
        int xTexto2 = (200 - fm.stringWidth(texto2)) / 2;

        g.drawString(texto1, xTexto1, 80);
        g.drawString(texto2, xTexto2, 120);

        g.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bi, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar imagem padrão", e);
        }
    }
}
