
### branch -> job-parameters
1. gradle clean build
(go inside build/libs)
2. java -jar spring-batch-test-0.0.1-SNAPSHOT.jar "item=shoes" "run.date(date)=2023/04/17"

### branch -> set-job-sequence
1. ./scripts/run.sh

### branch -> exceptions-in-steps
1. toggle boolean value in SpringBatchTestApplication - line 40
2. GOT_LOST = true (exception)
2. In scripts/run.sh -> (put same params for testing)
 java -jar ./build/libs/spring-batch-test-0.0.1-SNAPSHOT.jar "item=shoes" "run.date=2023/04/17";
3. ./scripts/run.sh
(job execution stops at the steps with exceptions)
4. GOT_LOST = false (success)
5. ./scripts/run.sh (same params as above)
(job steps will restart from last stopped step due to exception and continue next steps)

### branch -> conditional-steps-flow
1. toggle boolean value in SpringBatchTestApplication - line 40
2. GOT_LOST = true (exception)
3. ./scripts/run.sh
>>> packageItemStep > driveToAdressStep > storePackageStep
4. clean/drop db tables
5. toggle boolean value in SpringBatchTestApplication - line 40
6. GOT_LOST = false (success)
7. ./scripts/run.sh
>>> packageItemStep > driveToAdressStep > givePackageToCustomerStep

### branch -> job-execution-decider
(flow control with custom status)
1. DeliveryDecider line 13 change "<" or ">"
2. ./scripts/run.sh

### branch -> conditional-flow-exercise
1. ./scripts/run.sh

### branch -> batch-status-control
1. toggle boolean value in SpringBatchTestApplication - line 40
2. ./scripts/run.sh

### branch -> step-execution-listener
1. ./scripts/run_flowers_job.sh
