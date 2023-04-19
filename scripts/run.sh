
gradle clean build -x test;
java -jar -Dspring.batch.job.names=job ./build/libs/spring-batch-test-0.0.1-SNAPSHOT.jar;
#java -jar ./build/libs/spring-batch-test-0.0.1-SNAPSHOT.jar "item=shoes" "run.date=$CURRENT_DATE";
read;