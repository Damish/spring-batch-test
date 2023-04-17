
### branch -> job-parameters
1. gradle clean build
(go inside build/libs)
2. java -jar spring-batch-test-0.0.1-SNAPSHOT.jar "item=shoes" "run.date(date)=2023/04/17"

### branch -> set-job-sequence
1. ./scripts/run.sh