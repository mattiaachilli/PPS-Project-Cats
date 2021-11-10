package experiments.file

import cats.effect._
import cats.syntax.all._

import java.io._

object PolymorphicFileUtils {

  private val BUFFER_SIZE = 1024 * 10

  def inputStream[F[_] : Sync](file: File): Resource[F, FileInputStream] = {
    Resource.make { // Acquire
      Sync[F].blocking(new FileInputStream(file))
    } { dataInputStream => // Release
      Sync[F].blocking(dataInputStream.close()).handleErrorWith(_ => Sync[F].unit)
    }
  }

  def outputStream[F[_] : Sync](file: File): Resource[F, FileOutputStream] = {
    Resource.make { // Acquire
      Sync[F].blocking(new FileOutputStream(file))
    } { dataOutputStream => // Release
      Sync[F].blocking(dataOutputStream.close()).handleErrorWith(_ => Sync[F].unit)
    }
  }

  def inputOutputStreams[F[_] : Sync](inputFile: File, outputFile: File): Resource[F, (InputStream, OutputStream)] = {
    for {
      inStream <- inputStream(inputFile)
      outStream <- outputStream(outputFile)
    } yield (inStream, outStream)
  }

  private def transmit[F[_] : Sync](originFile: InputStream, destinationFile: OutputStream, buffer: Array[Byte],
                                    acc: Long): F[Long] = {
    for {
      amount <- Sync[F].blocking(originFile.read(buffer, 0, buffer.length))
      count <- if (amount > -1) Sync[F].blocking(destinationFile.write(buffer, 0, amount)) >> transmit(originFile,
        destinationFile, buffer, acc + amount) else Sync[F].pure(acc)
    } yield count
  }

  private def transfer[F[_] : Sync](originFile: InputStream, destinationFile: OutputStream, bufferSize: Int): F[Long] =
    transmit(originFile, destinationFile, new Array[Byte](bufferSize), 0L)

  def copy[F[_] : Sync](originFile: File, destinationFile: File, bufferSize: Option[Int] = None): F[Long] = {
    inputOutputStreams(originFile, destinationFile).use { case (in, out) =>
      transfer(in, out, bufferSize.getOrElse(BUFFER_SIZE))
    }
  }
}
