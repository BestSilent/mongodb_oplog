/** 
 * Project Name:mongdb_sync
 * File Name:Logger.java 
 * Package Name:com.cdeledu.rad3.util 
 * Date:2016年12月2日下午1:53:18 
 * Copyright (c) 2016, idragonking@163.com All Rights Reserved. 
 * 
*/  
  
package com.cdeledu.util;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/** 
 * ClassName:Logger <br/> 
 * Function: TODO ADD FUNCTION. <br/> 
 * Reason:   TODO ADD REASON. <br/> 
 * Date:     2016年12月2日 下午1:53:18 <br/> 
 * @author   wql 
 * @version   
 * @since    JDK 1.6 
 * @see       
 */
public class LoggerUtil
{
    public  Logger  getLoggerByName(String name,String filePath) {
        // 生成新的Logger
        // 如果已經有了一個Logger實例返回現有的
        Logger  logger= Logger.getLogger(name);
        // 清空Appender。特別是不想使用現存實例時一定要初期化
        logger.removeAllAppenders();
        // 設定Logger級別。
        logger.setLevel(Level.INFO);
        // 設定是否繼承父Logger。
        // 默認為true。繼承root輸出。
        // 設定false後將不輸出root。
        logger.setAdditivity(false);
        // 生成新的Appender
        FileAppender appender = new FileAppender();
        PatternLayout layout = new PatternLayout();
        // log的输出形式
        appender.setLayout(layout);
        // log输出路径
        // 这里使用了环境变量[catalina.home]，只有在tomcat环境下才可以取到
        appender.setFile(filePath);
        // log的文字码
        appender.setEncoding("UTF-8");
        // true:在已存在log文件后面追加 false:新log覆盖以前的log
        appender.setAppend(false);
        // 适用当前配置
        appender.activateOptions();
        // 将新的Appender加到Logger中
        logger.addAppender(appender);
        return logger;
    }
}
