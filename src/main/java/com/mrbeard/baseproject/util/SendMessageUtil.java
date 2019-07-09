package com.mrbeard.baseproject.util;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @ClassName SendMessageUtil
 * @Description 用于发送短信工具类
 * @Author Mrbeard
 * @Date 2019/4/8 16:33
 * @Version 1.0
 **/
@Component
public class SendMessageUtil {
    /**
     * 短信应用SDK AppID  1400开头
     */
    @Value("${customproperty.appid}")
    private int appid;

    /**
     * 短信应用SDK AppKey
     */
    @Value("${customproperty.appkey}")
    private  String appkey;

    /**
     * 短信模板ID，需要在短信应用中申请
     */
    @Value("${customproperty.templateId}")
    private  int templateId;
    /**
     * 签名
     */
    @Value("${customproperty.smsSign}")
    private  String smsSign;

    /**
     * 国家码，如 86 为中国
     */
    @Value("${customproperty.nationCode}")
    private  String nationCode;

    /**
     * 发送短信
     *
     * @param phoneNumber 手机号码
     * @param params      固定大小为2的字符串数组 例如：{"5678", "4"},第一个字符串为验证码，第二个字符串为有效时间（可根据自己需要设置）
     * @return 返回样例： {"result":0,"errmsg":"OK","ext":"","sid":"18:972aeec1e71f4b05b69ab724ed4b8184","fee":1}
     */
    public  String sendMessage(String phoneNumber, String[] params) {
        //返回结果
        String resultString = "";
        try {
            SmsSingleSender ssender = new SmsSingleSender(appid, appkey);
            SmsSingleSenderResult result = ssender.sendWithParam(nationCode, phoneNumber, templateId, params, smsSign, "", "");
            resultString = result.toString();
            return resultString;
        } catch (Exception e) {
            e.printStackTrace();
            return "发送短信失败！";
        }
    }
}
