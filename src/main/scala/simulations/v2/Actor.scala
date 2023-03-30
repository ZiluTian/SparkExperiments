package simulations.v2

/**
  * This class represents the main class of the generated classes
  * It contains the logic for message handling and defines the
  * functions for a step-wise simulation
  */
class Actor(var id: Long) {

  var proposeInterval: Int = 1

  var connectedAgentIds: List[Long] = List()

  var sendMessages: List[(Long, List[Any])] = List()
  /**
    * Stub, gets overriden by generated code
    * messages: a list of input messages
    * return: (a list of output messages, passed rounds)
    */
  def run(messages: List[Any]): Actor = {
    ???
  }
}