package com.corruptmemory.jdbc

// import java.sql.{Connection, Statement, ResultSet}

// import com.corruptmemory.sql.utils._

// trait ResultSets {
//   def withResultSet[T,S <: Statement](gen:S => ResultSet)(f:ResultSet => DBResult[T]):S => DBResult[T] = { stmt =>
//     try {
//     val rs = gen(stmt)
//     try { f(rs) }
//     catch { case e:Exception => uncaught(e) }
//     finally  { rs.close() }
//     } catch { case e:Exception => uncaught(e) }
//   }
// }