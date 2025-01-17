package org.apache.spark.sql.cstream

import java.io.Closeable

import scala.collection.JavaConverters._

import org.apache.avro.generic.GenericRecord
import org.apache.bookkeeper.api.stream.StreamConfig
import org.apache.bookkeeper.clients.impl.stream.event.EventPositionImpl
import org.apache.bookkeeper.clients.impl.stream.utils.PositionUtils
import org.apache.bookkeeper.common.concurrent.FutureUtils
import org.apache.bookkeeper.stream.protocol.RangeId
import org.apache.spark.internal.Logging
import org.apache.spark.sql.types.StructType

class MetadataReader(options: CStreamOptions) extends Closeable with Logging {

  import TPCH_TABLE_NAMES._
  import me.jinsui.shennong.bench.avro._

  lazy val stream = FutureUtils.result(
    CachedStorageClient.getOrCreate(ClientConfig(options.url, options.namespace))
      .openStream(
        options.streamName,
        StreamConfig.builder[Array[Byte], GenericRecord]().build()))

  def getSchema(): StructType = {
    getSchema(options.tbl)
  }

  // TODO: read schema from stream/schema registry later
  def getSchema(tbl: String): StructType = {
    val avroSchema = tbl match {
      case LINEITEM => Lineitem.getClassSchema
      case ORDERS => Orders.getClassSchema
      case PART => Part.getClassSchema
      case PARTSUPP => Partsupp.getClassSchema
      case NATION => Nation.getClassSchema
      case SUPPLIER => Supplier.getClassSchema
      case REGION => Region.getClassSchema
      case CUSTOMER => Customer.getClassSchema
      case _ => throw new NotImplementedError(s"not supported table: $tbl")
    }

    val dt = SchemaConverters.toSqlType(avroSchema).dataType
    assert(dt.isInstanceOf[StructType])
    dt.asInstanceOf[StructType]
  }

  def getStartPos(): Map[RangeId, EventPositionImpl] = {
    PositionUtils.toPositionImpls(stream.getHeadPosition.get()).asScala.toMap
  }

  def getEndPos(): Map[RangeId, EventPositionImpl] = {
    PositionUtils.toPositionImpls(stream.getTailPosition.get()).asScala.toMap
  }

  override def close(): Unit = {
    stream.close()
  }
}
