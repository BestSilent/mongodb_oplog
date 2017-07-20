/** 
 * Project Name:mongdb_sync
 * File Name:PropertiesUtil.java 
 * Package Name:com.cdeledu.rad3.util 
 * Date:2017年5月4日下午2:50:32 
 * Copyright (c) 2017, idragonking@163.com All Rights Reserved. 
 * 
*/  
  
package com.cdeledu.util;

import java.io.IOException;
import java.util.Properties;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class PropertiesUtil {

  public static String PropertyGetValue(String filename, String key) {
    String propertyval = "";
    try {
      Properties prop = PropertiesLoaderUtils.loadAllProperties(filename);
      propertyval = prop.getProperty(key);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return propertyval;
  }

}
