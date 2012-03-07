package com.corruptmemory

package object extractors {
  import scalaz._
  import syntax.validation._

  type DBResult[X] = ValidationNEL[Error,X]

  sealed trait Error
  case class Message(value:String) extends Error
  case class Caught(message:String,exception:Exception) extends Error
  case class Uncaught(exception:Exception) extends Error

  def message[X](v:String):DBResult[X] = Message(v).asInstanceOf[Error].failNel[X]
  def caught[X](m:String,e:Exception):DBResult[X] = Caught(m,e).asInstanceOf[Error].failNel[X]
  def uncaught[X](e:Exception):DBResult[X] = Uncaught(e).asInstanceOf[Error].failNel[X]
}