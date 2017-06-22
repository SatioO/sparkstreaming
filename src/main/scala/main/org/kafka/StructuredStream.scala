package main.org.kafka

import org.apache.spark.sql.SparkSession
import org.apache.log4j._
import org.apache.spark.streaming.kafka010._

object StructuredStream extends Serializable {
  @transient lazy val logger = Logger.getLogger(this.getClass())
  logger.setLevel(Level.INFO)
  
  def main(args: Array[String]) {
    logger.info("Structured Stream")
    
    val cols = List("user_id", "time", "event")
    
    // 1. Create Spark Session    
    val spark = SparkSession
                    .builder()
                    .config("spark.eventlog.enabled", false)
                    .config("spark.executor.memory", "2g")
                    .config("spark.driver.memory", "2g")
                    .appName("StructuredSparkStreaming")
//                    .enableHiveSupport()
                    .master("local[*]")
                    .getOrCreate()
    
    import spark.implicits._
    
    val lines = spark.readStream
                .format("kafka")
                .option("subscribe", "sparkstreaming")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("startingOffsets", "earliest")
                .load()
                .selectExpr("CAST(value AS STRING)",
                  "CAST(topic as STRING)",
                  "CAST(partition as INTEGER)")
                .as[(String, String, Integer)]
    
    lines.printSchema()
    
    val df =
      lines.map { line =>
        val columns = line._1.split(";") // value being sent out as a comma separated value "userid_1;2015-05-01T00:00:00;some_value"
        (columns(0), columns(1), columns(2))
      }.toDF(cols: _*)
      
    val ds = df.select($"user_id", $"time", $"event")
    
    val query = ds
                  .writeStream
                  .format("console")
                  .start()
    
    query.awaitTermination()
    spark.stop()
    
  }
}