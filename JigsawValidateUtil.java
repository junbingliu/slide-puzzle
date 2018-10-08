//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.xinshi.isone.commons;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import net.xinshi.isone.base.IsoneBaseEngine;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

public class JigsawValidateUtil {
    private static final int OFFSET = 10;
    private static final int PROPORTION = 8;

    public JigsawValidateUtil() {
    }

    public static JSONObject drawImages(String backgroundImageUrl) throws Exception {
        InputStream bc = null;
        URL url = new URL(backgroundImageUrl);
        HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
        if (httpConnection != null) {
            bc = httpConnection.getInputStream();
        }

        if (bc == null) {
            return null;
        } else {
            Image bcImg = ImageIO.read(bc);
            Map maskMap = getMask(bcImg);
            int realX = ((Integer)maskMap.get("rectX")).intValue();
            int maskWidth = ((Integer)maskMap.get("maskWidth")).intValue();
            int imageWidth = ((Integer)maskMap.get("imageWidth")).intValue();
            BufferedImage maskImg = (BufferedImage)maskMap.get("maskImg");
            BufferedImage mattingImg = getMatting(maskImg, bcImg, false, realX, maskWidth);
            BufferedImage background = getBackground(bcImg, maskImg);
            ByteArrayOutputStream mattingOs = new ByteArrayOutputStream();
            ImageIO.write(mattingImg, "png", mattingOs);
            ByteArrayOutputStream backgroundOs = new ByteArrayOutputStream();
            ImageIO.write(background, "jpg", backgroundOs);
            JSONObject fileResult1 = IsoneBaseEngine.fileService.uploadFile(mattingOs, ".png");
            JSONObject fileResult2 = IsoneBaseEngine.fileService.uploadFile(backgroundOs, ".jpg");
            String mattingFileId = fileResult1.optString("fileId");
            String bgFileId = fileResult2.optString("fileId");
            if (!StringUtils.isBlank(mattingFileId) && !StringUtils.isBlank(bgFileId)) {
                JSONObject jResult = new JSONObject();
                jResult.put("realX", realX);
                jResult.put("maskWidth", maskWidth);
                jResult.put("imageWidth", imageWidth);
                jResult.put("mattingFileId", mattingFileId);
                jResult.put("mattingFilePath", IsoneBaseEngine.fileService.getFullPath(mattingFileId));
                jResult.put("bgFileId", bgFileId);
                jResult.put("bgFilePath", IsoneBaseEngine.fileService.getFullPath(bgFileId));
                return jResult;
            } else {
                return null;
            }
        }
    }

    public static boolean checkMove(int realX, int imageWidth, int moveX, int boxWidth) throws Exception {
        moveX = imageWidth * moveX / boxWidth;
        if (realX != 0) {
            int min_x = realX - 10;
            int max_x = realX + 10;
            if (moveX > min_x && moveX < max_x) {
                return true;
            }
        }

        return false;
    }

    private static Shape getImageShape(Image img, boolean transparent) throws InterruptedException {
        ArrayList<Integer> x = new ArrayList();
        ArrayList<Integer> y = new ArrayList();
        int width = img.getWidth((ImageObserver)null);
        int height = img.getHeight((ImageObserver)null);
        PixelGrabber pgr = new PixelGrabber(img, 0, 0, -1, -1, true);
        pgr.grabPixels();
        int[] pixels = (int[])((int[])pgr.getPixels());

        int c;
        for(int i = 0; i < pixels.length; ++i) {
            c = pixels[i] >> 24 & 255;
            if (c != 0) {
                x.add(i % width > 0 ? i % width - 1 : 0);
                y.add(i % width == 0 ? (i == 0 ? 0 : i / width - 1) : i / width);
            }
        }

        int[][] matrix = new int[height][width];

        int temp;
        for(c = 0; c < height; ++c) {
            for(temp = 0; temp < width; ++temp) {
                matrix[c][temp] = 0;
            }
        }

        for(c = 0; c < x.size(); ++c) {
            matrix[((Integer)y.get(c)).intValue()][((Integer)x.get(c)).intValue()] = 1;
        }

        Area rec = new Area();
        temp = 0;
        int flag = transparent ? 0 : 1;

        for(int i = 0; i < height; ++i) {
            for(int j = 0; j < width; ++j) {
                Rectangle rectemp;
                if (matrix[i][j] == flag) {
                    if (temp == 0) {
                        temp = j;
                    } else if (j == width) {
                        if (temp == 0) {
                            rectemp = new Rectangle(j, i, 1, 1);
                            rec.add(new Area(rectemp));
                        } else {
                            rectemp = new Rectangle(temp, i, j - temp, 1);
                            rec.add(new Area(rectemp));
                            temp = 0;
                        }
                    }
                } else if (temp != 0) {
                    rectemp = new Rectangle(temp, i, j - temp, 1);
                    rec.add(new Area(rectemp));
                    temp = 0;
                }
            }

            temp = 0;
        }

        return rec;
    }

