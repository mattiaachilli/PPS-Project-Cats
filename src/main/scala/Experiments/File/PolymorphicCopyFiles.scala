package Experiments.File

import Experiments.File.PolymorphicFileUtils.copy
import cats.effect.{ExitCode, IO, IOApp}

import java.io.File

object PolymorphicCopyFiles extends IOApp{
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- if (args.length < 2) IO.raiseError(new IllegalArgumentException("Need origin and destination files"))
      else IO.unit
      originFile = new File(args.head)
      destinationFile = new File(args.tail.head)
      count <- copy[IO](originFile, destinationFile)
      _ <- IO.println(s"$count bytes copied from ${originFile.getPath} to ${destinationFile.getPath}")

    } yield ExitCode.Success
  }
}
