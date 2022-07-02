import Dependencies._
import sbt.Keys._

def testScope(project: ProjectReference) = project % "test->test;test->compile"

val scalaPbVersion = "0.11.8"

val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.github.merlijn",
  scalaVersion := "3.1.0",
  fork := true,
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-v"),
  // javacOptions := Seq("-source", jvmV, "-target", jvmV),
  // resolvers += Resolver.url("typesafe", url("https://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns),
  scalacOptions := Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
//    "-source:3.0-migration",
//    "-Ywarn-dead-code",
//    "-Xfatal-warnings",
    "-language:higherKinds",
    "-language:existentials",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-encoding", "utf8"
  ),
  packageOptions in (Compile, packageBin) +=
    Package.ManifestAttributes(
      "Build-Time" -> new java.util.Date().toString,
      "Build-Commit" -> git.gitHeadCommit.value.getOrElse("No Git Revision Found")
    )
)

val dependencyOverrideSettings = Seq(
  // note that this does NOT add the dependencies, just forces the version
  dependencyOverrides ++= Seq(
    catsCore,
    akkaActor,
    akkaStream,
    "com.github.jnr" % "jnr-constants" % "0.9.9"
  )
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val defaultModuleSettings = commonSettings ++ dependencyOverrideSettings ++ SonatypePublish.settings

lazy val scalaPBSettings = Seq(PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value))

lazy val recipeDsl = project
  .in(file("recipe-dsl"))
  .settings(defaultModuleSettings)
  .settings(
    moduleName := "baker-recipe-dsl",
    fork := false,
    // we have to exclude the sources because of a compiler bug: https://issues.scala-lang.org/browse/SI-10134
    sources in (Compile, doc) := Seq.empty,
    libraryDependencies ++= Seq(
          scalaTest % "test",
          scalaTestCheck % "test",
          junitInterface % "test",
          slf4jApi % "test",
          logback % "test"
        )
  )

lazy val graphLib = project
  .in(file("graph-lib"))
  .settings(defaultModuleSettings)
  .settings(
    moduleName := "graph-lib",
    libraryDependencies ++= Seq(
      scalaTest % "test",
      scalaTestCheck % "test",
      junitInterface % "test",
      slf4jApi % "test",
      logback % "test"
    )
  )

lazy val intermediateLanguage = project.in(file("intermediate-language"))
  .settings(defaultModuleSettings)
  .settings(
    moduleName := "baker-intermediate-language",
    libraryDependencies ++= Seq(
      slf4jApi,
      typeSafeConfig,
      scalaTest % "test",
      scalaTestCheck % "test",
      scalaCheck % "test",
      logback % "test")
  ).dependsOn(graphLib)

lazy val runtime = project.in(file("runtime"))
  .settings(defaultModuleSettings)
  .settings(scalaPBSettings)
  .settings(
    moduleName := "baker-runtime",
    // we have to exclude the sources because of a compiler bug: https://issues.scala-lang.org/browse/SI-10134
    sources in (Compile, doc) := Seq.empty,
    libraryDependencies ++=
      Seq(
        akkaActor,
        akkaPersistence,
        akkaPersistenceQuery,
        akkaClusterSharding,
        akkaSlf4j,
        akkaStream,
        ficusConfig.cross(CrossVersion.for3Use2_13),
        catsCore,
        catsEffect,
        
        "com.thesamet.scalapb" %% "compilerplugin" % scalaPbVersion,
        "com.thesamet.scalapb" %% "scalapb-runtime" % scalaPbVersion % "protobuf",
        protobufJava,
        slf4jApi,
        findbugs % "provided",
        akkaTestKit % "test",
        akkaStreamTestKit % "test",
        scalaTest % "test",
        scalaCheck % "test",
        levelDB % "test",
        levelDBJni % "test",
        graphvizJava % "test",
        junitInterface % "test",
        mockito % "test",
        logback % "test")
  )
  .dependsOn(graphLib)

lazy val recipeCompiler = project.in(file("compiler"))
  .settings(defaultModuleSettings)
  .settings(
    moduleName := "baker-compiler",
    libraryDependencies ++= Seq(
      slf4jApi,
      scalaTest % "test",
      scalaTestCheck % "test",
      scalaCheck % "test",
      logback % "test")
  )
  .dependsOn(recipeDsl, intermediateLanguage, testScope(recipeDsl))

lazy val baker = project
  .in(file("."))
  .settings(defaultModuleSettings)
  .settings(noPublishSettings)
  .aggregate(recipeDsl, intermediateLanguage, recipeCompiler, runtime)
