package it.emix.kintegration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import it.emix.kintegration.domain.Book;
import it.emix.kintegration.utils.BookPublisher;

@SpringBootApplication
public class Publisher {

	@Autowired
	private BookPublisher bookPublisher;

	public static void main(String[] args) {
		String arg [] = {"1", "2", "3"};
		ConfigurableApplicationContext context = SpringApplication.run(Publisher.class, arg);
		context.getBean(Publisher.class).run(context);
		context.close();
	}

	private void run(ConfigurableApplicationContext context) {

		System.out.println("Inside ProducerApplication run method...");

		MessageChannel producerChannel = context.getBean("producerChannel", MessageChannel.class);

		List<Book> books = bookPublisher.getBooks();

		for (Book book : books) {
			Map headers = Collections.singletonMap(KafkaHeaders.TOPIC, book.getGenre().toString());
			producerChannel.send(new GenericMessage(book.toString(), headers));
		}

		books.forEach(b -> {
			
			System.out.println("book send id: " + b.getBookId());
			System.out.println("book send Type: " + b.getGenre());
			System.out.println("book send title: " + b.getTitle());
		});
		System.out.println("Finished ProducerApplication run method...");
	}
}
