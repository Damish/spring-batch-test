
CURRENT_DATE=`date +"%Y-%m-%d %T" `
gradle clean build -x test;
java -jar -Dspring.batch.job.names=prepareFlowersJob ./build/libs/spring-batch-test-0.0.1-SNAPSHOT.jar "run.date=2023/04/18" type=$1;
#java -jar ./build/libs/spring-batch-test-0.0.1-SNAPSHOT.jar "item=shoes" "run.date=$CURRENT_DATE";
read;