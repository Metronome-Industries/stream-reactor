/*
 * Copyright 2017 Datamountaineer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datamountaineer.streamreactor.connect.mongodb.sink

import com.datamountaineer.streamreactor.common.utils.{JarManifest, ProgressCounter}
import com.datamountaineer.streamreactor.connect.mongodb.config.{MongoConfig, MongoConfigConstants}
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.sink.{SinkRecord, SinkTask}

import java.util
import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.util.{Failure, Success, Try}

/**
  * <h1>MongoSinkTask</h1>
  *
  * Kafka Connect Mongo DB sink task. Called by
  * framework to put records to the target sink
  **/
class MongoSinkTask extends SinkTask with StrictLogging {
  private var writer: Option[MongoWriter] = None
  private val manifest = JarManifest(getClass.getProtectionDomain.getCodeSource.getLocation)

  private val progressCounter = new ProgressCounter
  private var enableProgress: Boolean = false

  logger.info("Task initialising")

  /**
    * Parse the configurations and setup the writer
    **/
  override def start(props: util.Map[String, String]): Unit = {

    val conf = if (context.configs().isEmpty) props else context.configs()

    logger.info(scala.io.Source.fromInputStream(getClass.getResourceAsStream("/mongo-ascii.txt")).mkString + s" $version")
    logger.info(manifest.printManifest())

    val taskConfig = Try(MongoConfig(conf)) match {
      case Failure(f) => throw new ConnectException("Couldn't start Mongo Sink due to configuration error.", f)
      case Success(s) => s
    }

    writer = Some(MongoWriter(taskConfig, context = context))
    enableProgress = taskConfig.getBoolean(MongoConfigConstants.PROGRESS_COUNTER_ENABLED)
  }

  /**
    * Pass the SinkRecords to the mongo db writer for storing them
    **/
  override def put(records: util.Collection[SinkRecord]): Unit = {
    require(writer.nonEmpty, "Writer is not set!")
    val seq = records.asScala.toVector
    writer.foreach(w => w.write(seq))

    if (enableProgress) {
      progressCounter.update(seq)
    }
  }

  override def stop(): Unit = {
    logger.info("Stopping Mongo Database sink.")
    writer.foreach(w => w.close())
    progressCounter.empty()
  }

  override def flush(map: util.Map[TopicPartition, OffsetAndMetadata]): Unit = {}

  override def version: String = manifest.version()
}
