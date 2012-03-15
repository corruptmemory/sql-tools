package com.corruptmemory.sql

package object utils {
  import scalaz._
  import syntax.validation._

  type DBResult[X] = ValidationNEL[Error,X]
  type CheckResult[X] = ValidationNEL[CheckError,X]

  sealed trait CheckError
  sealed trait Error
  case class Message(value:String) extends Error with CheckError
  case class Rolledback(value:String) extends Error
  case class Caught(message:String,exception:Exception) extends Error with CheckError
  case class Uncaught(exception:Exception) extends Error with CheckError
  case class KeyedMessage(key:String,message:String) extends Error

  object DBResult {
    def rolledback[X](v:String):DBResult[X] = Rolledback(v).asInstanceOf[Error].failNel[X]
    def message[X](v:String):DBResult[X] = Message(v).asInstanceOf[Error].failNel[X]
    def caught[X](m:String,e:Exception):DBResult[X] = Caught(m,e).asInstanceOf[Error].failNel[X]
    def uncaught[X](e:Exception):DBResult[X] = Uncaught(e).asInstanceOf[Error].failNel[X]
    def keyedMessage[X](key:String,message:String):DBResult[X] = KeyedMessage(key,message).asInstanceOf[Error].failNel[X]
    def safeBracket[T](b: => T):DBResult[T] = try {
      b.successNel
    } catch { case e:Exception => uncaught(e) }

    def safeVBracket[T](b: => DBResult[T]):DBResult[T] = try {
      b
    } catch { case e:Exception => uncaught(e) }
    def constAp[A,B](b:DBResult[B]):DBResult[A => B] = b map ((x:B) => ((_:A) => x))
  }

  object CheckResult {
    def message[X](v:String):CheckResult[X] = Message(v).asInstanceOf[CheckError].failNel[X]
    def caught[X](m:String,e:Exception):CheckResult[X] = Caught(m,e).asInstanceOf[CheckError].failNel[X]
    def uncaught[X](e:Exception):CheckResult[X] = Uncaught(e).asInstanceOf[CheckError].failNel[X]
    def safeBracket[T](b: => T):CheckResult[T] = try {
      b.successNel
    } catch { case e:Exception => uncaught(e) }

    def safeVBracket[T](b: => CheckResult[T]):CheckResult[T] = try {
      b
    } catch { case e:Exception => uncaught(e) }
    def constAp[A,B](b:CheckResult[B]):CheckResult[A => B] = b map ((x:B) => ((_:A) => x))
    class CheckResultOps[X](cr:CheckResult[X]) {
      def toKeyedError(key:String):DBResult[X] =
        cr.fold(success = s => s.successNel[Error],
                failure = f => (f map {
                  case Message(m) => KeyedMessage(key,m).asInstanceOf[Error]
                  case Caught(m,e) => KeyedMessage(key,"%s -- %s".format(m,e)).asInstanceOf[Error]
                  case Uncaught(e) => KeyedMessage(key,"uncaught exception -- %s".format(e)).asInstanceOf[Error]
                }).fail[X])
    }
    implicit def toCheckResultOps[X](cr:CheckResult[X]):CheckResultOps[X] = new CheckResultOps[X](cr)
  }
}