
CURRENT_DATE=`date +"%Y-%m-%d %T" `
gradle clean build;
java -jar ./build/libs/spring-batch-test-0.0.1-SNAPSHOT.jar "item=shoes" "run.date=$CURRENT_DATE";
read;