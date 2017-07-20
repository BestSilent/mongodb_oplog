/*
 * @Title: MongoClientHelp.java
 * @Package com.cdeledu.mongo
 * @Description: TODO
 * @author wang Qinglong wangqinglong@cdeledu.com
 * @date 2017年05月05日 下午 09:15:00
 * @version V1.0
 *
 * Modification History:
 * Date         Author      Version     Description
 * --------------------------------------------------------------
 * 2017年05月05日
 */

package com.cdeledu.dbsync;

import com.cdeledu.util.BsonTimSerializableSave;
import com.cdeledu.util.Logger;
import com.cdeledu.util.MailInformUtil;
import com.cdeledu.util.PropertiesUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.QueryOperators;
import com.mongodb.ServerAddress;
import com.sun.jmx.snmp.Timestamp;
import java.net.UnknownHostException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;
import org.bson.types.BSONTimestamp;

/**
 * Created by dell on 2017/5/2.
 */
public class MongodbSync {
  private static final String OPLOG = "oplog.rs";
  private static final String LOCAL = "local";
  private static final String TS = "ts";
  private static final String SERTMP = "BSONTimestamp.ser";
  private String dbname;
  private String user;
  private String password;
  private static final int DEFAULTPORT = 27017;
  static MailInformUtil mailInformUtil;

  /**
   * 初始化
   */
  public MongodbSync() {
    this.dbname = PropertiesUtil.PropertyGetValue("mongodb.properties", "mongodb.db");
    this.user = PropertiesUtil.PropertyGetValue("mongodb.properties", "mongodb.username");
    this.password = PropertiesUtil.PropertyGetValue("mongodb.properties", "mongodb.pwd");
  }
  /**
   * 获取数据集合
   */
  public DBCollection getDBCollection() {
    DB database;
    //打开链接
    MongoClient mongoClient = null;
    DBCollection collections = null;
    try {
      MongoCredential credential = MongoCredential
          .createMongoCRCredential(user, dbname, password.toCharArray());
      mongoClient = new MongoClient(this.getSeeds(), Arrays.asList(credential));
      //获取其他库作为跳转，然后跳向local库
      database = mongoClient.getDB(dbname);
      //权限验证
      boolean auth = database.authenticate(user, password.toCharArray());
      if(!auth){
        mailInformUtil.sendMail("权限校验失败，请检查账户设置。");
        if (mongoClient != null) {
          mongoClient.close();
        }
      }else{
        //跳向local库
        database = mongoClient.getDB(LOCAL);
        collections = database.getCollection(OPLOG);
      }
    } catch (UnknownHostException e) {
      e.printStackTrace();
      //释放链接
      if (mongoClient != null) {
        mongoClient.close();
      }
    }
    return collections;
  }


  public static void main(String[] args) throws UnknownHostException {
    int timestamp = 1;
    /**
     * <p>
     * 如果参数不为空，以参数时间戳为准，如果为空，去最新游标记录，如果没有游标记录则判断从1970年开始
     * 参数时间格式 2017-05-05 15:40:00
     * args[0] 为 2017-05-05
     * args[1] 为 15:40:00
     * </p>
    * */
    if(args.length>0){
      if((args[0]!=null||args[0]!="")&&(args[1]!=null||args[1]!="")){
          timestamp = MongodbSync.getBsonTimesTamp(args[0]+" "+args[1]);
      }
    }
    MongodbSync.syncMongoData(timestamp);
  }


  /**
   * @syncMongoData 扫描并开始同步数据
   */
  public static void syncMongoData(int timestamp) {
    mailInformUtil = new MailInformUtil();
    MongodbSync mongodbSync = new MongodbSync();
    DBCollection collection = mongodbSync.getDBCollection();
    if (collection != null) {
      Logger logger = Logger.getLogger("MongodbSync");
      BSONTimestamp ts;
      if (timestamp != 1) {
        ts = new BSONTimestamp(timestamp, 1);
      } else {
        ts = (BSONTimestamp) BsonTimSerializableSave.getBsonObj(SERTMP);
        if (ts == null) {
          ts = new BSONTimestamp(1, 1);
        }
      }
      int i = 0;
      while (true) {
        try {
          final BasicDBObject query = new BasicDBObject();
          query.append(TS, new BasicDBObject(QueryOperators.GT, ts));
          DBCursor cursor = collection.find(query).sort(new BasicDBObject("$natural", 1));
          while (cursor != null && cursor.hasNext()) {
            DBObject obj = cursor.next();
            logger.info(String.valueOf(obj));
            if (!cursor.hasNext()) {
              ts = (BSONTimestamp) obj.get(TS);
              //断点存储
              BsonTimSerializableSave.saveBsonObj(ts, SERTMP);
            }
            //数据正常运转，然后进行初始化，
            i = 0;
          }
          //释放DBCursor
          cursor.close();
          try {
            Thread.sleep(1000 * 60); //一分钟后进行下一次扫描
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        } catch (MongoException e) {
          //断点存储
          BsonTimSerializableSave.saveBsonObj(ts, SERTMP);
          if (i >= 1000) {
            mailInformUtil.sendMail("连续异常已经超过1000，程序退出，请及时处理，请注意查看邮件。异常如下:" + e.toString());
            break;
          } else {
            i++;
          }
        }
      }
    }
  }

  /**
   * 获取mongoDB服务器地址列表
   */
  private List<ServerAddress> getSeeds() throws NumberFormatException, UnknownHostException {
    List<ServerAddress> seeds = new ArrayList<ServerAddress>();
    String[] hostPorts = PropertiesUtil.PropertyGetValue("mongodb.properties", "host").split(",");
    String[] hp = null;
    String host = null;
    int port = 0;
    for (String hostPort : hostPorts) {
      hp = hostPort.split(":");
      if (hp == null || hp.length <= 0) {
        host = "127.0.0.1";
        port = DEFAULTPORT;
      } else {
        if (hp.length == 1) {
          host = hp[0];
          port = DEFAULTPORT;
        } else {
          host = hp[0];
          port = Integer.parseInt(hp[1]);
        }
      }
      seeds.add(new ServerAddress(host, port));
    }
    return seeds;
  }

  /**根据时间戳来确定同步日期时间*/
  public static int getBsonTimesTamp(String strDate) {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    ParsePosition pos = new ParsePosition(0);
    Date strtodate = formatter.parse(strDate, pos);
    long time =strtodate.getTime()/1000L;
    int resulttamp = (int) time;
    return resulttamp;
  }
}
