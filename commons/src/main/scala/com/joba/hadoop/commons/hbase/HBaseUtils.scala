package com.joba.hadoop.commons.hbase

import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Table}


object HBaseUtils {

  def getConnection: Connection = {
    ConnectionFactory.createConnection()
  }

  def getTable(connection: Connection, tableName: String): Table = {
    connection.getTable(TableName.valueOf(tableName))
  }
}
