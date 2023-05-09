package com.example.springbatchtest;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;

@SpringBootApplication
@EnableBatchProcessing
public class SpringBatchTestApplication {

	public static String[] names = new String[] { "orderId", "firstName", "lastName", "email", "cost", "itemId",
			"itemName", "shipDate" };

	public static String[] tokens = new String[] {"order_id", "first_name", "last_name", "email", "cost", "item_id", "item_name", "ship_date"};

	public static String ORDER_SQL = "select order_id, first_name, last_name, "
			+ "email, cost, item_id, item_name, ship_date "
			+ "from SHIPPED_ORDER order by order_id";

	public static String INSERT_ORDER_SQL = "insert into "
			+ "SHIPPED_ORDER_OUTPUT(order_id, first_name, last_name, email, item_id, item_name, cost, ship_date)"
			+ " values(?,?,?,?,?,?,?,?)";

	public static String INSERT_ORDER_SQL_NAMED = "insert into "
			+ "SHIPPED_ORDER_OUTPUT(order_id, first_name, last_name, email, item_id, item_name, cost, ship_date)"
			+ " values(:orderId,:firstName,:lastName,:email,:itemId,:itemName,:cost,:shipDate)";

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public DataSource dataSource;

	@Bean
	public ItemProcessor<TrackedOrder,TrackedOrder> freeShippingItemProcessor() {
		return new FreeShippingItemProcessor();
	}

	@Bean
	public ItemProcessor<Order,TrackedOrder> trackedOrderItemProcessor() {
		return new TrackedOrderItemProcessor();
	}

	@Bean
	public ItemProcessor<Order,TrackedOrder> compositeItemProcessor() {
		return new CompositeItemProcessorBuilder<Order,TrackedOrder>()
				.delegates(trackedOrderItemProcessor(),freeShippingItemProcessor())
				.build();
	}

	@Bean
	public ItemWriter<Order> flatFileItemWriter() { // item writer to csv flat file
		FlatFileItemWriter<Order> itemWriter = new FlatFileItemWriter<>();
		itemWriter.setResource(new FileSystemResource("shipped_orders_output.csv"));
		DelimitedLineAggregator<Order> aggregator = new DelimitedLineAggregator<>();
		aggregator.setDelimiter(",");
		BeanWrapperFieldExtractor<Order> fieldExtractor = new BeanWrapperFieldExtractor<>();
		fieldExtractor.setNames(names);
		aggregator.setFieldExtractor(fieldExtractor);

		itemWriter.setLineAggregator(aggregator);
		return itemWriter;
	}

	@Bean
	public ItemWriter<Order> dbItemWriter() { // item writer to database
		return new JdbcBatchItemWriterBuilder<Order>()
				.dataSource(dataSource)
				//--with prepared statements
				//.sql(INSERT_ORDER_SQL)
				//.itemPreparedStatementSetter(new OrderItemPreparedStatementSetter())
				//--with bean mapped
				.sql(INSERT_ORDER_SQL_NAMED)
				.beanMapped()
				.build();
	}

	@Bean
	public ItemWriter<TrackedOrder> JsonItemWriter() { // item writer to Json file
		return new JsonFileItemWriterBuilder<TrackedOrder>()
				.jsonObjectMarshaller(new JacksonJsonObjectMarshaller<TrackedOrder>())
				.resource(new FileSystemResource("shipped_orders_output_json.json"))
				.name("JsonItemWriter")
				.build();
	}

	@Bean
	public ItemReader<Order> csvFileItemReader() {
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
	public ItemReader<Order> dbItemReader() {
		return new JdbcCursorItemReaderBuilder<Order>()
				.dataSource(dataSource)
				.name("jdbcCursorItemReader")
				.sql(ORDER_SQL)
				.rowMapper(new OrderRowMapper())
				.build();
	}

	private PagingQueryProvider queryProvider() throws Exception {
		SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
		factoryBean.setSelectClause("select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date ");
		factoryBean.setFromClause("from SHIPPED_ORDER");
		factoryBean.setSortKey("order_id");
		factoryBean.setDataSource(dataSource);
		return factoryBean.getObject();
	}

	@Bean
	public ItemReader<Order> pagingDbItemReader() throws Exception {
		return new JdbcPagingItemReaderBuilder<Order>()
				.dataSource(dataSource)
				.name("jdbcCursorItemReader")
				.queryProvider(queryProvider())
				.rowMapper(new OrderRowMapper())
				.pageSize(10) // should march chunk size
				.build();
	}

	@Bean
	public Step chunkBasedStep() throws Exception {
		return this.stepBuilderFactory.get("chunkBasedStep")
				.<Order,TrackedOrder>chunk(10)
				.reader(pagingDbItemReader())
				.processor(compositeItemProcessor())
				.writer(JsonItemWriter())
				.build();
	}

	@Bean
	public Job job() throws Exception {
		return this.jobBuilderFactory.get("job")
				.start(chunkBasedStep())
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchTestApplication.class, args);
	}

}
