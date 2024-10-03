package simulations

import scala.util.Random
import scala.io.Source

object StockMarket {
    def main(args: Array[String]): Unit = {
        val totalAgents: Int = args(0).toInt
        
        val graph = util.GraphFactory.bipartite(1, totalAgents - 1)

        val vertices: List[(Long, List[List[Double]])] = graph.adjacencyList().map(i => {
            val stock_timeseries: List[Double] = List(100.0)
            val lastDividend: Double = 0
            val lastAvg: Double = 100.0
            val currentPrice: Double = 100.0
            val dividendIncrease: Double = 0
            val recent10AvgInc: Double = 100.0
            val recent50AvgInc: Double = 95.0  
            val recent100AvgInc: Double = 107.1  
            val marketState = List(lastDividend, lastAvg, currentPrice, dividendIncrease, recent100AvgInc, recent50AvgInc, recent100AvgInc)

            val timer = List(0.0)   // for calculating past average
            
            val cash: Double = 1000.0
            val shares: Double = 1
            val estimatedWealth: Double = 1100.0
            val traderState = List(cash, shares, estimatedWealth)

            val rules = List(0, 0, 0, 0, 0.0, Random.nextInt(5), 0)  

            (i._1, List(stock_timeseries, marketState, timer, traderState, rules))
        }).toList

        // define the maximum number of iterations
        val maxIterations = 200
        val priceAdjustmentFactor: Double = 0.01
        val interestRate: Double = 0.0001

        def update(window: Double, timer: Double, lastAvg: Double, stock_timeseries: List[Double]): Int = {
            // moving window    
            var calculated_avg: Double = -1
            var sumPrice: Double = 0

            if (timer > window) {
                var i: Int = (timer-window).toInt
                while(i<timer){
                    sumPrice = stock_timeseries(i) + sumPrice  
                    i += 1
                }
                calculated_avg = sumPrice/window;
            } else {
                var i = 0
                while (i<timer){
                    sumPrice += stock_timeseries(i)
                    i += 1  
                }
                calculated_avg = sumPrice/timer;
            }

            if (lastAvg < calculated_avg){
                1
            } else {
                0
            }
        }

        // return (action, cash, shares)
        def evalRule(rule: Int, stockPrice: Double, marketState: List[Double], cash: Double, shares: Double): (Int, Double, Double) = {
            // assert(marketState.size == 7)
            val lastDividend = marketState(0)
            val lastAvg = marketState(1)
            val currentPrice = marketState(2)
            val dividendIncrease = marketState(3)
            val recent10AvgInc = marketState(4)
            val recent50AvgInc = marketState(5)   
            val recent100AvgInc = marketState(6)    

            var action = 0
            val buy = 1
            val sell = 2

            rule match {
                case 1 => 
                    if (dividendIncrease == 1 && stockPrice < cash) {
                        action = buy
                    } else if (dividendIncrease == 2 && shares > 1) {
                        action = sell
                    } 
                case 2 =>
                    if (recent10AvgInc == 1 && shares >= 1){
                        action = sell
                    } else if (stockPrice < cash && recent10AvgInc == 2){
                        action = buy
                    } 
                case 3 =>
                    if (recent50AvgInc == 1 && shares >= 1){
                        action = sell
                    } else if (stockPrice < cash && recent50AvgInc == 2){
                        action = buy
                    } 
                case 4 =>
                    if (recent100AvgInc == 1 && shares >= 1){
                        action = sell
                    } else if (stockPrice < cash && recent100AvgInc == 2){
                        action = buy
                    } 
                case _ => 
                    if (Random.nextBoolean){
                        if (stockPrice < cash) {
                            action = buy
                        } 
                    } else {
                        if (shares > 1) {
                            action = sell
                        }
                    }
            }
            if (action == buy) {
                (buy, cash - stockPrice, shares + 1)
            } else if (action == sell) {
                (sell, cash + stockPrice, shares - 1)
            } else {
                (0, cash, shares)
            }
        }
        
        def run(id: Long, state: List[List[Double]], messages: List[List[Double]]): List[List[Double]] = {
            var stock_timeseries: List[Double] = state(0)
            var marketState: List[Double] = state(1)
            // assert(marketState.size == 7)
            var lastDividend: Double = marketState(0)
            var lastAvg: Double = marketState(1)
            var currentPrice: Double = marketState(2)
            var dividendIncrease: Double = marketState(3)
            var recent10AvgInc: Double = marketState(4)
            var recent50AvgInc: Double = marketState(5)    
            var recent100AvgInc: Double = marketState(6)       
            var timer: Double = state(2).head
            var traderState: List[Double] = state(3)
            // assert(traderState.size == 3)
            var cash: Double = traderState(0)
            var shares: Double = traderState(1)
            var estimatedWealth: Double = traderState(2)

            val rules: Array[Double] = state(4).toArray
            var lastRule: Int = rules(5).toInt
            var nextAction: Int = rules(6).toInt

            // assert(rules.size == 7)

            timer += 1 
            if (id != 0) {  // trader 
                cash = cash * (1 + interestRate)
                messages.foreach(ms => {
                    val m_dividendPerShare = ms(0)
                    val m_lastAvg = ms(1)
                    val m_currentPrice = ms(2)
                    val m_dividendIncrease = ms(3)
                    val m_recent10AvgInc = ms(4)
                    val m_recent50AvgInc = ms(5)    
                    val m_recent100AvgInc = ms(6)    
                    val previousWealth = estimatedWealth
                    // Update the number of shares based on the new dividend
                    shares = shares * (1 + m_dividendPerShare)
                    // Calculate the new estimated wealth 
                    estimatedWealth = cash + shares * m_currentPrice
                    // Update the strength of prev action based on the feedback of the wealth changes
                    if (estimatedWealth > previousWealth) {
                        rules(lastRule) += 1
                    } else if (estimatedWealth < previousWealth) {
                        rules(lastRule) -= 1
                    }
                    // Select the rule with the highest strength for the next action 
                    val nextRule = rules.zipWithIndex.sortBy(x => x._1).head._2
                    // Obtain the action based on the rule 
                    val x = evalRule(nextRule, m_currentPrice, ms, cash, shares)
                    // Update lastRule with the recently selected rule 
                    rules(5) = nextRule
                    // Update the last action, cash, and shares
                    rules(6) = x._1
                    cash = x._2
                    shares = x._3 
                })
            } else {    // market
                var buyOrders: Int = 0
                var sellOrders: Int = 0

                messages.foreach(ms => {
                    if (ms(0)==1) {
                        buyOrders = buyOrders + 1
                    } else if (ms(0)==2) {
                        sellOrders = sellOrders + 1
                    }
                })
                // Update price based on buy-sell orders
                
                currentPrice = currentPrice*(1+priceAdjustmentFactor*(buyOrders - sellOrders))
                // Update the stock time series with the new price
                stock_timeseries = stock_timeseries :+ currentPrice
                // Increment the timer 
                // Calculate the average of the stock price
                lastAvg = stock_timeseries.reduce((a, b) => a + b) / timer 
                // Calculate new dividend
                var newDividendPerShare = 0.1* Random.nextGaussian()
                if (newDividendPerShare < 0) {
                    newDividendPerShare = 0
                }
                // Calculate whether dividend has increased. 0: None, 1: true, 2: false
                var dividendIncrease = 1.0
                if (newDividendPerShare == 0) {
                    dividendIncrease = 0
                } else if (lastDividend > newDividendPerShare) {
                    dividendIncrease = 2
                }
                // Calculate whether avg has increased for past 10 rounds
                recent10AvgInc = update(10, timer, lastAvg, stock_timeseries)
                // Calculate whether avg has increased for past 50 rounds
                recent50AvgInc = update(50, timer, lastAvg, stock_timeseries)
                // Calculate whether avg has increased for past 100 rounds
                recent100AvgInc = update(100, timer, lastAvg, stock_timeseries)
                // println("Current price is " + currentPrice)
            }
            List(stock_timeseries, List(lastDividend, lastAvg, currentPrice, dividendIncrease, recent10AvgInc, recent50AvgInc, recent100AvgInc), List(timer), List(cash, shares, estimatedWealth), rules.toList)
        }

        def sendMessage(id: Long, state: List[List[Double]]): List[Double] = {
            val marketState: List[Double] = state(1)
            // assert(marketState.size == 7)
            var lastDividend: Double = marketState(0)
            var lastAvg: Double = marketState(1)
            var currentPrice: Double = marketState(2)
            var dividendIncrease: Double = marketState(3)
            var recent10AvgInc: Double = marketState(4)
            var recent50AvgInc: Double = marketState(5)    
            var recent100AvgInc: Double = marketState(6)                   
            val rules: List[Double] = state(4)

            if (id == 0) {
                    List(lastDividend, lastAvg, currentPrice, dividendIncrease, recent10AvgInc, recent50AvgInc, recent100AvgInc)
            } else {
                List(rules(6))
            }
        }

        Simulate[List[List[Double]], List[Double]](vertices, run, sendMessage, graph.adjacencyList().mapValues(_.toList), List(), maxIterations)
    }
}