package com.mrbeard.baseproject.util;

import com.mrbeard.baseproject.exception.BaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @Author mrbeard
 * @Date 2018/12/6 17:32
 * 文件路径工具类
 **/
public class FilePathUtil {

    private static Logger logger = LoggerFactory.getLogger(FilePathUtil.class);

    /**
     * 获取jar包根目录路径
     *
     * @return
     */
    public String getWebRootContextPath() throws FileNotFoundException {
        //路径  E:\SpringBoot_WorkSpace\MrbeardZone
        String userPath = System.getProperty("user.dir");
        logger.info("获取到的用户根目录：System.getProperty(\"user.dir\")=======>" + userPath);
        //获取项目根目录

        //获取jar包运行的路径
        ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        String jarPatth = jarF.getParentFile().toString();
        logger.info("获取到的jar包根目录：System.getProperty(\"user.dir\")=======>" + jarPatth);
        return jarPatth;
    }

    /**
     * 下载文件工具类
     * @param fileName 文件名
     * @param filePath 文件路径
     * @throws BaseRuntimeException
     */
    public static void downloadFile(String filePath, String fileName) throws BaseRuntimeException {
        //下载开始
        try {
            File downFiles = new File(filePath);
            FileInputStream fis = null;
            byte[] data = new byte[(int) downFiles.length()];
            fis.close();
            HttpServletResponse response = WebUtil.getResponse();
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"; filename*=utf-8''" + fileName);
            response.setContentType("application/octet-stream");
            OutputStream fos = response.getOutputStream();
            fos.write(data);
            fis.close();
            fos.close();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            throw new BaseRuntimeException(e.getMessage());
        }
    }
}
