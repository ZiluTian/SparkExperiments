package exp.lineageOverhead

import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel._
import util._

// For an iterative application, see if the time per iteration increases as the application proceed
object LineageOverheadExp3 extends SparkTest {

  def main(args: Array[String]): Unit = {
    val totalNumber: Int = args(0).toInt
    val totalTurn: Int = args(1).toInt
    val debugMode: Boolean = args(2).toInt == 1

    // Create an artificial data set (a collection of ints)
    var numberRDD: RDD[Int] = sc.parallelize(Range(0, totalNumber))

    var currentTurn: Int = 0
    while (currentTurn < totalTurn) {
      val begin = System.currentTimeMillis() 
      numberRDD = numberRDD.map(i => i + 1).persist(MEMORY_ONLY_SER)
      numberRDD.count()
      val end = System.currentTimeMillis() 
      println(f"Iteration ${currentTurn} takes ${(end - begin)} ms")
      currentTurn += 1
    }

    if (debugMode) {
      println("The resulting RDD is ")
      println(numberRDD.collect().toList)   
    }
  }
}