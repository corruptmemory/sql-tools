package com.corruptmemory.jdbc

import java.sql.Connection

import com.corruptmemory.sql.utils._

sealed trait ConnectionW {
  def connection:Connection
  def withConnection[T](f:Connection => DBResult[T]):DBResult[T] = f(connection)
}

class ReaderConnection(val connection:Connection) extends ConnectionW
class WriterConnection(val connection:Connection) extends ConnectionW

trait ConnectionFactory[C <: ConnectionW] {
  def getConnection():C
}

trait BasicConnectionFactory extends ConnectionFactory[WriterConnection]