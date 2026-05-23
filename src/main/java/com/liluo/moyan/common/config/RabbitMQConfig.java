package com.liluo.moyan.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 */
@Configuration
public class RabbitMQConfig {
    
    // ==================== 点赞/收藏队列 ====================
    public static final String LIKE_EXCHANGE = "like.exchange";
    public static final String LIKE_QUEUE = "like.queue";
    public static final String LIKE_ROUTING_KEY = "like.routingkey";
    public static final String LIKE_DLQ = "like.dlq";
    public static final String LIKE_DLX_EXCHANGE = "like.dlx.exchange";
    
    // ==================== 通知队列 ====================
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.routingkey";
    public static final String NOTIFICATION_DLQ = "notification.dlq";
    public static final String NOTIFICATION_DLX_EXCHANGE = "notification.dlx.exchange";
    
    // ==================== ES 同步队列 ====================
    public static final String ES_SYNC_EXCHANGE = "es.sync.exchange";
    public static final String ES_SYNC_QUEUE = "es.sync.queue";
    public static final String ES_SYNC_ROUTING_KEY = "es.sync.routingkey";
    
    // ==================== 点赞队列配置 ====================
    
    @Bean
    public Queue likeQueue() {
        return QueueBuilder.durable(LIKE_QUEUE)
                .withArgument("x-dead-letter-exchange", LIKE_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", LIKE_ROUTING_KEY)
                .build();
    }
    
    @Bean
    public DirectExchange likeExchange() {
        return new DirectExchange(LIKE_EXCHANGE);
    }
    
    @Bean
    public Binding likeBinding() {
        return BindingBuilder.bind(likeQueue()).to(likeExchange()).with(LIKE_ROUTING_KEY);
    }
    
    @Bean
    public Queue likeDlq() {
        return QueueBuilder.durable(LIKE_DLQ).build();
    }
    
    @Bean
    public DirectExchange likeDlxExchange() {
        return new DirectExchange(LIKE_DLX_EXCHANGE);
    }
    
    @Bean
    public Binding likeDlqBinding() {
        return BindingBuilder.bind(likeDlq()).to(likeDlxExchange()).with(LIKE_ROUTING_KEY);
    }
    
    // ==================== 通知队列配置 ====================
    
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_ROUTING_KEY)
                .build();
    }
    
    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }
    
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(notificationExchange()).with(NOTIFICATION_ROUTING_KEY);
    }
    
    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }
    
    @Bean
    public DirectExchange notificationDlxExchange() {
        return new DirectExchange(NOTIFICATION_DLX_EXCHANGE);
    }
    
    @Bean
    public Binding notificationDlqBinding() {
        return BindingBuilder.bind(notificationDlq()).to(notificationDlxExchange()).with(NOTIFICATION_ROUTING_KEY);
    }
    
    // ==================== ES 同步队列配置 ====================
    
    @Bean
    public Queue esSyncQueue() {
        return QueueBuilder.durable(ES_SYNC_QUEUE).build();
    }
    
    @Bean
    public DirectExchange esSyncExchange() {
        return new DirectExchange(ES_SYNC_EXCHANGE);
    }
    
    @Bean
    public Binding esSyncBinding() {
        return BindingBuilder.bind(esSyncQueue()).to(esSyncExchange()).with(ES_SYNC_ROUTING_KEY);
    }
    
    // ==================== 消息转换器配置 ====================
    
    /**
     * 配置 JSON 消息转换器（替代默认的 Java 序列化）
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * 配置 RabbitTemplate 使用 JSON 消息转换器
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
    
    /**
     * 配置监听器容器工厂使用 JSON 消息转换器
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}
