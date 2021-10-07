version := "0.1"

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.12.11",
    name := "PPS-Project-Cats",
    // javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    libraryDependencies ++= Seq(
      // "junit" % "junit" % "4.12" % Test, // Junit 4
      "org.typelevel" %% "cats-effect" % "3.2.9"
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
