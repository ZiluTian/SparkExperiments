// package simulations.v4

// import org.apache.spark.rdd.RDD
// import org.apache.spark.storage.StorageLevel

// // Use three RDDs, swapping actor RDD with updatedActorRDD
// object SimulateMessageRDD2 { 
//     import simulations.util.Simulate._

//   def apply(actors: List[Actor], edges: Map[Long, List[Long]], ttotalTurn: Int): Unit = {

//     var currentTurn: Int = 0
//     var t1: Long = 0
//     var t2: Long = 0

//     var totalTime: Long = 0
//     var elapsedRound: Int = 0
//     var time_seq: List[Long] = List()

//     var actorRDD: RDD[(Long, Actor)] = sc.parallelize(actors).map(i => (i.id, i))
//     var messageRDD: RDD[(Long, List[Any])] = actorRDD.mapValues(i => List())

//     while (currentTurn < totalTurn ) {
//         t1 = System.currentTimeMillis()

//         val newMessageRDD = actorRDD.leftOuterJoin(messageRDD)
//           .mapValues(x => {x._1.run(x._2.getOrElse(List()))})
//           .flatMap(x => x._2.map(m => (m._1, m._2)))
//           .reduceByKey((m1, m2) => m1 ::: m2).cache()
//         newMessageRDD.count()
        
//         messageRDD = newMessageRDD
//         // newMessageRDD.unpersist()
        
//         elapsedRound = actorRDD.map(x => x._2.proposeInterval).collect().min
//         actorRDD.localCheckpoint()
//         currentTurn += elapsedRound
//         t2 = System.currentTimeMillis()
//         time_seq = time_seq ::: List(t2-t1)
//         println(f"Iteration ${currentTurn} takes ${t2-t1} ms")
//     }

//     val average = time_seq.sum / totalTurn
//     println(f"Average time per round ${average}")
//     sc.stop()
//   }
// }