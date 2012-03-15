package com.corruptmemory.sql.extractors

import com.corruptmemory.sql.utils._

import java.sql.ResultSet
import scalaz._
import syntax.applicative._

trait Builders {
  class Builder0[T](val ce:ResultSet => DBResult[SqlValue[T]]) {
    def apply[R](f:T => DBResult[R]):ResultSet => DBResult[R] = (rs:ResultSet) => ce(rs) flatMap (v => f(v.value))
    def applyUnwrapped:ResultSet => DBResult[T] = (rs:ResultSet) => ce(rs).map(_.value)
    def ~[U](b0:Builder0[U]):Builder[(T,U)] = new Builder[(T,U)]((r:ResultSet) => (applyUnwrapped(r) |@| b0.applyUnwrapped(r)).tupled)
    def ~[U](b1:Builder1[U]):Builder[(T,Option[U])] = new Builder[(T,Option[U])]((r:ResultSet) => (applyUnwrapped(r) |@| b1.applyUnwrapped(r)).tupled)
  }

  class Builder1[T](val ce:ResultSet => DBResult[Option[SqlValue[T]]]) {
    def apply[R](f:Option[T] => DBResult[R]):ResultSet => DBResult[R] = (rs:ResultSet) => ce(rs) flatMap (v => f(v.map(_.value)))
    def applyUnwrapped:ResultSet => DBResult[Option[T]] = (rs:ResultSet) => ce(rs).map(_.map(_.value))
    def ~[U](b0:Builder0[U]):Builder[(Option[T],U)] = new Builder[(Option[T],U)]((r:ResultSet) => (applyUnwrapped(r) |@| b0.applyUnwrapped(r)).tupled)
    def ~[U](b1:Builder1[U]):Builder[(Option[T],Option[U])] = new Builder[(Option[T],Option[U])]((r:ResultSet) => (applyUnwrapped(r) |@| b1.applyUnwrapped(r)).tupled)
  }

  class Builder[M](val ce:ResultSet => DBResult[M]) {
    def apply[R](f:M => DBResult[R]):ResultSet => DBResult[R] = (rs:ResultSet) => ce(rs) flatMap f
    def ~[T](b0:Builder0[T]):Builder[(M,T)] = new Builder[(M,T)]((r:ResultSet) => (ce(r) |@| b0.applyUnwrapped(r)).tupled)
    def ~[T](b1:Builder1[T]):Builder[(M,Option[T])] = new Builder[(M,Option[T])]((r:ResultSet) => (ce(r) |@| b1.applyUnwrapped(r)).tupled)
  }

  class BuilderWrapper0[T](val ce:ResultSet => DBResult[SqlValue[T]]) {
    def build:Builder0[T] = new Builder0(ce)
  }

  class BuilderWrapper1[T](val ce:ResultSet => DBResult[Option[SqlValue[T]]]) {
    def build:Builder1[T] = new Builder1(ce)
  }

  implicit def toBuilder0[T](m:ResultSet => DBResult[SqlValue[T]]):Builder0[T] = new Builder0[T](m)
  implicit def toBuilder1[T](m:ResultSet => DBResult[Option[SqlValue[T]]]):Builder1[T] = new Builder1[T](m)
  implicit def toBuilderWrapper0[T](m:ResultSet => DBResult[SqlValue[T]]):BuilderWrapper0[T] = new BuilderWrapper0[T](m)
  implicit def toBuilderWrapper1[T](m:ResultSet => DBResult[Option[SqlValue[T]]]):BuilderWrapper1[T] = new BuilderWrapper1[T](m)

  object ~ {
    def unapply[M,N](l:(M,N)):Option[(M,N)] = Some(l)
  }
}

object Builders extends Builders