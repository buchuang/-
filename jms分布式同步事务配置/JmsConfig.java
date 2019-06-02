package com.bcc.jmsdemo.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.connection.TransactionAwareConnectionFactoryProxy;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;

/**
 * Created by mavlarn on 2018/1/26.
 */
@Configuration
public class JmsConfig {
	
	@Bean
	public ConnectionFactory connectionFactory(){
		ConnectionFactory cf=new ActiveMQConnectionFactory("tcp://192.168.99.100:61616");
		TransactionAwareConnectionFactoryProxy proxy=new TransactionAwareConnectionFactoryProxy();
		proxy.setTargetConnectionFactory(cf);
		proxy.setSynchedLocalTransactionAllowed(true);
		return proxy;
	}

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, MessageConverter jacksonJmsMessageConverter) {
        JmsTemplate template = new JmsTemplate(connectionFactory);        // JmsTemplate使用的connectionFactory跟JmsTransactionManager使用的必须是同一个，不能在这里封装成caching之类的。
        template.setMessageConverter(jacksonJmsMessageConverter);
        template.setSessionTransacted(true);
        return template;
    }

    // 这个用于设置 @JmsListener使用的containerFactory
    @Bean
    public JmsListenerContainerFactory<?> msgFactory(ConnectionFactory connectionFactory,
                                                     DefaultJmsListenerContainerFactoryConfigurer configurer,
                                                     PlatformTransactionManager transactionManager) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setReceiveTimeout(10000L);
        factory.setTransactionManager(transactionManager);
        factory.setConcurrency("10");
        return factory;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter(){
        MappingJackson2MessageConverter converter=new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

}
