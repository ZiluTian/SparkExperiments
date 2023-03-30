package simulations.v1

import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

object SimulatePeriodicCheckpoint {
  import simulations.util.Simulate._
  import simulations.util._

  def apply(actors: List[Actor], totalTurn: Int): Unit = {

    var currentTurn: Int = 0
    var t1: Long = 0
    var t2: Long = 0
    val checkpointInterval: Int = 5

    var totalTime: Long = 0
    var elapsedRound: Int = 0
    var time_seq: List[Long] = List()

    var actorRDD: RDD[(Long, Actor)] = sc.parallelize(actors).map(i => (i.id, i))
    var messageRDD: RDD[(Long, List[Message])] = actorRDD.mapValues(i => List())

    val actorCheckpointer = new PeriodicRDDCheckpointer[(Long, Actor)](
        checkpointInterval, sc)
    actorCheckpointer.update(actorRDD.asInstanceOf[RDD[(Long, Actor)]])

    val messageCheckpointer = new PeriodicRDDCheckpointer[(Long, List[Message])](
        checkpointInterval, sc)
    messageCheckpointer.update(messageRDD.asInstanceOf[RDD[(Long, List[Message])]])

    var prevActorRDD: RDD[(Long, Actor)] = null

    while (currentTurn < totalTurn ) {
        t1 = System.currentTimeMillis()
        prevActorRDD = actorRDD

        actorRDD = actorRDD.leftOuterJoin(messageRDD).mapValues(x => {
          x._1.run(x._2.getOrElse(List[Message]()))
        })
        actorCheckpointer.update(actorRDD)

        elapsedRound = actorRDD.map(x => x._2.proposeInterval).collect().min

        val oldMessages = messageRDD

        messageRDD = actorRDD.flatMap(x => {
          x._2.sendMessages.map(m => (m._1, m._2.toList))}).reduceByKey((m1, m2) => m1 ::: m2)
        
        messageCheckpointer.update(messageRDD.asInstanceOf[RDD[(Long, List[Message])]])
        
        oldMessages.unpersist()
        prevActorRDD.unpersist()
        
        currentTurn += elapsedRound
        t2 = System.currentTimeMillis()
        println(f"Iteration ${currentTurn} takes ${t2-t1} ms")
        time_seq = time_seq ::: List(t2-t1)
    }

    // val average = time_seq.sum / time_seq.length
    val average = time_seq.sum / totalTurn
    println(f"Average time per round ${average}")
    sc.stop()
  }
}