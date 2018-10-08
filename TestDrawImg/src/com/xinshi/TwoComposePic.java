package com.xinshi;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * 利用透明的背景轮廓抠图
 * 参考了：http://blog.csdn.net/daixinmei/article/details/51085575后实现
 *
 * @author yzl
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class TwoComposePic {

    /**
     *
     * 将Image图像中的透明/不透明部分转换为Shape图形
     *
     * @param img 图片信息
     * @param transparent 是否透明
     * @return
     * @throws InterruptedException
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public static Shape getImageShape(Image img, boolean transparent) throws InterruptedException {
        ArrayList<Integer> x = new ArrayList<Integer>();
        ArrayList<Integer> y = new ArrayList<Integer>();
        int width = img.getWidth(null);
        int height = img.getHeight(null);

        // 首先获取图像所有的像素信息
        PixelGrabber pgr = new PixelGrabber(img, 0, 0, -1, -1, true);
        pgr.grabPixels();
        int pixels[] = (int[]) pgr.getPixels();

        // 循环像素
        for (int i = 0; i < pixels.length; i++) {
            // 筛选，将不透明的像素的坐标加入到坐标ArrayList x和y中
            int alpha = (pixels[i] >> 24) & 0xff;
            if (alpha == 0) {
                continue;
            } else {
                x.add(i % width > 0 ? i % width - 1 : 0);
                y.add(i % width == 0 ? (i == 0 ? 0 : i / width - 1) : i / width);
            }
        }

        // 建立图像矩阵并初始化(0为透明,1为不透明)
        int[][] matrix = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                matrix[i][j] = 0;
            }
        }

        // 导入坐标ArrayList中的不透明坐标信息
        for (int c = 0; c < x.size(); c++) {
            matrix[y.get(c)][x.get(c)] = 1;
        }

        /*
         * 逐一水平"扫描"图像矩阵的每一行，将透明（这里也可以取不透明的）的像素生成为Rectangle，
         * 再将每一行的Rectangle通过Area类的rec对象进行合并， 最后形成一个完整的Shape图形
         */
        Area rec = new Area();
        int temp = 0;
        //生成Shape时是1取透明区域还是取非透明区域的flag
        int flag = transparent ? 0 : 1;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (matrix[i][j] == flag) {
                    if (temp == 0)
                        temp = j;
                    else if (j == width) {
                        if (temp == 0) {
                            Rectangle rectemp = new Rectangle(j, i, 1, 1);
                            rec.add(new Area(rectemp));
                        } else {
                            Rectangle rectemp = new Rectangle(temp, i, j - temp, 1);
                            rec.add(new Area(rectemp));
                            temp = 0;
                        }
                    }
                } else {
                    if (temp != 0) {
                        Rectangle rectemp = new Rectangle(temp, i, j - temp, 1);
                        rec.add(new Area(rectemp));
                        temp = 0;
                    }
                }
            }
            temp = 0;
        }
        return rec;
    }

    /**
     *
     * 功能描述: <br>
     * 〈功能详细描述〉
     *
     * @param back
     * @param head
     * @param out
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public void composePic(String back, String head, String out,Boolean transparent) {

        try {
            File backFile = new File(back);
            Image backImg = ImageIO.read(backFile);
            int bw = backImg.getWidth(null);
            int bh = backImg.getHeight(null);

            File headFile = new File(head);
            Image headImg = ImageIO.read(headFile);
            int hw = headImg.getWidth(null);
            int hh = headImg.getHeight(null);

            //抠图区域
            Shape shape = getImageShape(ImageIO.read(new File(back)), transparent);

            BufferedImage img = new BufferedImage(bw, bh, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();
            //设置画布为透明
            img = g2d.getDeviceConfiguration().createCompatibleImage(bw, bh, Transparency.TRANSLUCENT);
            g2d.dispose();
            g2d = img.createGraphics();

            g2d.clip(shape);

            g2d.drawImage(headImg, 0, 0, bw, bh, null);

            g2d.dispose();

            ImageIO.write(img, "png", new File(out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @Description:截图
     * @param srcFile 源图片、targetFile 截好后图片全名、startAcross 开始截取位置横坐标、StartEndlong开始截图位置纵坐标、width截取的长，hight截取的高
     */
    public static void cutImage(String srcFile, String targetFile, int startAcross, int StartEndlong, int width,
                                int hight) throws Exception {
        // 取得图片读入器
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("png");
        ImageReader reader = readers.next();
        // 取得图片读入流
        InputStream source = new FileInputStream(srcFile);
        ImageInputStream iis = ImageIO.createImageInputStream(source);
        reader.setInput(iis, true);
        // 图片参数对象
        ImageReadParam param = reader.getDefaultReadParam();
        Rectangle rect = new Rectangle(startAcross, StartEndlong, width, hight);
        param.setSourceRegion(rect);
        BufferedImage bi = reader.read(0, param);
        ImageIO.write(bi, targetFile.split("\\.")[1], new File(targetFile));
    }

    public static void main(String args[]) throws Exception {
        String basePath = "D:/JavaDrawImg/";
        TwoComposePic pic = new TwoComposePic();
        pic.composePic(basePath+"mask.png", basePath+"avata.jpg", basePath+"result.png",false);
        TwoComposePic.cutImage(basePath+"result.png",basePath+"cut.png",200,0,100,400);
    }
}
