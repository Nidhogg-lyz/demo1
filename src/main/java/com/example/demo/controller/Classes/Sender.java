package com.example.demo.controller.Classes;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import org.springframework.stereotype.Component;

@Component
public class Sender {
    private final static String QUEUE_NAME = "nidhogg";
    private String host="101.133.231.147";
    private Channel channel;
    public Sender(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(5672);
        factory.setPassword("123456");
        factory.setUsername("user01");
        try{
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            this.channel=channel;
            this.channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public Channel getChannel() throws Exception{
        return this.channel;
    }
    public void Close(){
        try{
            channel.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void finalize(){
        Close();
    }
}
