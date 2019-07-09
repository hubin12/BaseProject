package com.mrbeard.baseproject;

import com.mrbeard.baseproject.util.SendMessageUtil;
import org.apache.commons.lang.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    @Autowired
    StringEncryptor stringEncryptor;

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Test
    public void contextLoads() {
    }

    @Test
    public void testStringUtils(){
        String labvalue = "#ge[]dfdf/fdf,fdf90990";
        labvalue = StringUtils.replace(labvalue,"#,",",");

        logger.info(labvalue);
    }

    @Test
    public void testSendMessage(){
        String [] params = {"2311","3"};
        SendMessageUtil sendMessageUtil = new SendMessageUtil();
        String result = sendMessageUtil.sendMessage("15979807792", params);
        logger.info(result);
    }

    @Test
    public void testIntegerWithInt(){
        Integer a = new Integer("12");
        int b = new Integer("13");
        System.out.println(a>b);
    }

    @Test
    public void decodeMessage(){
        //加密
        String encrypt = stringEncryptor.encrypt("");
        logger.info("appkey:"+encrypt);
    }

    @Test
    public void encodeMessage(){
        //解密
        String decrypt = stringEncryptor.decrypt("");
        logger.info(decrypt);
    }
}

