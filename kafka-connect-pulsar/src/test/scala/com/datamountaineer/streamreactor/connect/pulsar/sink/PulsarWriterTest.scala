package com.datamountaineer.streamreactor.connect.pulsar.sink

import com.datamountaineer.streamreactor.connect.pulsar.config.{PulsarConfigConstants, PulsarSinkConfig, PulsarSinkSettings}
import org.apache.kafka.connect.data.{Schema, SchemaBuilder, Struct}
import org.apache.kafka.connect.sink.SinkRecord
import org.apache.pulsar.client.api.{Producer, ProducerBuilder, PulsarClient}
import org.mockito.{Answers, MockitoSugar}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.jdk.CollectionConverters.MapHasAsJava


/**
  * Created by andrew@datamountaineer.com on 23/01/2018. 
  * stream-reactor
  */
class PulsarWriterTest extends AnyWordSpec with MockitoSugar with Matchers {
  val pulsarTopic = "persistent://landoop/standalone/connect/kafka-topic"

  def getSchema: Schema = {
    SchemaBuilder.struct
      .field("int8", SchemaBuilder.int8().defaultValue(2.toByte).doc("int8 field").build())
      .field("int16", Schema.INT16_SCHEMA)
      .field("int32", Schema.INT32_SCHEMA)
      .field("int64", Schema.INT64_SCHEMA)
      .field("float32", Schema.FLOAT32_SCHEMA)
      .field("float64", Schema.FLOAT64_SCHEMA)
      .field("boolean", Schema.BOOLEAN_SCHEMA)
      .field("string", Schema.STRING_SCHEMA)
      .build()
  }


  def getStruct(schema: Schema): Struct = {
    new Struct(schema)
      .put("int8", 12.toByte)
      .put("int16", 12.toShort)
      .put("int32", 12)
      .put("int64", 12L)
      .put("float32", 12.2f)
      .put("float64", 12.2)
      .put("boolean", true)
      .put("string", "foo")
  }

  "should write messages" in {

    val config = PulsarSinkConfig(Map(
      PulsarConfigConstants.HOSTS_CONFIG -> "pulsar://localhost:6650",
      PulsarConfigConstants.KCQL_CONFIG -> s"INSERT INTO $pulsarTopic SELECT * FROM kafka_topic BATCH = 10 WITHPARTITIONER = SinglePartition WITHCOMPRESSION = ZLIB WITHDELAY = 1000"
    ).asJava)

    val schema = getSchema
    val struct = getStruct(schema)
    val record1 = new SinkRecord("kafka_topic", 0, null, null, schema, struct, 1)

    val settings = PulsarSinkSettings(config)

    val client = mock[PulsarClient]

    val producer = mock[Producer[Array[Byte]]](Answers.RETURNS_DEEP_STUBS)

    val producerBuilder = mock[ProducerBuilder[Array[Byte]]]
    when(producerBuilder.create()).thenReturn(producer)

    val writer = new PulsarWriter(() => client.close(), () => Map("persistent://landoop/standalone/connect/kafka-topic" -> producerBuilder), settings)
    writer.write(List(record1))
  }
}
