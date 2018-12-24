import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.alibaba.druid.support.json.JSONUtils;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.SendResult;

public class ProducerTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        //您在控制台创建的 Producer ID
        properties.put(PropertyKeyConst.ProducerId, "PID_SDLH_Dispatch_Vehicle");
        // AccessKey 阿里云身份验证，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.AccessKey,"LTAIip9t4GkzCixq");
        // SecretKey 阿里云身份验证，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.SecretKey, "QxFaTOu4aOPWXHeN5R3LUGKDXitIUq");
        //设置发送超时时间，单位毫秒
        properties.setProperty(PropertyKeyConst.SendMsgTimeoutMillis, "3000");
        // 设置 TCP 接入域名（此处以公共云生产环境为例）
        properties.put(PropertyKeyConst.ONSAddr,
          "http://onsaddr-internet.aliyun.com/rocketmq/nsaddr4client-internet");

        Producer producer = ONSFactory.createProducer(properties);
        // 在发送消息前，必须调用 start 方法来启动 Producer，只需调用一次即可
        producer.start();
        
        Map<String,Object> orderInfo = new HashMap<String,Object>();
        orderInfo.put("orderId", 1);
        orderInfo.put("useTime", "2018-09-03 18:30");
        orderInfo.put("startPointName", "西三环物流中心");
        orderInfo.put("startPointAddress", "北辰大道34号");
        orderInfo.put("startPointLocation", "86.45845854,108.15948511");
        orderInfo.put("endPointName", "西三环物流中心");
        orderInfo.put("endPointAddress", "友谊西路5号");
        orderInfo.put("endPointLocation", "86.458487854,108.15948511");
        orderInfo.put("specialRequirement", "全拆座,带尾板");
        orderInfo.put("remark", "车内整洁，有两人跟车,司机帮忙搬东西");
        orderInfo.put("totalFee", "159");
        orderInfo.put("startFee", "69");
        orderInfo.put("extraFee", "50");
        orderInfo.put("attachFee", "40");
        orderInfo.put("payStatus", "0");
        //循环发送消息
        for (int i = 0; i < 1; i++){
        	System.out.println(JSONUtils.toJSONString(orderInfo));
            Message msg = new Message( //
                // Message 所属的 Topic
                "Topic_SDLH_Dispatch_Vehicle",
                // Message Tag 可理解为 Gmail 中的标签，对消息进行再归类，方便 Consumer 指定过滤条件在 MQ 服务器过滤
                "TagA",
                // Message Body 可以是任何二进制形式的数据， MQ 不做任何干预，
                // 需要 Producer 与 Consumer 协商好一致的序列化和反序列化方式
                
                JSONUtils.toJSONString(orderInfo).getBytes());
            // 设置代表消息的业务关键属性，请尽可能全局唯一。
            // 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
            // 注意：不设置也不会影响消息正常收发
            msg.setKey("ORDERID_" + i);

            try {
                SendResult sendResult = producer.send(msg);
                // 同步发送消息，只要不抛异常就是成功
                if (sendResult != null) {
                    System.out.println(new Date() + " Send mq message success. Topic is:" + msg.getTopic() + " msgId is: " + sendResult.getMessageId());
                }
            }
            catch (Exception e) {
                // 消息发送失败，需要进行重试处理，可重新发送这条消息或持久化这条数据进行补偿处理
                System.out.println(new Date() + " Send mq message failed. Topic is:" + msg.getTopic());
                e.printStackTrace();
            }
        }

        // 在应用退出前，销毁 Producer 对象
        // 注意：如果不销毁也没有问题
        producer.shutdown();
    }
}