import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "com.corruptmemory"
  val buildScalaVersion = "2.9.1"
  val buildVersion      = "0.0.1-SNAPSHOT"

  val buildSettings = Defaults.defaultSettings ++
                      Seq (organization := buildOrganization,
                           scalaVersion := buildScalaVersion,
                           version      := buildVersion,
                           shellPrompt  := ShellPrompt.buildShellPrompt)
}

object ShellPrompt {

  object devnull extends ProcessLogger {
    def info (s: => String) {}
    def error (s: => String) { }
    def buffer[T] (f: => T): T = f
  }

  val current = """\*\s+(\w+)""".r

  def gitBranches = ("git branch --no-color" lines_! devnull mkString)

  val buildShellPrompt = {
    (state: State) => {
      val currBranch = current findFirstMatchIn gitBranches map (_ group(1)) getOrElse "-"
      val currProject = Project.extract (state).currentProject.id
      "%s:%s:%s> ".format (currProject, currBranch, BuildSettings.buildVersion)
    }
  }
}

object Resolvers {
  val sonatatypeSnapshots = "sonatatypeSnapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  val sonatatypeReleases = "sonatatypeSnapshots" at "https://oss.sonatype.org/content/repositories/releases"
  val jbossResolver = "jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"
  val javaNetResolvers = "Java.net Maven 2 Repo" at "http://download.java.net/maven/2"
  val terracottaResolver = "Terracotta " at "http://repository.opencastproject.org/nexus/content/repositories/terracotta/"

  val allResolvers = Seq(sonatatypeReleases,sonatatypeSnapshots,jbossResolver,javaNetResolvers,terracottaResolver)
}

object Dependencies {
  val scalaCheckVersion = "1.9"
  val scalaZVersion = "7.0-SNAPSHOT"
  val jodaTimeVersion = "2.0"
  val jodaConvertVersion = "1.1"

  val scalaz = "org.scalaz" %% "scalaz-core" % scalaZVersion
  val scalaCheck = "org.scala-tools.testing" %% "scalacheck" % scalaCheckVersion % "test"
  val jodaTime = "joda-time" % "joda-time" % jodaTimeVersion
  val jodaConvert = "org.joda" % "joda-convert" % jodaConvertVersion
}

object KickassServiceBuild extends Build {
  val buildShellPrompt = ShellPrompt.buildShellPrompt

  import Dependencies._
  import BuildSettings._
  import Resolvers._

  def namedSubProject(projectName: String, id: String, path: File, settings: Seq[Setting[_]]) = Project(id, path, settings = buildSettings ++ settings ++ Seq(name := projectName))

  lazy val sqlTools = Project("sql-tools",
                              file("."),
                              settings = buildSettings ++ Seq(name := "Primative SQL tools")) aggregate (extractors)

  val extractorsDeps = Seq(scalaz,jodaTime,jodaConvert)
  lazy val extractors = namedSubProject("JDBC Extractors",
                                   "extractors",
                                   file("extractors"),
                                   Seq(scalacOptions ++= Seq("-deprecation", "-unchecked"),
                                       libraryDependencies := extractorsDeps,
                                       resolvers ++= allResolvers))
}
