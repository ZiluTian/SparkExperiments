package util

import org.apache.spark.{SparkConf, SparkContext}

object Run {    
    @transient lazy val conf: SparkConf = new SparkConf()
      .setAppName("SparkExperiments")
      .set("spark.driver.maxResultSize", "10g")
      .set("spark.hadoop.dfs.replication", "1")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      // .set("spark.driver.allowMultipleContexts", "true")

    @transient lazy val sc: SparkContext = new SparkContext(conf)
    val deployOption = Option(System.getProperty("sparkDeploy")).getOrElse("local")
    if (deployOption == "local") {
      conf.setMaster("local[*]")
    }

    sc.setLogLevel("ERROR")
    sc.setCheckpointDir("checkpoint/")
}