package com.joba.hadoop.commons.hbase

import java.util
import com.joba.hadoop.commons.logging.Loggable
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.filter.FuzzyRowFilter
import org.apache.hadoop.hbase.util.{Bytes, Pair}
import org.apache.hadoop.hbase.{HBaseConfiguration, KeyValue, TableName}
import scala.collection.JavaConversions._


object HBaseUtil extends Loggable with Serializable{

  def arm[T, Q](c: T {def close(): Unit})(f: (T) => Q): Q = {
    try {
      f(c)
    } finally {
      c.close()
    }
  }

  def getConnection: Connection = {
    val conf = HBaseConfiguration.create()
    INFO(s"Connecting to HBase with quorum '${conf.get("hbase.zookeeper.quorum")}' and port ${conf.get("hbase.zookeeper.property.clientPort")}")
    ConnectionFactory.createConnection(conf)
  }

  def getTable(connection: Connection, tableName: String): Table = {
    connection.getTable(TableName.valueOf(tableName))
  }

  def putToHBase(key: String, tableName: String)(addData:(Put)=>Put) = {
    arm(getConnection) { connection =>
      arm(getTable(connection, tableName)) { table =>
        val put = new Put(Bytes.toBytes(key), System.currentTimeMillis())
        table.put(addData(put))
      }
    }
  }

  def putsToHbase(puts: Seq[Put], tableName: String): Unit = {
    arm(getConnection) { connection =>
      arm(getTable(connection, tableName)) { table =>
        table.put(puts)
      }
    }
  }

  def truncateTable(tableName: String): Unit = {
    arm(getConnection) { connection =>
      val admin = connection.getAdmin
      try {
        admin.disableTable(TableName.valueOf(tableName))
        admin.truncateTable(TableName.valueOf(tableName), false)
        admin.enableTableAsync(TableName.valueOf(tableName))
      }
      catch {
        case e: Exception => ERROR(e)(s"Failed to truncate Hbase table $tableName")
      }
    }
  }

  def getFromHbase[T](rowKey: String, tableName: String)(implicit fReader: (String, Result) => T): T = {
    arm(getConnection) { connection =>
      arm(getTable(connection, tableName)) { table =>
        fReader(rowKey: String, table.get(new Get(rowKey.getBytes())))
      }
    }
  }

  def fuzzyScan[T](colFamily: String, keys: util.LinkedList[Pair[Array[Byte], Array[Byte]]], tableName: String)
                  (implicit fReader: ResultScanner => T): T = {
    arm(getConnection) { connection =>
      arm(getTable(connection, tableName)) { table =>
        val filter: FuzzyRowFilter = new FuzzyRowFilter(keys)
        val scan = new Scan().setFilter(filter)

        fReader(table.getScanner(scan))
      }
    }
  }
}
