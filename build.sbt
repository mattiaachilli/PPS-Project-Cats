version := "0.1"
lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.13.6",
    name := "PPS-Project-Cats",
    scalacOptions ++= Seq(
      "-Xasync",
      "-feature",
      "-deprecation",
      "-unchecked",
      "-language:postfixOps"
    ),
    libraryDependencies ++= Seq(
      // "junit" % "junit" % "4.12" % Test, // Junit 4
      "org.typelevel" %% "cats-core" % "2.6.1",
      "org.typelevel" %% "cats-effect" % "3.2.9" withSources() withJavadoc(),
      "org.typelevel" %% "cats-free" % "2.6.1",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.6" % Test,
      "org.typelevel" %% "cats-effect-cps" % "0.3.0",
      "io.chrisdavenport" %% "monoids" % "0.2.0",
      "org.typelevel" %% "cats-laws" % "2.6.1",
      "org.typelevel" %% "discipline-munit" % "1.0.6",
      "org.scalamacros" %% "resetallattrs" % "1.0.0",
      "org.typelevel" %% "cats-mtl-core" % "0.7.1",
      "org.http4s" %% "http4s-blaze-server" % "1.0.0-M21",
      "org.http4s" %% "http4s-circe" % "1.0.0-M21",
      "org.http4s" %% "http4s-dsl" % "1.0.0-M21",
      "io.circe" %% "circe-generic" % "0.14.0-M5",
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )