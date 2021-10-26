package Experiments.File

import cats.effect.{IO, Resource}
import cats.implicits.catsSyntaxTuple2Semigroupal

import java.io._

/**
 * Utils method for handle file with Cats-Effect.
 */
object FileUtils {
  private def inputStream(file: File): Resource[IO, FileInputStream] = {
    Resource.make { // Acquire
      IO.blocking(new FileInputStream(file))
    } { dataInputStream => // Release
      IO.blocking(dataInputStream.close()).handleErrorWith(e => IO.println(e.toString))
    }
  }

  private def inputStreamAutoClosable(file: File): Resource[IO, FileInputStream] = {
    Resource.fromAutoCloseable(IO.blocking(new FileInputStream(file)))
  }

  private def outputStream(file: File): Resource[IO, FileOutputStream] = {
    Resource.make { // Acquire
      IO.blocking(new FileOutputStream(file))
    } { dataOutputStream => // Release
      IO.blocking(dataOutputStream.close()).handleErrorWith(e => IO.println(e.toString))
    }
  }

  private def outputStreamAutoClosable(file: File): Resource[IO, FileOutputStream] = {
    Resource.fromAutoCloseable(IO.blocking(new FileOutputStream(file)))
  }

  /**
   * Method that create InputStream and OutputStream from input and output file
   *
   * @param inputFile  file for input
   * @param outputFile file for output
   * @return Resource that encapsulates both resources in a single Resource instance
   */
  def inputOutputStreams(inputFile: File, outputFile: File): Resource[IO, (InputStream, OutputStream)] = {
    for {
      inStream <- inputStream(inputFile)
      outStream <- outputStream(outputFile)
    } yield (inStream, outStream)
  }

  private def transmit(originFile: InputStream, destinationFile: OutputStream, buffer: Array[Byte],
                       acc: Long): IO[Long] = {
    for {
      amount <- IO.blocking(originFile.read(buffer, 0, buffer.length))
      count <- if (amount > -1) IO.blocking(destinationFile.write(buffer, 0, amount)) >> transmit(originFile,
        destinationFile, buffer, acc + amount) else IO.pure(acc)
    } yield count
  }

  private def transfer(originFile: InputStream, destinationFile: OutputStream): IO[Long] = transmit(originFile,
    destinationFile, new Array[Byte](1024 * 10), 0L)

  /**
   * Method that copy data from file to another.
   *
   * @param originFile      where the data is to be copied from
   * @param destinationFile where the data is to be copied
   * @return returns an IO instance with the byte copied.
   */
  def copy(originFile: File, destinationFile: File): IO[Long] = {
    val inIO: IO[InputStream] = IO(new FileInputStream(originFile))
    val outIO: IO[OutputStream] = IO(new FileOutputStream(destinationFile))

    (inIO, outIO) // Getting resources
      .tupled // From (IO[InputStream], IO[OutputStream]) to IO[(InputStream, OutputStream)]
      .bracket {
        case (in, out) => transfer(in, out) // Using resources
      } {
        case (in, out) => // Freeing resources
          (IO(in.close()), IO(out.close()))
            .tupled // From (IO[Unit], IO[Unit]) to IO[(Unit, Unit)]
            .handleErrorWith(_ => IO.unit).void
      }
  }
}
