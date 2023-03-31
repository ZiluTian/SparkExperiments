package exp.v1

import scala.collection.mutable.{Buffer, ListBuffer, Map => MutMap}
import java.util.concurrent.ConcurrentLinkedQueue

/**
  * This class represents the main class of the generated classes
  * It contains the logic for message handling and defines the
  * functions for a step-wise simulation
  */
class Actor(var id: Long) {

  var proposeInterval: Int = 1
  /**
    * Contains the messages, which should be sent to other actors in the next step
    */
    // Replace mutable with immutable ds, for Spark serialization
  var sendMessages: MutMap[Long, Buffer[Message]] = MutMap[Long, Buffer[Message]]()
  // var sendMessages: Map[Long, List[Message]] = Map[Long, List[Message]]()

  var connectedAgentIds: List[Long] = List()

  final def sendMessage(receiver: Long, message: Message): Unit = {
    // sendMessages = sendMessages + (receiver -> (message :: sendMessages.getOrElse(receiver, List[Message]()))) 
    sendMessages.getOrElseUpdate(receiver, new ListBuffer[Message]()).append(message)
  }

  /**
    * Stub, gets overriden by generated code
    * messages: a list of input messages
    * return: (a list of output messages, passed rounds)
    */
  def run(messages: List[Message]): Actor = {
    ???
  }
}