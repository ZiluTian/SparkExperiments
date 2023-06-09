package exp.v2

import scala.io.Source
import scala.util.Random

class Cell(id: Long, cfreq: Int, cint: Int) extends Actor(id) {
    var aliveNeighbors: Int = 0
    var alive: Int = if (Random.nextBoolean()) 1 else 0

    override def run(messages: List[Any]): Actor = {
        aliveNeighbors = 0
        messages.foreach(m => {
            aliveNeighbors = aliveNeighbors + m.asInstanceOf[Int]
        })

        if ((alive==1) && (aliveNeighbors > 3 || aliveNeighbors < 2)) {
            alive = 0
        } else if ((alive==0) && (aliveNeighbors==3)) {
            alive = 1
        }

        proposeInterval = cint
        sendMessages = connectedAgentIds.map(i => {
            (i, Range(0, cfreq).map(_ => alive).toList)
        })
        this
    }
}

object GameOfLife {
    def main(args: Array[String]): Unit = {
        val edgeFilePath: String = args(0)
        val cfreq: Int = args(1).toInt
        val interval: Int = args(2).toInt
        val mode: Int = args(3).toInt

        val source = Source.fromFile(edgeFilePath)
        var vertices: Map[Long, Actor] = Map[Long, Actor]()
        for (line <- source.getLines()) {
            val fields = line.split(" ")
            val srcId: Long = fields(0).toLong
            val dstId: Long = fields(1).toLong
            if (vertices.get(srcId).isDefined){
                vertices(srcId).connectedAgentIds = dstId :: vertices(srcId).connectedAgentIds
            } else {
                vertices = vertices + (srcId -> new Cell(srcId, cfreq, interval))
            }
        }
        source.close()

        mode match {
            case 1 => SimulateMessageRDD(vertices.values.toList, 200)
            case 2 => SimulateMessageRDD2(vertices.values.toList, 200)
            case 3 => SimulatePeriodicCheckpoint(vertices.values.toList, 200)
        }
    }
}