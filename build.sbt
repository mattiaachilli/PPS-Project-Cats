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
      "org.typelevel" %% "cats-core" % "2.3.0",
      "org.typelevel" %% "cats-effect" % "3.2.9" withSources() withJavadoc(),
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.6" % Test,
      "org.typelevel" %% "cats-effect-cps" % "0.3.0",
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
