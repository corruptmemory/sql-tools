package com.corruptmemory.jdbc

// import java.sql.Connection

// import com.corruptmemory.sql.utils._

// import scalaz._
// import scalaz.syntax._
// import syntax.validation._
// import syntax.pointed._
// import syntax.monoid._
// import syntax.bind._
// import syntax.monad._

// class TransactionBuilder[T](begin:Connection => DBResult[Unit] = Transactions.defaultBegin,
//                             commit:Connection => DBResult[Unit] = Transactions.defaultCommit,
//                             rollback:Connection => DBResult[T] = Transactions.defaultRollback) {
//   def apply(f: Connection => DBResult[T]):ConnectionW[Write] => DBResult[T] =
//     (_ {c =>
//       val b = begin(c)
//       if (b.isSuccess) {
//         val result = f(c)
//         (result flatMap (_ => commit(c) flatMap (_ => result))) orElse rollback(c)
//       } else b.fold(failure = f => f.fail,
//                     success = _ => message("Could not begin transaction"))
//     })
// }

// trait Transactions {
//   def transaction[T](transB:TransactionBuilder[T] = new TransactionBuilder())(f:Connection => DBResult[T]):ConnectionW[Write] => DBResult[T] =
//     transB(f)
// }

// object Transactions {
//   def defaultBegin:Connection => DBResult[Unit] = c => safeBracket(c.setAutoCommit(false))
//   def defaultCommit:Connection => DBResult[Unit] = c => safeBracket(c.commit())
//   def defaultRollback[T]:Connection => DBResult[T] =
//     c => safeBracket(c.rollback()) flatMap (_ => rolledback[T]("transaction rolled back"))
// }