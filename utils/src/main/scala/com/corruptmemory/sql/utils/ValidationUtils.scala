package com.corruptmemory.sql.utils

import scalaz._
import syntax.validation._
import syntax.std.optionV._
import scala.util.control.Exception._

class Checker[T](val key:String,testRunner:Vector[Checker.CheckFunc[T]] => (=> T) => CheckResult[T] = Checker.defaultRunTests) { self =>
  import CheckResult._
  import Checker._
  val tests:Vector[CheckFunc[T]] = Vector()
  def runTests(v: => T):CheckResult[T] = testRunner(tests)(v)
  def apply(v: => T):DBResult[T] = runTests(v).toKeyedError(key)
  def ++(f:CheckFunc[T]):Checker[T] = new Checker[T](self.key,testRunner) { override val tests = f +: self.tests }
}

object Checker {
  import CheckResult._
  type CheckFunc[T] = T => CheckResult[T]

  def defaultRunTests[T]:Vector[CheckFunc[T]] => (=> T) => CheckResult[T] =
    tests => v => (((tests.view map (x => safeVBracket(x(v)))) filter (_.isFailure)).foldLeft(v.successNel[CheckError]){(s,v1) => s ap CheckResult.constAp(v1)})

  def noNullRunTests[T <: AnyRef]:Vector[CheckFunc[T]] => (=> T) => CheckResult[T] =
    tests => v => if (v == null) message("Cannot be null")
                  else defaultRunTests(tests)(v)

  def nullableRunTests[T <: AnyRef]:Vector[CheckFunc[T]] => (=> T) => CheckResult[T] =
    tests => v => if (v == null) v.successNel
                  else defaultRunTests(tests)(v)

  def optionRunTests[T <: Option[X] forSome {type X;}]:Vector[CheckFunc[T]] => (=> T) => CheckResult[T] =
    noNullRunTests

  def test[T](msg:String,p:T => Boolean):CheckFunc[T] =
    value => if (p(value)) value.successNel else message(msg)

  // TODO: No, need to replace this
  val emailRegex = """([\w-]+(?:\.[\w-]+)*@(?:[\w-]+\.)+\w{2,7})\b""".r
}

trait Checkers {
  import CheckResult._
  import Checker._
  def checker[T <: AnyVal](key:String):Checker[T] = new Checker[T](key,defaultRunTests)
  def checkerNotNull[T <: AnyRef](key:String):Checker[T] = new Checker[T](key,noNullRunTests)
  def checkerNullable[T <: AnyRef](key:String):Checker[T] = new Checker[T](key,nullableRunTests)

  def required(msg:String = "Required"):CheckFunc[String] = { value =>
    val v = value.trim()
    if (!v.isEmpty) v.successNel else message(msg)
  }

  def notNone[T](msg:String = "Cannot be none"):CheckFunc[Option[T]] = { value =>
    if (value.isDefined) value.successNel
    else message(msg)
  }

  def mapTest[T](f:(=> T) => CheckResult[T]):CheckFunc[Option[T]] = { value =>
    value.fold(some = t => f(t).fold(success = s => Some(s).successNel,
                                     failure = f => f.fail),
               none = None.successNel)
  }

  def email(msg:String = "Not a valid email address"):CheckFunc[String] = _ match {
    case emailRegex(em) => em.successNel
    case _ => message(msg)
  }

  def minLength(length:Int)(msg:String = "Must be a minimum of %d characters".format(length)):CheckFunc[String] =
    test(msg,_.length >= length)

  def maxLength(length:Int)(msg:String = "Must be a maximum of %d characters".format(length)):CheckFunc[String] =
    test(msg,_.length <= length)

  def max[A](num:A)(msg:String = "Cannot be greater than %s".format(num))(implicit n:scala.math.Ordering[A]):CheckFunc[A] =
    test[A](msg,x => n.lteq(x,num))

  def min[A](num:A)(msg:String = "Cannot be less than %s".format(num))(implicit n:scala.math.Ordering[A]):CheckFunc[A] =
    test[A](msg,x => n.gteq(x,num))
}

object Checkers extends Checkers