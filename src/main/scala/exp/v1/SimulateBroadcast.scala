package exp.v1

import org.apache.spark.rdd.RDD
import util.SparkTest

// Use broadcast variable
object SimulateBroadcast extends SparkTest { 

  def apply(actors: List[Actor], totalTurn: Int): Unit = {

    var currentTurn: Int = 0
    var collectedMessages = scala.collection.Map[Long, List[Message]]()

    var t1: Long = 0
    var t2: Long = 0
    var t3: Long = 0

    var totalTime: Long = 0
    var elapsedRound: Int = 0
    var time_seq: List[Long] = List()

    var actorRDD: RDD[Actor] = sc.parallelize(actors)

    while (currentTurn < totalTurn ) {
        t1 = System.currentTimeMillis()

        collectedMessages = actorRDD.flatMap(a => {
          a.sendMessages.map(i => (i._1, i._2.toList))
        }).combineByKey((msgs: List[Message]) => msgs,
          (l: List[Message], message: List[Message]) => message ::: l,
          (l1: List[Message], l2: List[Message]) => l1 ::: l2).collectAsMap()

        val dMessages = sc.broadcast(collectedMessages)
        t2 = System.currentTimeMillis()

        actorRDD = actorRDD.map(x => {
          x.run(dMessages.value.getOrElse(x.id, List[Message]()))
        }).cache()
        
        actorRDD.localCheckpoint()

        elapsedRound = actorRDD.map(i => i.proposeInterval).collect.min
        currentTurn += elapsedRound
        t3 = System.currentTimeMillis()
        println(f"Iteration ${currentTurn} takes ${t3-t1} ms")

        time_seq = time_seq ::: List(t3-t1)
    }

    val updatedActors: List[Actor] = actorRDD.collect.toList
    val average = time_seq.sum / totalTurn
    println(f"Average time per round ${average}")
    sc.stop()
  }
}