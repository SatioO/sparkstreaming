import org.apache.spark.sql.types._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.Column

object SparkStream {
  val INPUT_DIR = "/home/vaibhav/Desktop/input"
  val jsonSchema = new StructType().add("time", TimestampType).add("action", StringType)
  
  def main(args: Array[String]) {
    val spark = SparkSession
                .builder()
                .config("spark.eventlog.enabled", "false")
                .config("spark.driver.memory", "2g")
                .config("spark.executor.memory", "2g")
                .appName("StructuredSparkStreaming")
                .master("local[*]")
                .getOrCreate()
    
    spark.sparkContext.setLogLevel("WARN")
    import spark.implicits._
    
    val staticInputDF = spark.readStream.schema(jsonSchema).json(INPUT_DIR)
    
    val staticCountsDF = staticInputDF.groupBy($"action", window($"time", "1 hour", "5 minutes")).count()
    
    // Register the DataFrame as table 'static_counts'
    staticCountsDF.createOrReplaceTempView("static_counts")
    
    val query = staticCountsDF.writeStream
      .outputMode("complete")
      .format("console")
      .start()
    
    query.awaitTermination();
  }
}