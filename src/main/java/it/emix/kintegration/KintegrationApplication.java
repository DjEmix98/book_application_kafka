package it.emix.kintegration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.kafka.Kafka;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;

import it.emix.kintegration.domain.Book;
import it.emix.kintegration.utils.BookPublisher;

@SpringBootApplication
public class KintegrationApplication {

	@Autowired
	PollableChannel consumerChannel;

	public static void main(String[] args) {
		String arg2 [] = {"fantasy", "horror", "romance", "thriller"};
		ConfigurableApplicationContext context = new SpringApplicationBuilder(KintegrationApplication.class).run(arg2);

		List<String> valid_topics = Arrays.asList("fantasy", "horror", "romance", "thriller");

		List<String> topics = new ArrayList<>();
		if (arg2.length > 0) {
			for (String arg : arg2) {
				if (valid_topics.contains(arg))
					topics.add(arg);
			}
		}

		context.getBean(KintegrationApplication.class).run(context, topics);
		context.close();
	}

	private void run(ConfigurableApplicationContext context, List<String> topics) {

		System.out.println("Inside ConsumerApplication run method...");
		PollableChannel consumerChannel = context.getBean("consumerChannel", PollableChannel.class);

		for (String topic : topics)
			addAnotherListenerForTopics(topic);

		Message received = consumerChannel.receive();
		while (received != null) {
			received = consumerChannel.receive();
			Book book = null;
			BeanUtils.copyProperties(received.getPayload(), book);
			System.out.println("Received " + book);
		}
	}

	@Autowired
	private IntegrationFlowContext flowContext;

	@Autowired
	private KafkaProperties kafkaProperties;

	public void addAnotherListenerForTopics(String... topics) {
		Map consumerProperties = kafkaProperties.buildConsumerProperties();
		IntegrationFlow flow = IntegrationFlows
				.from(Kafka.messageDrivenChannelAdapter(
						new DefaultKafkaConsumerFactory(consumerProperties), topics))
				.channel("consumerChannel").get();
		this.flowContext.registration(flow).register();
	}
}
