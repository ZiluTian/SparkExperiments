package simulations

import org.apache.spark.rdd.RDD
import scala.collection.mutable.{Buffer}
import scala.reflect.ClassTag
import util._

// Use both actor RDD and message RDD
object Simulate extends SparkTest {

  def apply[V: ClassTag, M: ClassTag](
        agents: List[(Long, V)],
        run: (Long, V, List[M]) => V, 
        sendMessage: (Long, V) => M,
        edges: Map[Long, List[Long]],
        initialMessage: List[M], 
        totalTurn: Int): Unit = {

    var currentTurn: Int = 1
    var t1: Long = 0
    var t2: Long = 0

    var totalTime: Long = 0
    var elapsedRound: Int = 1
    var time_seq: List[Long] = List()
    val checkpointInterval: Int = 10
    
    var actorRDD: RDD[(Long, V)] = sc.parallelize(agents)
    var messageRDD: RDD[(Long, List[M])] = null
    val edgeRDD: RDD[(Long, List[Long])] = sc.parallelize(edges.toSeq)

    // Initialization
    actorRDD = actorRDD.map(i => (i._1, run(i._1, i._2, initialMessage)))
    actorRDD.count()
    val actorCheckpointer = new PeriodicRDDCheckpointer[(Long, V)](checkpointInterval, sc)
    actorCheckpointer.update(actorRDD)

    val messageCheckpointer = new PeriodicRDDCheckpointer[(Long, List[M])](checkpointInterval, sc)

    messageRDD = actorRDD.map(i => (i._1, sendMessage(i._1, i._2)))
      .leftOuterJoin(edgeRDD).flatMap(i => i._2._2.getOrElse(List()).map(r => (r, List(i._2._1)))).cache()
      .reduceByKey((m1, m2) => m1 ::: m2)
    messageRDD.count()
    messageCheckpointer.update(messageRDD)

    currentTurn += 1

    var prevActors: RDD[(Long, V)] = null

    while (currentTurn < totalTurn ) {
        t1 = System.currentTimeMillis()

        prevActors = actorRDD
        // Join vertex with messages
        actorRDD = actorRDD.leftOuterJoin(messageRDD).map(i => {
          (i._1, run(i._1, i._2._1, i._2._2.get))
        }).cache()
        actorRDD.count()
        actorCheckpointer.update(actorRDD)
        
        // send messages
        val oldMessages = messageRDD
        messageRDD = actorRDD.map(i => (i._1, sendMessage(i._1, i._2)))
          .leftOuterJoin(edgeRDD).flatMap(i => i._2._2.getOrElse(List()).map(r => (r, List(i._2._1)))).cache()
          .reduceByKey((m1, m2) => m1 ::: m2)
        messageRDD.count()

        messageCheckpointer.update(messageRDD)
        oldMessages.unpersist()
        prevActors.unpersist()

        t2 = System.currentTimeMillis()
        time_seq = time_seq ::: List(t2-t1)
        println(f"Iteration ${currentTurn} takes ${t2-t1} ms")
        currentTurn += elapsedRound
      }

    // val average = time_seq.sum / time_seq.length
    val average = time_seq.sum / totalTurn
    println(f"Average time per round ${average}")
    sc.stop()
  }
}