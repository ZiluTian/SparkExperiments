package simulations.v1

import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}
import scala.collection.mutable.{Buffer}

import collection.JavaConverters._


// Use both actor RDD and message RDD
object SimulateMessageRDD {
  import simulations.util.Simulate._
  def apply(actors: List[Actor], totalTurn: Int): Unit = {

    var currentTurn: Int = 0
    var t1: Long = 0
    var t2: Long = 0

    var totalTime: Long = 0
    var elapsedRound: Int = 0
    var time_seq: List[Long] = List()

    var actorRDD: RDD[(Long, Actor)] = sc.parallelize(actors).map(i => (i.id, i))
    var messageRDD: RDD[(Long, List[Message])] = actorRDD.mapValues(i => List())

    while (currentTurn < totalTurn ) {
        t1 = System.currentTimeMillis()

        actorRDD = actorRDD.leftOuterJoin(messageRDD).mapValues(x => {
          x._1.run(x._2.getOrElse(List[Message]()))
        }).cache()
        messageRDD.unpersist()
        messageRDD = actorRDD.flatMap(x => x._2.sendMessages.map(m => (m._1, m._2.toList))).reduceByKey((m1, m2) => m1 ::: m2)
        elapsedRound = actorRDD.map(x => x._2.proposeInterval).collect().min
        actorRDD.localCheckpoint()
        currentTurn += elapsedRound
        messageRDD.persist(StorageLevel.MEMORY_ONLY_SER)
        messageRDD.count()
        messageRDD.localCheckpoint()
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