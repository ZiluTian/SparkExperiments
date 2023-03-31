package util

import org.apache.spark.{SparkConf, SparkContext}

trait SparkTest {    
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

    @transient lazy val sc: SparkContext = new SparkContext(conf)
    sc.setLogLevel("ERROR")
    sc.setCheckpointDir("checkpoint/")
}