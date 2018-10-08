package com.xinshi;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class DrawBcImg {
    public static void main(String[] args) throws Exception{
//        int imageWidth = 400;
//        int imageHeight = 400;
//        String head=new String("D:/JavaDrawImg/avata.jpg");
//        File headFile = new File(head);
//        Image headImg = ImageIO.read(headFile);
//        BufferedImage img = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g2d = img.createGraphics();
//        //设置画布为透明
//        g2d.drawImage(headImg, 0, 0, 400, 400, null);
//        g2d.dispose();
//        g2d = img.createGraphics();
//        g2d.setColor(new Color(61, 107, 255));
//        g2d.fillRect(200, 200, 80, 80);
//        g2d.fillArc(230,180,30,30,0,360);
//        g2d.fillArc(270,230,30,30,0,360);
//        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_ATOP, 0.0f));
//        g2d.fillArc(190,230,30,30,0,360);
//        ImageIO.write(img, "PNG", new File("D:/JavaDrawImg/bcd.png"));
//        g2d.dispose();

        int imageWidth = 400;
        int imageHeight = 400;
        String head=new String("D:/JavaDrawImg/avata.jpg");
        File headFile = new File(head);
        Image headImg = ImageIO.read(headFile);
        String mask=new String("D:/JavaDrawImg/mask.png");
        File maskFile = new File(mask);
        Image maskImg = ImageIO.read(maskFile);
        BufferedImage img = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(headImg, 0, 0, 400, 400, null);
        g2d.dispose();
        g2d = img.createGraphics();
        g2d.drawImage(maskImg, 0, 0, 400, 400, null);
        ImageIO.write(img, "PNG", new File("D:/JavaDrawImg/bcd.png"));
        g2d.dispose();
    }
}
