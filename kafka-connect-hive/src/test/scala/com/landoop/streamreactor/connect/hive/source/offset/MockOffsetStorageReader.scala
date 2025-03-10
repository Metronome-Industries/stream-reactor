package com.landoop.streamreactor.connect.hive.source.offset

import com.landoop.streamreactor.connect.hive.source.SourcePartition.{fromSourceOffset, toSourcePartition}

import java.util
import com.landoop.streamreactor.connect.hive.source.{SourceOffset, SourcePartition}
import org.apache.kafka.connect.storage.OffsetStorageReader

import scala.jdk.CollectionConverters.{MapHasAsJava, MapHasAsScala}


class MockOffsetStorageReader(map: Map[SourcePartition, SourceOffset]) extends OffsetStorageReader {

  override def offsets[T](partitions: util.Collection[util.Map[String, T]]): util.Map[util.Map[String, T], util.Map[String, AnyRef]] = ???

  override def offset[T](partition: util.Map[String, T]): util.Map[String, AnyRef] = {
    val sourcePartition = toSourcePartition(partition.asScala.toMap.view.mapValues(_.toString).toMap)
    map.get(sourcePartition).map(fromSourceOffset).map(_.asJava).orNull
  }
}
