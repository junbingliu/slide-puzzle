package com.infoscape.spc.web;

import com.alibaba.fastjson.JSON;
import com.infoscape.spc.entity.ImgUrl;
import com.infoscape.spc.entity.JsonResult;
import com.infoscape.spc.util.ImageUtil;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Random;

public class MainServlet extends HttpServlet{

    //写图片的位置
    private static final String IMG_DIR="D:\\workspace\\IdeaJavaProject\\slidePuzzleCode\\target\\slidePuzzleCode\\resource\\image\\drawImg";
    //读图片的位置
    private static final String RECOURSE_URL="/resource/image/drawImg/";
    //位移偏差(PX)
    private static final int OFFSET=10;
    //背景图宽度是抠图宽度的几倍
    private static final int PROPORTION=8;
    //背景图数量
    private static final int IMG_AMOUNT=3;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        res.setContentType("text/html;charset=UTF-8");
        //禁用缓存，确保网页信息是最新数据
        res.setHeader("Pragma","No-cache");
        res.setHeader("Cache-Control","no-cache");
        res.setDateHeader("Expires", -10);

        String path = req.getServletPath();
        System.out.println("=-=-=-=-=-=-");
        System.out.println(path);
        String httpUrl = req.getScheme()+"://"+ req.getServerName()+":"+req.getServerPort()+req.getContextPath();
        if ("/drawImg.do".equals(path)) {
            File dir=new File(IMG_DIR);
            if(!dir.exists()){
                dir.mkdir();
            }else {
                File[] files = dir.listFiles();
                for(File file : files){
                    if(file.isFile()){
                        file.delete();//删除之前生成的图片
                    }
                }
            }
            Random ran=new Random();
            int ranN=ran.nextInt(IMG_AMOUNT)+1;
            InputStream bc = this.getServletContext().getResourceAsStream("resource/image/"+ranN+".jpg");
            Image bcImg = ImageIO.read(bc);
            ImageUtil imageUtil = new ImageUtil();
            BufferedImage maskImg = imageUtil.getMask(bcImg,PROPORTION);
            int realX=imageUtil.getRectX();
            int bcImgWidth=imageUtil.getBcImgWidth();
            //正确位移存入session
            HttpSession session = req.getSession();
            session.setAttribute("realX", realX);
            session.setAttribute("bcImgWidth",bcImgWidth);

            BufferedImage mattingImg = imageUtil.getMatting(maskImg, bcImg, false);
            BufferedImage background = ImageUtil.getBackground(bcImg, maskImg);

            ImgUrl imgUrl=null;

            imgUrl=draw(req, res, mattingImg,background,httpUrl);

            if(imgUrl!=null){
                returnJson(req,res,new JsonResult(imgUrl));//返回图片URL给浏览器
            }

        }else if("/checkMove.do".equals(path)){
            int moveX=Integer.valueOf(req.getParameter("moveX"));
            int boxWidth=Integer.valueOf(req.getParameter("boxWidth"));
            //检查位移
            HttpSession session = req.getSession();
            int realX=0;
            int bcImgWidth=0;
            realX = (Integer) session.getAttribute("realX");
            bcImgWidth = (Integer)session.getAttribute("bcImgWidth");
            //按实际比例计算浏览器位移
            moveX=(bcImgWidth*moveX)/boxWidth;
            if(realX!=0){
                int min_x=realX-OFFSET;//偏差最小值
                int max_x=realX+OFFSET;//偏差最大值
                if(moveX>min_x && moveX<max_x){
                    returnJson(req,res,new JsonResult(JsonResult.SUCCESS,null,"位移正确"));
                }else {
                    returnJson(req,res,new JsonResult(JsonResult.ERROR,null,"位移错误"));
                }
            }else {
                returnJson(req,res,new JsonResult(JsonResult.ERROR,null,"位移错误"));
            }
        } else {
            throw new RuntimeException();
        }
    }

    protected ImgUrl draw(HttpServletRequest req, HttpServletResponse res, BufferedImage mattingImg, BufferedImage background, String httpUrl)
            throws ServletException, IOException {

        Random random=new Random();
        String randomNum1=String.valueOf((int) random.nextInt(900));
        String randomNum2=String.valueOf((int) random.nextInt(900));
        String mattUrl="MATT"+randomNum2+randomNum1;
        String bcUrl="BC"+randomNum1+randomNum2;

        //写图片位置
        String fileUrl=IMG_DIR+File.separator;
        File f1  = new File(fileUrl+mattUrl+".png");
        ImageIO.write(mattingImg,"png",f1);
        File f2  = new File(fileUrl+bcUrl+".jpg");
        ImageIO.write(background,"jpg",f2);

        mattUrl=httpUrl+RECOURSE_URL+mattUrl+".png";
        bcUrl=httpUrl+RECOURSE_URL+bcUrl+".jpg";

        ImgUrl imgUrl=new ImgUrl(mattUrl,bcUrl);

        return imgUrl;

    }

    protected void returnJson(HttpServletRequest request,HttpServletResponse response,Object object){
        response.setContentType("text/html;charset=UTF-8");
        //禁用缓存，确保网页信息是最新数据
        response.setHeader("Pragma","No-cache");
        response.setHeader("Cache-Control","no-cache");
        response.setDateHeader("Expires", -10);
        PrintWriter out = null;
        try {
            out = response.getWriter();
            String jsonStr=JSON.toJSONString(object);
            out.print(jsonStr);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            out.close();
        }
    }

}
