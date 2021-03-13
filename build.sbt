import Dependencies._
import sbt.Keys._

def testScope(project: ProjectReference) = project % "test->test;test->compile"

val dottyVersion = "3.0.0-RC1"
val scalaPbVersion = "0.11.0-M7"

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
          javaxInject,
          paranamer,
          reflections,
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
  )


lazy val runtime = project.in(file("runtime"))
  .settings(defaultModuleSettings)
  .settings(scalaPBSettings)
  .settings(
    moduleName := "baker-runtime",
    // we have to exclude the sources because of a compiler bug: https://issues.scala-lang.org/browse/SI-10134
    sources in (Compile, doc) := Seq.empty,
    libraryDependencies ++=
      Seq(
        akkaActor.withDottyCompat(scalaVersion.value),
        akkaPersistence.withDottyCompat(scalaVersion.value),
        akkaPersistenceQuery.withDottyCompat(scalaVersion.value),
        akkaClusterSharding.withDottyCompat(scalaVersion.value),
        akkaInmemoryJournal.withDottyCompat(scalaVersion.value),
        akkaSlf4j.withDottyCompat(scalaVersion.value),
        akkaStream.withDottyCompat(scalaVersion.value),
        chill.withDottyCompat(scalaVersion.value),
        ficusConfig.withDottyCompat(scalaVersion.value),
        catsCore.withDottyCompat(scalaVersion.value),
        catsEffect.withDottyCompat(scalaVersion.value),
        
        "com.thesamet.scalapb" %% "compilerplugin" % scalaPbVersion,
        "com.thesamet.scalapb" %% "scalapb-runtime" % scalaPbVersion % "protobuf",
        protobufJava,
        kryo,
        kryoSerializers,
        slf4jApi,
        findbugs % "provided",
        akkaTestKit.withDottyCompat(scalaVersion.value) % "test",
        akkaStreamTestKit.withDottyCompat(scalaVersion.value) % "test",
        akkaInmemoryJournal.withDottyCompat(scalaVersion.value) % "test",
        akkaPersistenceCassandra.withDottyCompat(scalaVersion.value) % "test",
        scalaTest % "test",
        scalaCheck % "test",
        levelDB % "test",
        levelDBJni % "test",
        betterFiles.withDottyCompat(scalaVersion.value) % "test",
        graphvizJava % "test",
        junitInterface % "test",
        mockito % "test",
        logback % "test")
  )
  .dependsOn(intermediateLanguage, testScope(recipeDsl), testScope(recipeCompiler))

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
  .aggregate(recipeDsl, intermediateLanguage, recipeCompiler) //, runtime)
