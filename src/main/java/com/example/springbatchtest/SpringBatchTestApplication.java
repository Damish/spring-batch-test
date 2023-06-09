package com.example.springbatchtest;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@SpringBootApplication
@EnableBatchProcessing
public class SpringBatchTestApplication {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public JobExecutionDecider deliveryDecider() {
        return new DeliveryDecider();
    }

    @Bean
    public JobExecutionDecider receiptDecider() {
        return new ReceiptDecider();
    }

    @Bean
    public Flow deliveryFlow(){
        return new FlowBuilder<SimpleFlow>("deliveryFlow")
                .start(driveToAdressStep())
                .on("FAILED").fail()
                .from(driveToAdressStep())
                .on("*").to(deliveryDecider())
                .on("PRESENT").to((givePackageToCustomerStep()))
                .next(receiptDecider()).on("CORRECT").to(thankCustomerStep())
                .from(receiptDecider()).on("INCORRECT").to(refundStep())
                .from(deliveryDecider())
                .on("NOT_PRESENT").to((leaveAtDoorStep()))
                .build();
    }

    @Bean
    public StepExecutionListener selectFlowerListener(){
        return new FlowersSelectionStepExecutionListener();
    }

/*
    @Bean
    public Step nestedBillingJobStep(){
        return this.stepBuilderFactory.get("nestedBillingJobStep").job(billingJob()).build();
    }
*/

    @Bean
    public Step sendInvoiceStep() {
        return this.stepBuilderFactory.get("invoiceStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Invoice is send to the customer");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Flow billingFlow(){
        return new FlowBuilder<SimpleFlow>("billingFlow").start(sendInvoiceStep()).build();
    }

    @Bean
    public Step selectFLowersStep() {
        return this.stepBuilderFactory.get("selectFLowersStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Gathering flowers for order");
                return RepeatStatus.FINISHED;
            }
        }).listener(selectFlowerListener()).build();
    }

    @Bean
    public Step removeThronesStep() {
        return this.stepBuilderFactory.get("removeThronesStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Remove thrones from roses");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step arrangeFLowersStep() {
        return this.stepBuilderFactory.get("arrangeFLowersStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Arranging flowers for order");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Job prepareFlowers(){
        return this.jobBuilderFactory.get("prepareFlowersJob")
                .start(selectFLowersStep())
                    .on("TRIM_REQUIRED").to(removeThronesStep()).next(arrangeFLowersStep())
                .from(selectFLowersStep())
                    .on("NO_TRIM_REQUIRED").to(arrangeFLowersStep())
                .from(arrangeFLowersStep())
                    .on("*").to(deliveryFlow())
                .end()
                .build();
    }

    @Bean
    public Step thankCustomerStep() {
        return this.stepBuilderFactory.get("thankCustomerStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Thank Customer");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step refundStep() {
        return this.stepBuilderFactory.get("refundStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Giving customer refund");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step leaveAtDoorStep() {
        return this.stepBuilderFactory.get("leaveAtDoorStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Leaving the package at the door");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step storePackageStep() {
        return this.stepBuilderFactory.get("storePackageStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Storing the package while the customer address is located");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step givePackageToCustomerStep() {
        return this.stepBuilderFactory.get("givePackageToCustomerStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Given the package to the customer");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step driveToAdressStep() {
        boolean GOT_LOST = false;
        return this.stepBuilderFactory.get("driveToAddressStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                if (GOT_LOST) {
                    throw new RuntimeException("Got lost driving to the address");
                }
                System.out.println("Successfully arrived at the address");
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Step packageItemStep() {
        return this.stepBuilderFactory.get("packageItemStep").tasklet(new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                String item = chunkContext.getStepContext().getJobParameters().get("item").toString();
                String date = chunkContext.getStepContext().getJobParameters().get("run.date").toString();

                System.out.println(String.format("The %s has been packaged on %s .", item, date));
                return RepeatStatus.FINISHED;
            }
        }).build();
    }

    @Bean
    public Job deliverPackageJob() {
        return this.jobBuilderFactory.get("deliverPackageJob")
                .start(packageItemStep())
                .split(new SimpleAsyncTaskExecutor())
                .add(deliveryFlow(),billingFlow())
                .end()
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchTestApplication.class, args);
    }

}
