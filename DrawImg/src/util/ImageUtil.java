package util;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;
import java.util.Random;


public class ImageUtil {

    private int rectX;
    private int maskWidth;
    private int bcImgWidth;

    public ImageUtil(){
        this.rectX=0;
        this.maskWidth=0;
        this.bcImgWidth=0;
    }

    public void setRectX(int rectX) {
        this.rectX = rectX;
    }

    public void setMaskWidth(int maskWidth) {
        this.maskWidth = maskWidth;
    }

    public int getMaskWidth() {
        return this.maskWidth;
    }

    public int getRectX() {
        return this.rectX;
    }

    public void setBcImgWidth(int bcImgWidth) {
        this.bcImgWidth = bcImgWidth;
    }

    public int getBcImgWidth() {
        return bcImgWidth;
    }

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

    public BufferedImage getMatting(Image maskImg, Image bcImg,Boolean transparent) {

        BufferedImage img = null;
        try {
            int bw = maskImg.getWidth(null);
            int bh = maskImg.getHeight(null);

            //抠图区域
            Shape shape = getImageShape(maskImg, transparent);

            img = new BufferedImage(bw, bh, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();
            //设置画布为透明
            img = g2d.getDeviceConfiguration().createCompatibleImage(bw, bh, Transparency.TRANSLUCENT);
            g2d.dispose();
            g2d = img.createGraphics();
            g2d.clip(shape);
            g2d.drawImage(bcImg, 0, 0, bw, bh, null);
            g2d.dispose();
            BufferedImage cutImg=img.getSubimage(this.rectX,0,this.maskWidth,bh);
            return cutImg;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public BufferedImage getMask(Image bcImg ,int scale){
        int imageWidth = bcImg.getWidth(null);
        this.bcImgWidth=imageWidth;
        int imageHeight = bcImg.getHeight(null);
        BufferedImage img = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        //设置画布为透明
        img = g2d.getDeviceConfiguration().createCompatibleImage(imageWidth, imageHeight, Transparency.TRANSLUCENT);
        g2d = img.createGraphics();
        g2d.setColor(new Color(255, 255, 255));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_ATOP, 0.5f));

        int rectW=imageWidth/scale;
        int rectH=rectW;

        int arcW=rectW/3;
        int arcH=arcW;

        int maskWidth=rectW+arcW*2/3;
        this.maskWidth=maskWidth;

        int maskHeight=rectH+arcH*2/3;

        int maxX=imageWidth-maskWidth;
        int minX=maskWidth;
        int maxY=imageHeight-maskHeight;
        int minY=maskHeight;
        Random rand=new Random();
        int rectX =rand.nextInt(maxX - minX + 1) + minX;
        this.rectX=rectX;
        int rectY =rand.nextInt(maxY - minY + 1) + minY;


        int arcX_1=rectX+rectW/3;
        int arcY_1=rectY-arcH*2/3;

        int arcX_2=rectX+rectW-arcW/3;
        int arcY_2=rectY+arcH;

        int arcX_3=rectX-arcW/3;
        int arcY_3=arcY_2;

        g2d.fillRect(rectX, rectY, rectW, rectH);
        g2d.fillArc(arcX_1,arcY_1,arcW,arcH,0,360);
        g2d.fillArc(arcX_2,arcY_2,arcW,arcH,0,360);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_ATOP, 0.0f));
        g2d.fillArc(arcX_3,arcY_3,arcW,arcH,0,360);
        g2d.dispose();
        return img;
    }

    public static BufferedImage getBackground(Image bcImg,Image maskImg){
        int imageWidth = bcImg.getWidth(null);
        int imageHeight = bcImg.getHeight(null);
        BufferedImage img = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(bcImg, 0, 0, imageWidth, imageHeight, null);
        g2d = img.createGraphics();
        g2d.drawImage(maskImg, 0, 0, imageWidth, imageHeight, null);
        g2d.dispose();
        return img;
    }

}
