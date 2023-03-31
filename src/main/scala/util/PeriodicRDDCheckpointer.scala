package util

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.rdd.RDD


class PeriodicRDDCheckpointer[T](
    checkpointInterval: Int,
    sc: SparkContext,
    storageLevel: StorageLevel)
  extends PeriodicCheckpointer[RDD[T]](checkpointInterval, sc) {
  require(storageLevel != StorageLevel.NONE)

  def this(checkpointInterval: Int, sc: SparkContext) =
    this(checkpointInterval, sc, StorageLevel.MEMORY_ONLY)

  //zt Overwrite default with local checkpoint 
  // default: data.checkpoint()
  override protected def checkpoint(data: RDD[T]): Unit = data.checkpoint()

  override protected def isCheckpointed(data: RDD[T]): Boolean = data.isCheckpointed

  override protected def persist(data: RDD[T]): Unit = {
    // data.cache()
    if (data.getStorageLevel == StorageLevel.NONE) {
      data.persist(storageLevel)
    }
  }

  override protected def unpersist(data: RDD[T]): Unit = data.unpersist()

  override protected def getCheckpointFiles(data: RDD[T]): Iterable[String] = {
    data.getCheckpointFile.map(x => x)
  }
}
