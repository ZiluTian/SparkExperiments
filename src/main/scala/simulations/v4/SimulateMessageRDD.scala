package simulations.v4

import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}
import scala.collection.mutable.{Buffer}

import collection.JavaConverters._


// Use both actor RDD and message RDD
object SimulateMessageRDD {
  import simulations.util.Simulate._
  def apply(actors: List[Actor], edges: Map[Long, List[Long]], totalTurn: Int): Unit = {

    var currentTurn: Int = 0
    var t1: Long = 0
    var t2: Long = 0

    var totalTime: Long = 0
    var elapsedRound: Int = 0
    var time_seq: List[Long] = List()

    var actorRDD: RDD[(Long, Actor)] = sc.parallelize(actors).map(i => (i.id, i))
    val edgeRDD: RDD[(Long, List[Long])] = sc.parallelize(edges.toSeq)
    var messageRDD: RDD[(Long, List[Any])] = actorRDD.mapValues(i => List())

    while (currentTurn < totalTurn ) {
        t1 = System.currentTimeMillis()

        messageRDD = actorRDD.leftOuterJoin(messageRDD)       
          .mapValues(x => {x._1.run(x._2.getOrElse(List()))}) // RDD[Long, List[Any]]
          .leftOuterJoin(edgeRDD).cache() // RDD[Long, (List[Any], Option[List[Long]])]
          .flatMap(x => x._2._2.getOrElse(List()).map(i => (i, x._2._1)))
          .reduceByKey((m1, m2) => m1 ::: m2).cache()
        
        messageRDD.count()
        // messageRDD.localCheckpoint()
        
        elapsedRound = actorRDD.map(x => x._2.proposeInterval).collect().min
        actorRDD.localCheckpoint()
        currentTurn += elapsedRound
        
        t2 = System.currentTimeMillis()
        time_seq = time_seq ::: List(t2-t1)
        println(f"Iteration ${currentTurn} takes ${t2-t1} ms")
    }

    // val average = time_seq.sum / time_seq.length
    val average = time_seq.sum / totalTurn
    println(f"Average time per round ${average}")
    sc.stop()
  }
}