package Experiments.File

import Experiments.File.PolymorphicFileUtils.copy
import cats.effect.{ExitCode, IO, IOApp}

import java.io.File
import java.nio.file.{Files, Paths}

object MainAdvancedPolymorphicCopyFiles extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- if (args.length < 2) IO.raiseError(new IllegalArgumentException("Need origin and destination files"))
      else IO.unit
      _ <- if (!Files.exists(Paths.get(args.head))) IO.raiseError(
        new IllegalArgumentException("Files must be exists!")) else IO.unit
      _ <- if (args.head == args.tail.head) IO.raiseError(new IllegalArgumentException("Origin file and destination " +
        "files must be different!")) else IO.unit
      _ <- if (Files.exists(Paths.get(args.tail.head))) IO.println("Override destination file (Y/N)?") >>
        IO.readLine.map(_ != "Y").ifM(IO.canceled, IO.unit) else IO.unit

      originFile = new File(args.head)
      destinationFile = new File(args.tail.head)

      count <- copy[IO](originFile, destinationFile)
      _ <- IO.println(s"$count bytes copied from ${originFile.getPath} to ${destinationFile.getPath}")

    } yield ExitCode.Success
  }
}
