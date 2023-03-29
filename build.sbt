ThisBuild / organization := "ch.epfl.data"
ThisBuild / scalaVersion := "2.12.8"
ThisBuild / version := "1.0-SNAPSHOT"

val project_name = "SparkExperiments"
name := project_name

val sparkVersion = "3.3.0"

run / fork := true

val sparkDeployOption = Option(System.getProperty("sparkDeploy")).getOrElse("local")

lazy val sparkSettings = if (sparkDeployOption == "local") { 
  println("Local Spark deployment. Please add -DsparkDeploy=cluster to your sbt command to assemble for Spark cluster.")
  Seq(
  libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion, 
  libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion, 
)} else { 
  Seq(
  // Use following config with "provided" when assemblying a uber jar
  libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion % "provided", 
)}

lazy val sparkExperiment = (project in file("."))
  .settings(
    name := f"${project_name}",
    // scalacOptions += "-Ywarn-unused",
    sparkSettings,
    Test / parallelExecution := false,
    excludeDependencies += "commons-logging" % "commons-logging",
    assemblyMergeStrategy in assembly := {
      case PathList("io", "netty", xs @ _*)               => MergeStrategy.first
      case PathList("google", "protobuf", xs @ _*)        => MergeStrategy.first
      case PathList("com", "google", "protobuf", xs @ _*) => MergeStrategy.first
      case PathList("scalapb", xs @ _*)                   => MergeStrategy.first
      case "application.conf"                             => MergeStrategy.concat
      case "reference.conf"                               => MergeStrategy.concat
      case "module-info.class"                            => MergeStrategy.concat
      case "META-INF/io.netty.versions.properties"        => MergeStrategy.first
      case "META-INF/native/libnetty-transport-native-epoll.so" =>
        MergeStrategy.first
      case n if n.endsWith(".txt")   => MergeStrategy.concat
      case n if n.endsWith("NOTICE") => MergeStrategy.concat
      case "logback.xml" => MergeStrategy.first 
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )


