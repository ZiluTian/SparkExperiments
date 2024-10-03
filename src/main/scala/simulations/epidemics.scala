package simulations

import scala.util.Random
import scala.io.Source
import scala.math.{max, abs}
import breeze.stats.distributions.Gamma

object Epidemics {
    def main(args: Array[String]): Unit = {
        val totalAgents: Int = args(0).toInt

        val graph = if (totalAgents < 0) {
            util.GraphFactory.stochasticBlock(abs(totalAgents), 0.01, 0, 5)
        } else {
            util.GraphFactory.erdosRenyi(totalAgents, 0.01)
        }

        val vertices: List[(Long, List[Int])] = graph.adjacencyList().map(i => {
            val age: Int = Random.nextInt(90) + 10
            val symptomatic: Int = if (Random.nextBoolean) 1 else 0
            val health: Int = if (Random.nextInt(100)==0) 2 else 0
            val vulnerability: Int = if (age > 60) 1 else 0
            val daysInfected: Int = 0
            (i._1, List(age, symptomatic, health, vulnerability, daysInfected))
        }).toList

        // define the maximum number of iterations
        val maxIterations = 50

        // Encodings (health states)
        val Susceptible: Int = 0
        val Exposed: Int = 1
        val Infectious: Int = 2
        val Hospitalized: Int = 3
        val Recover: Int = 4
        val Deceased: Int = 5
        // Encodings (vulnerability levels)
        val Low: Int = 0
        val High: Int = 1
        // Infectious parameter (gamma distribution)
        val infectiousAlpha = 0.25
        val infectiousBeta = 1
        val symptomaticSkew = 2

        def change(health: Int, vulnerability: Int): Int = {
            health match {
                case Susceptible => Exposed
                case Exposed => 
                    val worse_prob: Double = eval(vulnerability, Exposed, Infectious)
                    if (Random.nextDouble < worse_prob) {
                        Infectious
                    } else {
                        Recover
                    }
                case Infectious => 
                    val worse_prob: Double = eval(vulnerability, Infectious, Hospitalized)
                    if (Random.nextDouble < worse_prob) {
                        Hospitalized
                    } else {
                        Recover
                    }
                case Hospitalized =>
                    val worse_prob: Double = eval(vulnerability, Hospitalized, Deceased)
                    if (Random.nextDouble < worse_prob) {
                        Deceased
                    } else {
                        Recover
                    }
                case _ => health
            }
        }

        def stateDuration(health: Int): Int = {
            val randDuration: Int = (3*Random.nextGaussian()).toInt

            health match {
                case Infectious => max(2, randDuration+6) 
                case Hospitalized => max(2, randDuration+7) 
                case Exposed => max(3, randDuration+5)
            }
        }

        def infectiousness(health: Int, symptomatic: Boolean): Double = {
            if (health == Infectious) {
                var gd = Gamma(infectiousAlpha, infectiousBeta).draw()
                if (symptomatic){
                    gd = gd * 2
                }
                gd
            } else {
                0
            }
        }

        def eval(vulnerability: Int, currentHealth: Int, projectedHealth: Int): Double = {
            vulnerability match {
                case Low =>
                    (currentHealth, projectedHealth) match {
                        case (Exposed, Infectious) => 0.6
                        case (Infectious, Hospitalized) => 0.1
                        case (Hospitalized, Deceased) => 0.1
                        case _ => 0.01
                    }
                case High =>
                    (currentHealth, projectedHealth) match {
                        case (Exposed, Infectious) => 0.9
                        case (Infectious, Hospitalized) => 0.4
                        case (Hospitalized, Deceased) => 0.5
                        case _ => 0.05
                    }
            }
        }
        
        def run(id: Long, state: List[Int], messages: List[Double]): List[Int] = {
            val age: Int = state(0)
            val symptomatic: Int = state(1)
            var health: Int = state(2)
            val vulnerability: Int = state(3)
            var daysInfected: Int = state(4)

            if (id != 0) { // people
                if (health != Deceased) {
                    if ((health != Susceptible) && (health != Recover)) {
                        if (daysInfected == stateDuration(health)) {
                            // health = 4
                            health = change(health, vulnerability)
                            daysInfected = 0
                        } else {
                            daysInfected = daysInfected + 1
                        }
                    }

                    messages.foreach(m => {
                        if (health==0) {
                            var risk: Double = m
                            if (age > 60) {
                                risk = risk * 2
                            } 
                            if (risk > 1) {
                                health = change(health, vulnerability)
                            }
                        }
                    })
                }
            } 
            
            List(age, symptomatic, health, vulnerability, daysInfected)
        }

        def sendMessage(id: Long, state: List[Int]): Double = {
            val age: Int = state(0)
            val symptomatic: Int = state(1)
            var health: Int = state(2)
            val vulnerability: Int = state(3)
            var daysInfected: Int = state(4)
            var idleCountDown: Int = state(5)

            // Calculate infectiousness once
            if (symptomatic == 1){
                infectiousness(health.toInt, true)        
            } else {
                infectiousness(health.toInt, false)
            }
        }

        Simulate[List[Int], Double](vertices, run, sendMessage, graph.adjacencyList().mapValues(_.toList), List(), maxIterations)
    }
}