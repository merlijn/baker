import Dependencies.{scalaGraph, _}
import sbt.Keys._

import CrossPlugin.autoImport.crossProject
import CrossPlugin.autoImport.CrossType

def testScope(project: ProjectReference) = project % "test->test;test->compile"

val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.ing.baker",
  scalaVersion := "2.12.4",
  crossScalaVersions := Seq("2.12.4"),
  fork := true,
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-v"),
  javacOptions := Seq("-source", jvmV, "-target", jvmV),
  scalacOptions := Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Ywarn-dead-code",
    "-language:higherKinds",
    "-language:existentials",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-encoding", "utf8",
    s"-target:jvm-$jvmV",
    "-Xfatal-warnings"
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

lazy val defaultModuleSettings = commonSettings ++ dependencyOverrideSettings ++ Revolver.settings ++ SonatypePublish.settings

lazy val scalaPBSettings = Seq(PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value))

lazy val bakertypes = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("bakertypes"))
  .settings(defaultModuleSettings)
  .settings(
    moduleName := "baker-types",
    fork := false,
    libraryDependencies ++= compileDeps(
      objenisis,
      scalaReflect(scalaVersion.value)
    ) ++ testDeps(scalaTest, scalaCheck, logback, scalaCheck)
  )

lazy val bakertypesJvm = bakertypes.jvm
lazy val bakertypesJs = bakertypes.js

lazy val recipeDsl = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("recipe-dsl"))
  .settings(defaultModuleSettings)
  .settings(
    moduleName := "baker-recipe-dsl",
    fork := false,
    // we have to exclude the sources because of a compiler bug: https://issues.scala-lang.org/browse/SI-10134
    sources in (Compile, doc) := Seq.empty,
    libraryDependencies ++=
      compileDeps(
        javaxInject,
        paranamer,
        reflections,
        scalaReflect(scalaVersion.value),
      ) ++
        testDeps(
          scalaTest,
          scalaCheck,
          junitInterface,
          slf4jApi,
          logback
        )
  ).dependsOn(bakertypes)

lazy val recipeDslJvm = recipeDsl.jvm
lazy val recipeDslJs = recipeDsl.js

lazy val intermediateLanguage = project.in(file("intermediate-language"))
  .settings(defaultModuleSettings)
  .settings(
    moduleName := "baker-intermediate-language",
    libraryDependencies ++= compileDeps(
      scalaGraph,
      slf4jApi,
      scalaGraphDot,
      objenisis,
      typeSafeConfig
    ) ++ testDeps(scalaTest, scalaCheck, logback)
  ).dependsOn(bakertypesJvm)


lazy val runtime = project.in(file("runtime"))
  .settings(defaultModuleSettings)
  .settings(scalaPBSettings)
  .settings(
    moduleName := "baker-runtime",
    // we have to exclude the sources because of a compiler bug: https://issues.scala-lang.org/browse/SI-10134
    sources in (Compile, doc) := Seq.empty,
    libraryDependencies ++=
      compileDeps(
        akkaActor,
        akkaPersistence,
        akkaPersistenceQuery,
        akkaClusterSharding,
        akkaInmemoryJournal,
        akkaSlf4j,
        akkaStream,
        ficusConfig,
        catsCore,
        catsEffect,
        guava,
        chill,
        objenisis,
        scalapbRuntime,
        protobufJava,
        jodaTime,
        kryo,
        kryoSerializers,
        slf4jApi
      ) ++ testDeps(
        akkaTestKit,
        akkaStreamTestKit,
        akkaInmemoryJournal,
        akkaPersistenceCassandra,
        levelDB,
        levelDBJni,
        betterFiles,
        graphvizJava,
        junitInterface,
        scalaTest,
        scalaCheck,
        mockito,
        logback)
        ++ providedDeps(findbugs)
  )
  .dependsOn(intermediateLanguage, testScope(recipeDslJvm), testScope(recipeCompiler), testScope(bakertypesJvm))



lazy val recipeCompiler = project.in(file("compiler"))
  .settings(defaultModuleSettings)
  .settings(
    moduleName := "baker-compiler",
    libraryDependencies ++=
      compileDeps(slf4jApi) ++ testDeps(scalaTest, scalaCheck, logback)
  )
  .dependsOn(recipeDslJvm, intermediateLanguage, testScope(recipeDslJvm))

lazy val baker = project
  .in(file("."))
  .settings(defaultModuleSettings)
  .settings(noPublishSettings)
  .aggregate(bakertypesJvm, bakertypesJs, recipeDslJvm, recipeDslJs, intermediateLanguage, recipeCompiler, runtime)
