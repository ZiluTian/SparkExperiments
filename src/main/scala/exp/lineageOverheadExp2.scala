package exp

import org.apache.spark.rdd.RDD

// For an iterative application, see if the time per iteration increases as the application proceed
// Use two RDDs, one being a local variable, and unpersist the other one
object LineageOverheadExp2 {
  import util.Run.sc

  def main(args: Array[String]): Unit = {
    val totalNumber: Int = args(0).toInt
    val totalTurn: Int = args(1).toInt

    // Create an artificial data set (a collection of ints)
    var numberRDD: RDD[Int] = sc.parallelize(Range(0, totalNumber))

    var currentTurn: Int = 0
    while (currentTurn < totalTurn) {
      val begin = System.currentTimeMillis() 
      val updatedNumberRDD = numberRDD.map(i => i + 1).cache()
      
      updatedNumberRDD.count()
      numberRDD.unpersist()
      numberRDD = updatedNumberRDD

      val end = System.currentTimeMillis() 
      println(f"Iteration ${currentTurn} takes ${(end - begin)} ms")
      currentTurn += 1
    }

    val debugMode = args.size > 2
    if (debugMode) {
      println("The resulting RDD is ")
      println(numberRDD.collect().toList)   
    }
  }
}