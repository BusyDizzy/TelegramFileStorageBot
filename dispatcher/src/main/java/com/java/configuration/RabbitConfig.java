package com.java.configuration;

import com.java.model.RabbitQueue;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue textMessageQueue() {
        return QueueBuilder.durable(RabbitQueue.TEXT_MESSAGE_UPDATE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", RabbitQueue.DLX_TEXT_MESSAGE_UPDATE)
                .build();
    }

    @Bean
    public Queue docMessageQueue() {
        return QueueBuilder.durable(RabbitQueue.DOC_MESSAGE_UPDATE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", RabbitQueue.DLX_DOC_MESSAGE_UPDATE)
                .build();
    }

    @Bean
    public Queue answerMessageQueue() {
        return QueueBuilder.durable(RabbitQueue.ANSWER_MESSAGE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", RabbitQueue.DLX_ANSWER_MESSAGE)
                .build();
    }

    @Bean
    public Queue dlxTextMessageQueue() {
        return QueueBuilder.durable(RabbitQueue.DLX_TEXT_MESSAGE_UPDATE).build();
    }

    @Bean
    public Queue dlxDocMessageQueue() {
        return QueueBuilder.durable(RabbitQueue.DLX_DOC_MESSAGE_UPDATE).build();
    }

    @Bean
    public Queue dlxAnswerMessageQueue() {
        return QueueBuilder.durable(RabbitQueue.DLX_ANSWER_MESSAGE).build();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setChannelTransacted(true);
        return rabbitTemplate;
    }

}


