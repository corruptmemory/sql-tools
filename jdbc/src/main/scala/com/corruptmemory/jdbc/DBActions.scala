package com.corruptmemory.jdbc

import java.sql.Connection

import com.corruptmemory.sql.utils._
import DBResult._

import scalaz._
import scalaz.syntax.validation._

trait FlexiMonad[A,F[_],B,T[_]] {
  def map(fa:F[A])(f:A => B):T[B]
  def flatMap(fa:F[A])(f:A => T[B]):T[B]
}

trait DBAction[A] {
  type C <: ConnectionW
  type F[_] <: DBAction[_]

  def action:Connection => DBResult[A]
  def apply(c:C):DBResult[A] = c.withConnection(action)

  def map[B,T[_] <: DBAction[_]](f: A => B)(implicit cbf:FlexiMonad[A,F,B,T]):T[B] =
    cbf.map(this.asInstanceOf[F[A]])(f)

  def flatMap[B,T[_] <: DBAction[_]](f: A => T[B])(implicit cbf:FlexiMonad[A,F,B,T]):T[B] =
    cbf.flatMap(this.asInstanceOf[F[A]])(f)
}

trait DBReadActionInstances0 {
  implicit def readWriteCBF[A,B]:FlexiMonad[A,DBReadAction,B,DBWriteAction] = new FlexiMonad[A,DBReadAction,B,DBWriteAction] {
    def map(fa:DBReadAction[A])(f:A => B):DBWriteAction[B] =
      new DBWriteAction[B]((c:Connection) => fa.action(c) map f)

    def flatMap(fa:DBReadAction[A])(f:A => DBWriteAction[B]):DBWriteAction[B] =
      new DBWriteAction[B]((c:Connection) => fa.action(c).fold(failure = f1 => f1.fail,
                                                               success = s => f(s).action(c)))
  }
}

trait DBReadActionInstances extends DBReadActionInstances0 {
  implicit def readReadCBF[A,B]:FlexiMonad[A,DBReadAction,B,DBReadAction] = new FlexiMonad[A,DBReadAction,B,DBReadAction] {
    def map(fa:DBReadAction[A])(f:A => B):DBReadAction[B] =
      new DBReadAction[B]((c:Connection) => fa.action(c) map f)

    def flatMap(fa:DBReadAction[A])(f:A => DBReadAction[B]):DBReadAction[B] =
      new DBReadAction[B]((c:Connection) => fa.action(c).fold(failure = f1 => f1.fail,
                                                              success = s => f(s).action(c)))
  }
}

trait DBWriteActionInstances {
  implicit def writeWriteCBF[A,B]:FlexiMonad[A,DBWriteAction,B,DBWriteAction] = new FlexiMonad[A,DBWriteAction,B,DBWriteAction] {
    def map(fa:DBWriteAction[A])(f:A => B):DBWriteAction[B] =
      new DBWriteAction[B]((c:Connection) => fa.action(c) map f)

    def flatMap(fa:DBWriteAction[A])(f:A => DBWriteAction[B]):DBWriteAction[B] =
      new DBWriteAction[B]((c:Connection) => fa.action(c).fold(failure = f1 => f1.fail,
                                                               success = s => f(s).action(c)))
  }
}

trait DBActionInstances extends DBWriteActionInstances with DBReadActionInstances

class DBReadAction[T](val action:Connection => DBResult[T]) extends DBAction[T] {
  type C = ReaderConnection
  type F[X] = DBReadAction[X]
}

class DBWriteAction[T](val action:Connection => DBResult[T]) extends DBAction[T] {
  type C = WriterConnection
  type F[X] = DBWriteAction[X]
}

object DBActions extends DBActionInstances {
  def dbReadAction[T](a:Connection => DBResult[T]):DBReadAction[T] = new DBReadAction[T](a)
  def dbWriteAction[T](a:Connection => DBResult[T]):DBWriteAction[T] = new DBWriteAction[T](a)

  def query[T](sql:String) = dbReadAction[T](_ => message("nope"))
  def insert[T](sql:String) = dbWriteAction[T](_ => message("nope"))

  def q1 = for {
    x <- query[Int]("select * from whatever")
    _ <- insert[Unit]("insert")
  } yield x+6

  def q2 = for {
    x <- query[Int]("select * from whatever")
  } yield x*3
}