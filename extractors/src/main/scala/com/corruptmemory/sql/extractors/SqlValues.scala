package com.corruptmemory.sql.extractors

import com.corruptmemory.sql.utils._

import java.sql.{Array => SqlAry, Blob, Clob, NClob, Date, Time, Timestamp, Ref, RowId, SQLXML}
import java.io.{InputStream,Reader => JavaReader}
import java.math.BigDecimal
import java.net.URL

sealed trait SqlValue[T] {
  def value:T
}
case class SqlString(value:String) extends SqlValue[String]
case class SqlNString(value:String) extends SqlValue[String]
case class SqlSQLXML(value:SQLXML) extends SqlValue[SQLXML]
case class SqlInt(value:Int) extends SqlValue[Int]
case class SqlBoolean(value:Boolean) extends SqlValue[Boolean]
case class SqlLong(value:Long) extends SqlValue[Long]
case class SqlShort(value:Short) extends SqlValue[Short]
case class SqlDouble(value:Double) extends SqlValue[Double]
case class SqlFloat(value:Float) extends SqlValue[Float]
case class SqlByte(value:Byte) extends SqlValue[Byte]
case class SqlBytes(value:Array[Byte]) extends SqlValue[Array[Byte]]
case class SqlArray(value:SqlAry) extends SqlValue[SqlAry]
case class SqlDate(value:Date) extends SqlValue[Date]
case class SqlTime(value:Time) extends SqlValue[Time]
case class SqlTimestamp(value:Timestamp) extends SqlValue[Timestamp]
case class SqlBlob(value:Blob) extends SqlValue[Blob]
case class SqlClob(value:Clob) extends SqlValue[Clob]
case class SqlNClob(value:NClob) extends SqlValue[NClob]
case class SqlBinaryStream(value:InputStream) extends SqlValue[InputStream]
case class SqlAsciiStream(value:InputStream) extends SqlValue[InputStream]
case class SqlCharacterStream(value:JavaReader) extends SqlValue[JavaReader]
case class SqlNCharacterStream(value:JavaReader) extends SqlValue[JavaReader]
case class SqlBigDecimal(value:BigDecimal) extends SqlValue[BigDecimal]
case class SqlRef(value:Ref) extends SqlValue[Ref]
case class SqlRowId(value:RowId) extends SqlValue[RowId]
case class SqlURL(value:URL) extends SqlValue[URL]

trait SqlValues {
  implicit def sqlValueToValue[V](v:SqlValue[V]):V = v.value
  implicit def sqlOptionToValue[V](v:Option[SqlValue[V]]) = v map (_.value)
}

object Test extends SqlValues with Builders {
  import ColumnExtractors._
  import scalaz._
  import syntax.validation._
  import java.sql.{ResultSet}

  def foo1 = get[SqlString]("a").build {
    case a => (a.length).successNel
  }
  def foo2 = (get[SqlString]("a")~get[SqlInt]("b")) {
    case a~b => (a.length + b).successNel
  }
  def foo3 = (get[SqlString]("a")~get[SqlInt]("b")~get[Option[SqlDouble]]("c")) {
    case a~b~c => ((a.length + b)*c.getOrElse(1.0)).successNel
  }
  def foo4 = (get[SqlString]("a")~get[SqlInt]("b")~get[Option[SqlDouble]]("c")~get[Option[SqlNString]]("d")) {
    case a~b~c~d => ((a.length + b)*c.getOrElse(1.0),d).successNel
  }
  def foo5 = (get[Option[SqlNString]]("d")~get[SqlString]("a")~get[SqlInt]("b")~get[Option[SqlDouble]]("c")) {
    case d~a~b~c => ((a.length + b)*c.getOrElse(1.0),d.getOrElse("")).successNel
  }
}