package org.apache.spark.sql.cstream

import java.util.{Locale, Properties}

import org.apache.spark.sql.catalyst.util.CaseInsensitiveMap

class CStreamOptions(@transient val parameters: CaseInsensitiveMap[String]) extends Serializable {

  import CStreamOptions._

  def this(parameters: Map[String, String]) = this(CaseInsensitiveMap(parameters))

  /**
    * Returns a property with all options.
    */
  val asProperties: Properties = {
    val properties = new Properties()
    parameters.originalMap.foreach { case (k, v) => properties.setProperty(k, v) }
    properties
  }

  val url = parameters(CS_URL)
  val tbl = parameters(CS_TBL)
  val streamName = parameters(CS_STREAM)
  val parallelism = parameters(CS_PARALLELISM).toInt
  val namespace = parameters(CS_NAMESPACE)
  val readAheadCacheSize = parameters(CS_READAHEADCACHESIZE).toLong
  val pollTimeoutMs = parameters(CS_POLLTIMEOUTMS).toLong
}


object CStreamOptions {

  private val csOptionNames = collection.mutable.Set[String]()

  private def newOption(name: String): String = {
    csOptionNames += name.toLowerCase(Locale.ROOT)
    name
  }

  val CS_URL = newOption("url")
  val CS_TBL  = newOption("tableName")
  val CS_STREAM = newOption("streamName")
  val CS_PARALLELISM = newOption("parallelism")
  val CS_NAMESPACE = newOption("namespace")
  val CS_READAHEADCACHESIZE = newOption("readAheadCacheSize")
  val CS_POLLTIMEOUTMS = newOption("pollTimeoutMs")

}

object TPCH_TABLE_NAMES {
  val CUSTOMER = "customer"
  val LINEITEM = "lineitem"
  val NATION = "nation"
  val ORDERS = "orders"
  val PART = "part"
  val PARTSUPP = "partsupp"
  val REGION = "region"
  val SUPPLIER = "supplier"
}
