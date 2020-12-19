import Dependencies.{scalaGraph, _}
import sbt.Keys._

def testScope(project: ProjectReference) = project % "test->test;test->compile"

val dottyVersion = "3.0.0-M1"

val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.ing.baker",
  scalaVersion := dottyVersion,
  crossScalaVersions := Seq("2.13.4", dottyVersion),
  fork := true,
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-v"),
  javacOptions := Seq("-source", jvmV, "-target", jvmV),
  resolvers += Resolver.url("typesafe", url("https://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns),
  scalacOptions := Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
//    "-Ywarn-dead-code",
//    "-Xfatal-warnings",
    "-language:higherKinds",
    "-language:existentials",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-encoding", "utf8",
    s"-target:jvm-$jvmV"
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

lazy val bakerTypes = project
  .in(file("bakertypes"))
  .settings(defaultModuleSettings)
  .settings(
    moduleName := "baker-types",
    fork := false,
    libraryDependencies ++= Seq(
        objenisis,
//        scalaReflect(scalaVersion.value),
        (scalaTest % "test").withDottyCompat(scalaVersion.value),
        (scalaCheck % "test").withDottyCompat(scalaVersion.value),
        logback % "test"
    )
  )

lazy val recipeDsl = project
  .in(file("recipe-dsl"))
  .settings(defaultModuleSettings)
  .settings(
    moduleName := "baker-recipe-dsl",
    fork := false,
    // we have to exclude the sources because of a compiler bug: https://issues.scala-lang.org/browse/SI-10134
    sources in (Compile, doc) := Seq.empty,
    libraryDependencies ++= Seq(
          javaxInject,
          paranamer,
          reflections,
          (scalaTest % "test").withDottyCompat(scalaVersion.value),
          (scalaCheck % "test").withDottyCompat(scalaVersion.value),
          junitInterface % "test",
          slf4jApi % "test",
          logback % "test"
        )
  ).dependsOn(bakerTypes)

lazy val intermediateLanguage = project.in(file("intermediate-language"))
  .settings(defaultModuleSettings)
  .settings(
    moduleName := "baker-intermediate-language",
    libraryDependencies ++= Seq(
      slf4jApi,
      objenisis,
      typeSafeConfig,
      scalaGraph.withDottyCompat(scalaVersion.value),
      scalaGraphDot.withDottyCompat(scalaVersion.value),
      (scalaTest % "test").withDottyCompat(scalaVersion.value),
      (scalaCheck % "test").withDottyCompat(scalaVersion.value),
      logback % "test")
  ).dependsOn(bakerTypes)


lazy val runtime = project.in(file("runtime"))
  .settings(defaultModuleSettings)
  .settings(scalaPBSettings)
  .settings(
    moduleName := "baker-runtime",
    // we have to exclude the sources because of a compiler bug: https://issues.scala-lang.org/browse/SI-10134
    sources in (Compile, doc) := Seq.empty,
    libraryDependencies ++=
      compileDeps(
        akkaActor.withDottyCompat(scalaVersion.value),
        akkaPersistence.withDottyCompat(scalaVersion.value),
        akkaPersistenceQuery.withDottyCompat(scalaVersion.value),
        akkaClusterSharding.withDottyCompat(scalaVersion.value),
        akkaInmemoryJournal.withDottyCompat(scalaVersion.value),
        akkaSlf4j.withDottyCompat(scalaVersion.value),
        akkaStream.withDottyCompat(scalaVersion.value),
        chill.withDottyCompat(scalaVersion.value),
        ficusConfig.withDottyCompat(scalaVersion.value),
        ("com.thesamet.scalapb" %% "scalapb-runtime" % "0.10.9" % "protobuf").withDottyCompat(scalaVersion.value),
        ("com.thesamet.scalapb" %% "compilerplugin" % "0.10.9").withDottyCompat(scalaVersion.value),
        catsCore.withDottyCompat(scalaVersion.value),
        catsEffect.withDottyCompat(scalaVersion.value),
        guava,
        objenisis,
        protobufJava,
        kryo,
        kryoSerializers,
        slf4jApi,
        findbugs % "provided",
        akkaTestKit.withDottyCompat(scalaVersion.value) % "test",
        akkaStreamTestKit.withDottyCompat(scalaVersion.value) % "test",
        akkaInmemoryJournal.withDottyCompat(scalaVersion.value) % "test",
        akkaPersistenceCassandra.withDottyCompat(scalaVersion.value) % "test",
        scalaTest.withDottyCompat(scalaVersion.value) % "test",
        scalaCheck.withDottyCompat(scalaVersion.value) % "test",
        levelDB % "test",
        levelDBJni % "test",
        betterFiles.withDottyCompat(scalaVersion.value) % "test",
        graphvizJava % "test",
        junitInterface % "test",
        mockito % "test",
        logback % "test")
  )
  .dependsOn(intermediateLanguage, testScope(recipeDsl), testScope(recipeCompiler), testScope(bakerTypes))

lazy val recipeCompiler = project.in(file("compiler"))
  .settings(defaultModuleSettings)
  .settings(
    moduleName := "baker-compiler",
    libraryDependencies ++= Seq(
      slf4jApi,
      scalaTest.withDottyCompat(scalaVersion.value) % "test",
      scalaCheck.withDottyCompat(scalaVersion.value) % "test",
      logback % "test")
  )
  .dependsOn(recipeDsl, intermediateLanguage, testScope(recipeDsl))

lazy val baker = project
  .in(file("."))
  .settings(defaultModuleSettings)
  .settings(noPublishSettings)
  .aggregate(bakerTypes, recipeDsl, intermediateLanguage, recipeCompiler, runtime)
