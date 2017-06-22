import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.streaming.StreamingQuery;

object SimpleClass {
  
  case class Person(firstName: String, lastname: String, sex: String, age: Long) extends Serializable
  
  val INPUT_DIR = "/home/vaibhav/Desktop/input"
  
  def main(args: Array[String]) {
    //1 - Start the Spark session
    val spark = SparkSession
                .builder()
                .config("spark.eventlog.enabled", "false")
                .config("spark.driver.memory", "2g")
                .config("spark.executor.memory", "2g")
                .appName("StructuredSparkStreaming")
                .master("local[*]")
                .getOrCreate()
                
    spark.sparkContext.setLogLevel("WARN")
    
    //2- Define the input data schema
    import spark.implicits._
    
    val PersonSchema = new StructType()
                  .add("firstName", "string")
                  .add("lastname", "string")
                  .add("sex", "string")
                  .add("age", "long")
                  
    //3 - Create a Data set representing the stream of input files
    val df: Dataset[Person] = spark.readStream.schema(PersonSchema).json(INPUT_DIR).as[Person]
    
    //When data arrives from the stream, these steps will get executed
    
    //4 - Create a temporary table so we can use SQL queries  
    df.createOrReplaceTempView("people")
    val ageAverage = spark.sql("SELECT AVG(age) as average_age, sex FROM people GROUP BY sex")
    
    //5 - Write the the output of the query to the console
    val query = ageAverage.writeStream
      .outputMode("complete")
      .format("console")
      .start()

    query.awaitTermination();
  }
}