package com.sdkj.dispatch;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import java.util.Properties;

public class ConsumerTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        // 您在控制台创建的 Consumer ID
        properties.put(PropertyKeyConst.ConsumerId, "CID_SDLH_Dispatch_Vehicle");
        // AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.AccessKey, "LTAIip9t4GkzCixq");
        // SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.SecretKey, "QxFaTOu4aOPWXHeN5R3LUGKDXitIUq");
        // 设置 TCP 接入域名（此处以公共云生产环境为例）
        properties.put(PropertyKeyConst.ONSAddr,
          "http://onsaddr-internet.aliyun.com/rocketmq/nsaddr4client-internet");
          // 集群订阅方式 (默认)
          // properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.CLUSTERING);
          // 广播订阅方式
          // properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.BROADCASTING);
        Consumer consumer = ONSFactory.createConsumer(properties);
        consumer.subscribe("Topic_SDLH_Dispatch_Vehicle", "TagA||TagB", new MessageListener() { //订阅多个 Tag
            public Action consume(Message message, ConsumeContext context) {
            	System.out.println("receive start");
            	try {
                    System.out.println("Receive2: " + new String(message.getBody()));
                    Thread.sleep(6*1000);
                    
            	}catch(Exception e) {
            		e.printStackTrace();
            	}
            	System.out.println("receive end");
                return Action.CommitMessage;
            }
        });
        //订阅另外一个 Topic
//        consumer.subscribe("Topic_SDLH_Dispatch_Vehicle", "*", new MessageListener() { //订阅全部 Tag
//            public Action consume(Message message, ConsumeContext context) {
//                System.out.println("Receive23: " + message);
//                return Action.CommitMessage;
//            }
//        });
        consumer.start();
        System.out.println("Consumer Started");
    }
}