    private static BufferedImage getMatting(Image maskImg, Image bcImg, Boolean transparent, int rectX, int maskWidth) throws Exception {
        int bw = maskImg.getWidth((ImageObserver)null);
        int bh = maskImg.getHeight((ImageObserver)null);
        Shape shape = getImageShape(maskImg, transparent.booleanValue());
        BufferedImage img = new BufferedImage(bw, bh, 1);
        Graphics2D g2d = img.createGraphics();
        img = g2d.getDeviceConfiguration().createCompatibleImage(bw, bh, 3);
        g2d.dispose();
        g2d = img.createGraphics();
        g2d.clip(shape);
        g2d.drawImage(bcImg, 0, 0, bw, bh, (ImageObserver)null);
        g2d.dispose();
        BufferedImage cutImg = img.getSubimage(rectX, 0, maskWidth, bh);
        return cutImg;
    }

    private static Map getMask(Image bcImg) {
        int imageWidth = bcImg.getWidth((ImageObserver)null);
        int imageHeight = bcImg.getHeight((ImageObserver)null);
        BufferedImage img = new BufferedImage(imageWidth, imageHeight, 1);
        Graphics2D g2d = img.createGraphics();
        img = g2d.getDeviceConfiguration().createCompatibleImage(imageWidth, imageHeight, 3);
        g2d = img.createGraphics();
        g2d.setColor(new Color(255, 255, 255));
        g2d.setComposite(AlphaComposite.getInstance(11, 0.5F));
        int rectW = imageWidth / 8;
        int arcW = rectW / 3;
        int maskWidth = rectW + arcW * 2 / 3;
        int maskHeight = rectW + arcW * 2 / 3;
        int maxX = imageWidth - maskWidth;
        int maxY = imageHeight - maskHeight;
        Random rand = new Random();
        int rectX = rand.nextInt(maxX - maskWidth + 1) + maskWidth;
        int rectY = rand.nextInt(maxY - maskHeight + 1) + maskHeight;
        int arcX_1 = rectX + rectW / 3;
        int arcY_1 = rectY - arcW * 2 / 3;
        int arcX_2 = rectX + rectW - arcW / 3;
        int arcY_2 = rectY + arcW;
        int arcX_3 = rectX - arcW / 3;
        g2d.fillRect(rectX, rectY, rectW, rectW);
        g2d.fillArc(arcX_1, arcY_1, arcW, arcW, 0, 360);
        g2d.fillArc(arcX_2, arcY_2, arcW, arcW, 0, 360);
        g2d.setComposite(AlphaComposite.getInstance(11, 0.0F));
        g2d.fillArc(arcX_3, arcY_2, arcW, arcW, 0, 360);
        g2d.dispose();
        Map result = new HashMap();
        result.put("maskImg", img);
        result.put("rectX", rectX);
        result.put("maskWidth", maskWidth);
        result.put("imageWidth", imageWidth);
        return result;
    }

    private static BufferedImage getBackground(Image bcImg, Image maskImg) {
        int imageWidth = bcImg.getWidth((ImageObserver)null);
        int imageHeight = bcImg.getHeight((ImageObserver)null);
        BufferedImage img = new BufferedImage(imageWidth, imageHeight, 1);
        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(bcImg, 0, 0, imageWidth, imageHeight, (ImageObserver)null);
        g2d = img.createGraphics();
        g2d.drawImage(maskImg, 0, 0, imageWidth, imageHeight, (ImageObserver)null);
        g2d.dispose();
        return img;
    }
}
