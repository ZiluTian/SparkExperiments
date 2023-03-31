package util

import scala.io.Source

object GraphLoader {
    def build(edgeFilePath: String): Map[Long, List[Long]] = {
        val source = Source.fromFile(edgeFilePath)
        var edges: Map[Long, List[Long]] = Map[Long, List[Long]]()

        for (line <- source.getLines()) {
            val fields = line.split(" ")
            val srcId: Long = fields(0).toLong
            val dstId: Long = fields(1).toLong
            edges = edges + (srcId -> (dstId :: edges.getOrElse(srcId, List())))
        }
        source.close()
        edges
    }
}