package com.joba.hadoop.commons.hbase

import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes

import scala.collection.JavaConversions._
import scala.collection.mutable


object HBaseDAO {

  val config = HBaseConfiguration.create()
  val connection: Connection = ConnectionFactory.createConnection(config)
  val admin: Admin = connection.getAdmin()


  def getTable(tableName:String) =
    connection.getTable(TableName.valueOf(tableName))

  /**
   * Delete HBase Table if exists
   *
   * @param tableName Name of the table to delete
   */
  def deleteTable(tableName: String) {
    val tName = TableName.valueOf(tableName)
    if (admin.tableExists(tName)) {
      admin.disableTable(tName)
      admin.deleteTable(tName)
    }
  }

  /**
   * Create a new HBase Table
   *
   * @param tableName  Name of the table to create
   * @param families Name of the column families
   */
  def createTable(tableName: String, families: Array[String]): Unit = {
    val tName = TableName.valueOf(tableName)
    if (admin.tableExists(tName)) {
      println("This table already exists!")
    } else {
      val tableDesc = new HTableDescriptor(tName)
      for (i <- 0 to families.length - 1) {
        tableDesc.addFamily(new HColumnDescriptor(families(i)))
      }
      admin.createTable(tableDesc)
      println("Table " + tableName + " created with success")
    }
  }

  def checkRowKey(tableName: String,rowKey: String, list: List[Get]): Unit = {
    val table = connection.getTable(TableName.valueOf(tableName))
    val put = new Put(Bytes.toBytes(rowKey))
    val kv = new KeyValue(Bytes.toBytes(rowKey))
    put.add(kv)
    table.put(put)
  }

  def existRowKey(tableName: String, list: List[Get]): Unit = {
    val table = connection.getTable(TableName.valueOf(tableName))
    val exists = table.existsAll(list)
    for (i <- 0 to (exists.length - 1)) {
      print(exists(i))
    }
  }

  def getRowKey(tableName: String, rowKey: String): Boolean = {
    val table = connection.getTable(TableName.valueOf(tableName))
    val get = new Get(Bytes.toBytes(rowKey))
    if(table.exists(get)) true
    else false
  }

  /**
   * Add new Record to HBase table
   *
   * @param tableName Name of the table
   * @param rowKey    Row Key to add new record
   * @param column    Column Family
   * @param qualifier
   * @param value     Value to add
   */
  def addOrUpdateRecord(tableName: String, rowKey: String, column: String, qualifier: String, value: String): Unit = {
    val table = connection.getTable(TableName.valueOf(tableName))
    val put = new Put(Bytes.toBytes(rowKey))
    val kv = new KeyValue(Bytes.toBytes(rowKey), Bytes.toBytes(column), Bytes.toBytes(qualifier), System.currentTimeMillis(), Bytes.toBytes(value))
    put.add(kv)
    table.put(put)
  }

  def addOrUpdateRecord2(tableName: String, list: List[Put]): Unit = {
    val table = connection.getTable(TableName.valueOf(tableName))
    table.put(list)
  }

  /**
   * Delete a Record from HBase table
   *
   * @param tableName
   * @param rowKey
   */
  def delRecord(tableName: String, rowKey: String): Unit = {
    val table = connection.getTable(TableName.valueOf(tableName))
    val del = new Delete(rowKey.getBytes())
    table.delete(del)
    println("Record Deleted with success")
  }

  /**
   * Delete a Record by Qualifier from HBase table
   *
   * @param tableName
   * @param rowKey
   * @param family
   * @param qualifier
   */
  def delRecordQualifier(tableName: String, rowKey: String, family: String, qualifier: String): Unit = {
    val table = connection.getTable(TableName.valueOf(tableName))
    val del = new Delete(rowKey.getBytes())
    del.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier))
    table.delete(del)
  }


  /**
   * Get all the data from a record
   */
  def getOneRecord(tableName: String, rowKey: String, family: String): mutable.Map[String,String] = {
    val table = connection.getTable(TableName.valueOf(tableName))
    val get = new Get(rowKey.getBytes())
    val rs: Result = table.get(get)
    val map = collection.mutable.Map[String, String]()
    if(rs.getExists != null) {
      for (kv: Cell <- rs.listCells()) {
        if (new String(CellUtil.cloneFamily(kv)) == family) {
          map += (new String(CellUtil.cloneQualifier(kv))) -> (new String(CellUtil.cloneValue(kv)))
        }
      }
      map
    } else {
      null
    }
  }

  /**
   * Get one record value by rowKey, Column and Qualifier
   *
   * @param tableName
   * @param rowKey
   * @param column
   * @param qualifier
   * @return new_value
   */
  def getRecordValue(tableName: String, rowKey: String, column: String, qualifier: String): String = {
    val table = connection.getTable(TableName.valueOf(tableName))
    val get = new Get(rowKey.getBytes())
    val rs: Result = table.get(get)
    val result = rs.getValue(Bytes.toBytes(column), Bytes.toBytes(qualifier))
    val new_value = Bytes.toString(result)
    new_value
  }

  /**
   * Get the number of qualifiers on the column y with the rowKey x
   *
   * @param tableName
   * @param rowKey
   * @param column
   * @return Number of qualifiers on the column
   */
  def getRecordCount(tableName: String, rowKey: String, column: String): Int = {
    val table = connection.getTable(TableName.valueOf(tableName))
    val get = new Get(rowKey.getBytes())
    val rs: Result = table.get(get)
    rs.size()
  }


  /**
   * Get all the data from the HBase table
   *
   * @param tableName
   * @return count Number of values in the table
   */
  def getAllRecord(tableName: String): Int = {
    val table = connection.getTable(TableName.valueOf(tableName))
    var count = 0
    val s = new Scan()
    val ss = table.getScanner(s)
    for (r <- ss) {
      for (kv <- r.listCells()) {
        count = count + 1
        println(new String(CellUtil.cloneRow(kv)) + " ")
        println(new String(CellUtil.cloneFamily(kv)) + ":")
        println(new String(CellUtil.cloneQualifier(kv)) + " ")
        println(kv.getTimestamp() + " ")
        println(new String(CellUtil.cloneValue(kv)))
      }
    }
    count
  }

}
