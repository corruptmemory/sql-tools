package com.corruptmemory.jdbc

// import java.sql.{Connection, Statement, PreparedStatement}

// import com.corruptmemory.sql.utils._

// trait Statements {
//   def withStatementGen[T,S <: Statement](gen:Connection => S)(f:S => DBResult[T]):Connection => DBResult[T] = { conn =>
//     try {
//     val stmt = gen(conn)
//     try { f(stmt) }
//     catch { case e:Exception => uncaught(e) }
//     finally  { stmt.close() }
//     } catch { case e:Exception => uncaught(e) }
//   }

//   def withPrepatedStatement[T](sql:String)(f:PreparedStatement => DBResult[T]):Connection => DBResult[T] =
//     withStatementGen(_.prepareStatement(sql))(f)

//   def withStatement[T](f:Statement => DBResult[T]):Connection => DBResult[T] =
//     withStatementGen(_.createStatement())(f)
// }