package com.example.demo.controller.Classes;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.sql.Types;
@Component
public class Receiver {
    private final static String QUEUE_NAME = "nidhogg";
    private String host="101.133.231.147";
    Channel channel;
    @Autowired
    JdbcTemplate jdbcTemplate;

    public Receiver(){
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
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");
                String sql="INSERT INTO LOG VALUES (null,null,?)";
                if(jdbcTemplate!=null)
                    jdbcTemplate.update(sql,new Object[]{message}, new int[]{Types.VARCHAR});
            };
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void Close(){
        try{
            channel.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void consume(){
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };
        try{
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
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

