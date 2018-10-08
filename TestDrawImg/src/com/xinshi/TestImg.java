package com.xinshi;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class TestImg {
    public static void main(String[] args) throws Exception{
//        int imageWidth = 200;
//        int imageHeight = 200;
//        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
//        Graphics graphics = image.getGraphics();
//        int fontSize = 100;
//        Font font = new Font("楷体", Font.PLAIN, fontSize);
//        graphics.setFont(font);
//        graphics.setColor(new Color(246, 96, 0));
//        graphics.fillRect(0, 0, imageWidth, imageHeight);
//        graphics.setColor(new Color(255, 255, 255));
//        int strWidth = graphics.getFontMetrics().stringWidth("好");
//        graphics.drawString("好", fontSize - (strWidth / 2), fontSize + 30);
//        ImageIO.write(image, "PNG", new File("D:/JavaDrawImg/abc.png"));
//        graphics.dispose();
        int num =100+(int)(Math.random()*200);
        System.out.println(num);
    }


}
