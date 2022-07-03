import sbt._

//noinspection TypeAnnotation
object Dependencies {

  val akkaVersion = "2.6.19"

  val typeSafeConfig =            "com.typesafe"               %  "config"                             % "1.4.2"

  val mockito =                   "org.mockito"                %  "mockito-all"                        % "1.10.19"
  val junitInterface =            "com.novocode"               %  "junit-interface"                    % "0.11"

  val akkaActorTyped           = "com.typesafe.akka"        %% "akka-actor-typed"           % akkaVersion
  val akkaPersistence          = "com.typesafe.akka"        %% "akka-persistence-typed"     % akkaVersion
  val akkaPersistenceJdbc      = "com.lightbend.akka"       %% "akka-persistence-jdbc"      % "5.0.4"         // no scala 3
  val akkaStream               = "com.typesafe.akka"        %% "akka-stream"                % akkaVersion
  val akkaPersistenceQuery     = "com.typesafe.akka"        %% "akka-persistence-query"     % akkaVersion
  val akkaSerializationJackson = "com.typesafe.akka"        %% "akka-serialization-jackson" % akkaVersion

  val akkaCluster =               "com.typesafe.akka"          %% "akka-cluster"                       % akkaVersion
  val akkaClusterSharding =       "com.typesafe.akka"          %% "akka-cluster-sharding"              % akkaVersion
  val akkaSlf4j =                 "com.typesafe.akka"          %% "akka-slf4j"                         % akkaVersion
  val akkaTestKit =               "com.typesafe.akka"          %% "akka-testkit"                       % akkaVersion
  val akkaStreamTestKit =         "com.typesafe.akka"          %% "akka-stream-testkit"                % akkaVersion
  val akkaMultiNodeTestkit =      "com.typesafe.akka"          %% "akka-multi-node-testkit"            % akkaVersion

  val levelDB   =                 "org.iq80.leveldb"           %  "leveldb"                            % "0.12"
  val levelDBJni =                "org.fusesource.leveldbjni"  %  "leveldbjni-all"                     % "1.8"

  val logback =                   "ch.qos.logback"             %  "logback-classic"                    % "1.2.11"
  val ficusConfig =               "com.iheart"                 %% "ficus"                              % "1.5.2"
  
  val scalaGraphDot =             "org.scala-graph"            %% "graph-dot"                          % "1.13.0"
  val graphvizJava =              "guru.nidi"                  %  "graphviz-java"                      % "0.18.0"
  
  val catsEffect =                "org.typelevel"              %% "cats-effect"                        % "3.3.12"
  val catsCore =                  "org.typelevel"              %% "cats-core"                          % "2.7.0"

  val liftJson =                  "net.liftweb"                %% "lift-json"                          % "3.3.0"
  
  val findbugs =                  "com.google.code.findbugs"   %  "jsr305"                             % "3.0.2"

  val protobufJava =              "com.google.protobuf"        % "protobuf-java"                       % "3.14.0"

  val slf4jApi =                  "org.slf4j"                  %  "slf4j-api"                          % "1.7.36"
  val scalaTest =                 "org.scalatest"              %% "scalatest"                          % "3.2.12"
  val scalaCheck =                "org.scalacheck"             %% "scalacheck"                         % "1.15.3"
  val scalaTestCheck =            "org.scalatestplus"          %% "scalacheck-1-15"                    % "3.2.11.0"

  def scopeDeps(scope: String, modules: Seq[ModuleID]) =  modules.map(m => m % scope)
  def compileDeps(modules: ModuleID*) = modules.toSeq
  def testDeps(modules: ModuleID*) = scopeDeps("test", modules)

  def providedDeps(modules: ModuleID*) = scopeDeps("provided", modules)
}
