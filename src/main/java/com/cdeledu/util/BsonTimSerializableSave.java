package com.cdeledu.util;

 /** 
 * Project Name:mongdb_sync
 * File Name:BsonTimSerializableSave.java 
 * Package Name:com.cdeledu.rad3.util 
 * Date:2017年5月4日下午1:53:18 
 * Copyright (c) 2017, idragonking@163.com All Rights Reserved. 
 * 
*/  

import java.io.*;

public class BsonTimSerializableSave {


  public static void saveBsonObj(Object bsonObj, String objName) {
    try {
      FileOutputStream fs = new FileOutputStream(objName);
      ObjectOutputStream os = new ObjectOutputStream(fs);
      os.writeObject(bsonObj);
      os.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static Object getBsonObj(String objName) {
    Object resultobj = null;
    try {
      FileInputStream fs = new FileInputStream(objName);
      ObjectInputStream is = new ObjectInputStream(fs);
      resultobj = is.readObject();
      is.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return resultobj;
  }

}
