package com.corruptmemory.extractors

import java.sql.{ResultSet}

import scalaz._
import syntax.validation._

object ColumnExtractors {
  def doGet[T](c:String,b: => DBResult[T])(implicit m:Manifest[T]):DBResult[T] = try {
      b
    } catch {
      case e:Exception => caught("error extracting '%s' of type '%s'".format(c,m.toString),e)
    }
  def doGetNotNullable[T](c:String,b: => DBResult[T])(implicit m:Manifest[T]):DBResult[T] =
    doGet(c,b) flatMap {v => if (v == null) message("got NULL for '%s' - NULLs forbidden, use Option[%s] for nullable values".format(c,m.toString))
                             else v.successNel }
  implicit def getString(c:String):ResultSet => DBResult[SqlString] = (r:ResultSet) => doGetNotNullable(c,SqlString(r.getString(c)).successNel)
  implicit def getNString(c:String):ResultSet => DBResult[SqlNString] = (r:ResultSet) => doGetNotNullable(c,SqlNString(r.getNString(c)).successNel)
  implicit def getArray(c:String):ResultSet => DBResult[SqlArray] = (r:ResultSet) => doGetNotNullable(c,SqlArray(r.getArray(c)).successNel)
  implicit def getBinaryStream(c:String):ResultSet => DBResult[SqlBinaryStream] = (r:ResultSet) => doGetNotNullable(c,SqlBinaryStream(r.getBinaryStream(c)).successNel)
  implicit def getAsciiStream(c:String):ResultSet => DBResult[SqlAsciiStream] = (r:ResultSet) => doGetNotNullable(c,SqlAsciiStream(r.getAsciiStream(c)).successNel)
  implicit def getBigDecimal(c:String):ResultSet => DBResult[SqlBigDecimal] = (r:ResultSet) => doGetNotNullable(c,SqlBigDecimal(r.getBigDecimal(c)).successNel)
  implicit def getBlob(c:String):ResultSet => DBResult[SqlBlob] = (r:ResultSet) => doGetNotNullable(c,SqlBlob(r.getBlob(c)).successNel)
  implicit def getClob(c:String):ResultSet => DBResult[SqlClob] = (r:ResultSet) => doGetNotNullable(c,SqlClob(r.getClob(c)).successNel)
  implicit def getNClob(c:String):ResultSet => DBResult[SqlNClob] = (r:ResultSet) => doGetNotNullable(c,SqlNClob(r.getNClob(c)).successNel)
  implicit def getCharacterStream(c:String):ResultSet => DBResult[SqlCharacterStream] = (r:ResultSet) => doGetNotNullable(c,SqlCharacterStream(r.getCharacterStream(c)).successNel)
  implicit def getNCharacterStream(c:String):ResultSet => DBResult[SqlNCharacterStream] = (r:ResultSet) => doGetNotNullable(c,SqlNCharacterStream(r.getNCharacterStream(c)).successNel)
  implicit def getByte(c:String):ResultSet => DBResult[SqlByte] = (r:ResultSet) => doGet(c,SqlByte(r.getByte(c)).successNel)
  implicit def getBytes(c:String):ResultSet => DBResult[SqlBytes] = (r:ResultSet) => doGetNotNullable(c,SqlBytes(r.getBytes(c)).successNel)
  implicit def getInt(c:String):ResultSet => DBResult[SqlInt] = (r:ResultSet) => doGet(c,SqlInt(r.getInt(c)).successNel)
  implicit def getLong(c:String):ResultSet => DBResult[SqlLong] = (r:ResultSet) => doGet(c,SqlLong(r.getLong(c)).successNel)
  implicit def getShort(c:String):ResultSet => DBResult[SqlShort] = (r:ResultSet) => doGet(c,SqlShort(r.getShort(c)).successNel)
  implicit def getDouble(c:String):ResultSet => DBResult[SqlDouble] = (r:ResultSet) => doGet(c,SqlDouble(r.getDouble(c)).successNel)
  implicit def getFloat(c:String):ResultSet => DBResult[SqlFloat] = (r:ResultSet) => doGet(c,SqlFloat(r.getFloat(c)).successNel)
  implicit def getBoolean(c:String):ResultSet => DBResult[SqlBoolean] = (r:ResultSet) => doGet(c,SqlBoolean(r.getBoolean(c)).successNel)
  implicit def getDate(c:String):ResultSet => DBResult[SqlDate] = (r:ResultSet) => doGetNotNullable(c,SqlDate(r.getDate(c)).successNel)
  implicit def getTime(c:String):ResultSet => DBResult[SqlTime] = (r:ResultSet) => doGetNotNullable(c,SqlTime(r.getTime(c)).successNel)
  implicit def getTimestamp(c:String):ResultSet => DBResult[SqlTimestamp] = (r:ResultSet) => doGetNotNullable(c,SqlTimestamp(r.getTimestamp(c)).successNel)
  implicit def getRef(c:String):ResultSet => DBResult[SqlRef] = (r:ResultSet) => doGetNotNullable(c,SqlRef(r.getRef(c)).successNel)
  implicit def getRowId(c:String):ResultSet => DBResult[SqlRowId] = (r:ResultSet) => doGetNotNullable(c,SqlRowId(r.getRowId(c)).successNel)
  implicit def getSQLXML(c:String):ResultSet => DBResult[SqlSQLXML] = (r:ResultSet) => doGetNotNullable(c,SqlSQLXML(r.getSQLXML(c)).successNel)
  implicit def getURL(c:String):ResultSet => DBResult[SqlURL] = (r:ResultSet) => doGetNotNullable(c,SqlURL(r.getURL(c)).successNel)
  implicit def getOption[T : Manifest](c:String):ResultSet => DBResult[Option[T]] =
    { (r:ResultSet) =>
        doGet(c,(r.getObject(c) match {
          case null => None
          case x => Some(x.asInstanceOf[T])
        }).successNel)
    }

  def get[T](c:String)(implicit f:String => ResultSet => DBResult[T], m:Manifest[T]):ResultSet => DBResult[T] = f(c)
}
