package simulations

import scala.util.Random
import scala.io.Source

object GameOfLife {
    def main(args: Array[String]): Unit = {
        val totalAgents: Int = args(0).toInt
        val width: Int = 100
        val graph = util.GraphFactory.torus2D(width, (totalAgents / width).toInt)

        val vertices: List[(Long, Boolean)] = graph.nodes.map(i => (i, Random.nextBoolean())).toList

        def run(id: Long, alive: Boolean, messages: List[Boolean]): Boolean = {
            // println(f"Received ${messages.flatten.size} messages!")
            val  aliveNeighbors = messages.filter(_==true).size
            if (alive && (aliveNeighbors > 3 || aliveNeighbors < 2)) {
                true
            } else if ((!alive) && (aliveNeighbors==3)) {
                false
            } else {
                alive
            }
        }

        def sendMessage(id: Long, alive: Boolean): Boolean = {
            alive
        }

        Simulate[Boolean, Boolean](vertices, run, sendMessage, graph.adjacencyList().mapValues(_.toList), List(), 200)
    }
}