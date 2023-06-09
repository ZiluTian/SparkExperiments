package exp.lineageOverhead

import org.apache.spark.rdd.RDD
import util._

// Logic RDD, checkpoint but without persisting to disk
object LineageOverheadExp5 extends SparkTest {

  def main(args: Array[String]): Unit = {
    val totalNumber: Int = args(0).toInt
    val totalTurn: Int = args(1).toInt
    val debugMode: Boolean = args(2).toInt == 1
    // Create an artificial data set (a collection of ints)
    var numberRDD: RDD[Int] = sc.parallelize(Range(0, totalNumber))

    var currentTurn: Int = 0
    while (currentTurn < totalTurn) {
      val begin = System.currentTimeMillis() 
      numberRDD = numberRDD.map(i => i + 1).cache()
      numberRDD.localCheckpoint()
    //   println(numberRDD.toDebugString)
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