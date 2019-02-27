package com.code.util;

import java.util.List;

import org.apache.commons.mail.*;

/**
 * @author hengjian
 * @date 2019/2/27
 */
public class MailUtil {
    private static String mailServerHost = "smtp.163.com";
    private static String mailSenderAddress = "test@163.com";
    private static String mailSenderNick = "test";
    private static String mailSenderUsername = "test@163.com";
    private static String mailSenderPassword = "xxx";

    /**
     * 发送 邮件方法 (Html格式，支持附件)
     *
     * @return void
     */
    public static void sendEmail(MailInfo mailInfo) {

        try {
            HtmlEmail email = new HtmlEmail();
            // 配置信息
            email.setHostName(mailServerHost);
            email.setFrom(mailSenderAddress, mailSenderNick);
            email.setAuthentication(mailSenderUsername, mailSenderPassword);
            email.setCharset("UTF-8");
            email.setSubject(mailInfo.getSubject());
            email.setHtmlMsg(mailInfo.getContent());

            // 添加附件
            List<EmailAttachment> attachments = mailInfo.getAttachments();
            if (null != attachments && attachments.size() > 0) {
                for (int i = 0; i < attachments.size(); i++) {
                    email.attach(attachments.get(i));
                }
            }

            // 收件人
            List<String> toAddress = mailInfo.getToAddress();
            if (null != toAddress && toAddress.size() > 0) {
                for (int i = 0; i < toAddress.size(); i++) {
                    email.addTo(toAddress.get(i));
                }
            }
            // 抄送人
            List<String> ccAddress = mailInfo.getCcAddress();
            if (null != ccAddress && ccAddress.size() > 0) {
                for (int i = 0; i < ccAddress.size(); i++) {
                    email.addCc(ccAddress.get(i));
                }
            }
            //邮件模板 密送人
            List<String> bccAddress = mailInfo.getBccAddress();
            if (null != bccAddress && bccAddress.size() > 0) {
                for (int i = 0; i < bccAddress.size(); i++) {
                    email.addBcc(ccAddress.get(i));
                }
            }
            email.send();
            System.out.println("邮件发送成功！");
        } catch (EmailException e) {
            e.printStackTrace();
        }
    }
}