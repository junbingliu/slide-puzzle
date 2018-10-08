package com.xinshi;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class DrawMask {
    public static void main(String[] args) throws Exception{
        int imageWidth = 400;
        int imageHeight = 400;
        BufferedImage img = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        //设置画布为透明
        img = g2d.getDeviceConfiguration().createCompatibleImage(imageWidth, imageHeight, Transparency.TRANSLUCENT);
        g2d.dispose();
        g2d = img.createGraphics();
        g2d.setColor(new Color(252, 252, 252));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_ATOP, 0.5f));
        g2d.fillRect(200, 200, 80, 80);
        g2d.fillArc(230,180,30,30,0,360);
        g2d.fillArc(270,230,30,30,0,360);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_ATOP, 0.0f));
        g2d.fillArc(190,230,30,30,0,360);
        ImageIO.write(img, "PNG", new File("D:/JavaDrawImg/mask.png"));
        g2d.dispose();
        System.out.print("ok");
    }
}
