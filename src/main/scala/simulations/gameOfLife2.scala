package simulations

import scala.util.Random
import scala.io.Source

object GameOfLife2 {
    def main(args: Array[String]): Unit = {
        val edgeFilePath: String = args(0)
        val cfreq: Int = args(1).toInt
        val interval: Int = args(2).toInt

        val source = Source.fromFile(edgeFilePath)
        var edges: Map[Long, List[Long]] = Map[Long, List[Long]]()

        for (line <- source.getLines()) {
            val fields = line.split(" ")
            val srcId: Long = fields(0).toLong
            val dstId: Long = fields(1).toLong
            edges = edges + (srcId -> (dstId :: edges.getOrElse(srcId, List())))
        }
        source.close()

        val vertices: List[(Long, List[Int])] = edges.map(i => (i._1, {
          if (Random.nextBoolean) {
            List(1, interval)
          } else {
            List(0, interval)
          }
        })).toList

        def run(id: Long, state: List[Int], messages: List[List[Int]]): List[Int] = {
          var alive: Int = state(0)
          var idleCountDown: Int = state(1)

          if (idleCountDown > 1) {
            idleCountDown -= 1
          } else {
            // println("Total received messages " + receivedMsgs.size)
            if (messages.size == 8) {
              val aliveNeighbors = messages.filter(i => i == 1).size
              if ((alive==1) && ((aliveNeighbors > 3*cfreq) || (aliveNeighbors < 2*cfreq))) {
                  alive = 0
              } else if ((alive==0) && (aliveNeighbors < 3*cfreq) && (aliveNeighbors > 2*cfreq)){
                  alive = 1
              } 
            } 
            idleCountDown = interval
          }
          List(alive, idleCountDown)
        }

        def sendMessage(id: Long, state: List[Int]): List[Int] = {
          val alive: Int = state(0)
          val idleCountDown: Int = state(1)
          
          if (idleCountDown <= 1) {
            Range(0, cfreq).map(_ => alive).toList
          } else {
            List()
          }
        }

        Simulate[List[Int], List[Int]](vertices, run, sendMessage, edges, List(), 200)
    }
}