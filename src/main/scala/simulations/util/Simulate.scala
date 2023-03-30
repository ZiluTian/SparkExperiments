package simulations.util

import org.apache.spark.{SparkConf, SparkContext}

object Simulate {    
    val deployOption = Option(System.getProperty("sparkDeploy")).getOrElse("local")
    
    @transient lazy val conf: SparkConf = new SparkConf()
      .setAppName("SparkExperiments")
      .set("spark.driver.maxResultSize", "10g")
      .set("spark.hadoop.dfs.replication", "1")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      // .set("spark.driver.allowMultipleContexts", "true")
    
    if (deployOption == "local") {
      conf.setMaster("local[*]")
    } 

    conf.registerKryoClasses(Array(classOf[simulations.v1.Message], 
      classOf[simulations.v1.Actor], classOf[simulations.v2.Actor]))

    @transient lazy val sc: SparkContext = new SparkContext(conf)
    sc.setLogLevel("ERROR")
    sc.setCheckpointDir("checkpoint/")
}