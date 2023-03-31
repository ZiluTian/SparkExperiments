package simulations

import scala.util.Random
import scala.io.Source

object GameOfLife {
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

        val vertices: List[(Long, Boolean)] = edges.map(i => (i._1, Random.nextBoolean())).toList

        def run(id: Long, alive: Boolean, messages: List[List[Boolean]]): Boolean = {
            // println(f"Received ${messages.flatten.size} messages!")
            val  aliveNeighbors = messages.flatten.filter(_==true).size
            if (alive && (aliveNeighbors > 3*cfreq || aliveNeighbors < 2*cfreq)) {
                true
            } else if ((!alive) && (aliveNeighbors==3)) {
                false
            } else {
                alive
            }
        }

        def sendMessage(id: Long, alive: Boolean): List[Boolean] = {
            Range(0, cfreq).map(_ => alive).toList
        }

        Simulate[Boolean, List[Boolean]](vertices, run, sendMessage, edges, List(), 200)
    }
}