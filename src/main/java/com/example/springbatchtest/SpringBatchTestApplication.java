package com.example.springbatchtest;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;

import java.util.List;

@SpringBootApplication
@EnableBatchProcessing
public class SpringBatchTestApplication {

	public static String[] tokens = new String[] {"order_id", "first_name", "last_name", "email", "cost", "item_id", "item_name", "ship_date"};

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public ItemReader<Order> itemReader() {
		FlatFileItemReader<Order> itemReader = new FlatFileItemReader<>();
		itemReader.setLinesToSkip(1);
		itemReader.setResource(new FileSystemResource("shipped_orders.csv"));

		DefaultLineMapper<Order> lineMapper = new DefaultLineMapper<Order>();
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setNames(tokens);

		lineMapper.setLineTokenizer(tokenizer);

		lineMapper.setFieldSetMapper(new OrderFieldSetMapper());

		itemReader.setLineMapper(lineMapper);
		return itemReader;
	}

	@Bean
	public Step chunkBasedStep() {
		return this.stepBuilderFactory.get("chunkBasedStep")
				.<Order,Order>chunk(3)
				.reader(itemReader())
				.writer(new ItemWriter<Order>() {
					@Override
					public void write(List<? extends Order> items) throws Exception {
						System.out.println(String.format("Received list of size: %s", items.size()));
						items.forEach(System.out::println);
					}
				}).build();
	}

	@Bean
	public Job job(){
		return this.jobBuilderFactory.get("job")
				.start(chunkBasedStep())
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchTestApplication.class, args);
	}

}
