version := "0.1"

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.12.11",
    name := "PPS-Project-Cats",
    // javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    libraryDependencies ++= Seq(
      // "junit" % "junit" % "4.12" % Test, // Junit 4
      "org.typelevel" %% "cats-effect" % "3.2.9",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.6" % Test,
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
