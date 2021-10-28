package Experiments.File

import Experiments.File.FileUtils.copy
import cats.effect.{ExitCode, IO, IOApp}

import java.io.File

/**
 * The goal is to create a program that copies files using Cats-Effect and FP.
 */
object MainSimpleCopyFiles extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- if (args.length < 2) IO.raiseError(new IllegalArgumentException("Need origin and destination files"))
      else IO.unit
      originFile = new File(args.head)
      destinationFile = new File(args.tail.head)
      count <- copy(originFile, destinationFile)
      _ <- IO.println(s"$count bytes copied from ${originFile.getPath} to ${destinationFile.getPath}")

    } yield ExitCode.Success
  }
}
