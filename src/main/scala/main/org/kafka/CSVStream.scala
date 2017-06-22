package main.org.kafka
import org.apache.spark.sql.SparkSession
import org.apache.log4j.Logger
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions.window
import org.apache.spark.sql.Dataset


object CSVStream extends Serializable {
case class Uber(time: Double, latitude: Double, longitude: Double, location: String) extends Serializable
  @transient lazy val logger =  Logger.getLogger(this.getClass())
  
  // Uber schema for CSV  
  val uberSchema = new StructType()
                    .add("time", "timestamp")
                    .add("latitude", "double")
                    .add("longitude", "double")
                    .add("location", "string")
  
  def main(args: Array[String]):Unit = {
  
    // SparkSession creation    
    val spark = SparkSession
                  .builder()
                  .config("spark.executor.memory", "2g")
                  .config("spark.driver.memory", "2g")
                  .enableHiveSupport()
                  .master("local[*]")
                  .getOrCreate()
                  
    import spark.implicits._
    
    // Read stream from csv source    
    val DF = spark
                .readStream
                .format("csv")
                .schema(uberSchema)
                .load("/home/vaibhav/SimpleApp/data/")
                .as[Uber]
    
    // Dataset basically allows us to perform functional programming concepts as well as Dataframe manipulations.
    // So we get the best of both. To create dataset we need to pass the structure schema
    // Here I am performing simple conditional where operation on dataset.
    val FilteredDF = DF.where($"latitude" > 40 && $"longitude" < -72 && $"location" === "B02617")
    
    // Filter operation on dataset.    
    val MappedDF = FilteredDF.filter { line => 
      line.latitude > 41
    }
    
    // Sink to console    
    val query = MappedDF
                .writeStream
                .format("console")
//                .outputMode("complete")
                .start()
                
    query.awaitTermination()
    spark.close()
  }
}