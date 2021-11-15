package snippets.typeclasses

object TypeclassExample extends App {
  case class StudentId(id: Int)
  case class StaffId(id: Int)
  case class Score(s: Double)

  trait Printer[A] {
    def getString(a: A): String
  }

  implicit val studentPrinter: Printer[StudentId] = new Printer[StudentId] {
    override def getString(a: StudentId): String = s"StudentId: ${a.id}"
  }

  implicit val staffPrinter: Printer[StaffId] = new Printer[StaffId] {
    override def getString(a: StaffId): String = s"StaffId: ${a.id}"
  }

  implicit val scorePrinter: Printer[Score] = new Printer[Score] {
    override def getString(a: Score): String = s"StudentId: ${a.s}%"
  }

  def show[A](a: A)(implicit printer: Printer[A]): String = printer.getString(a)

  val firstStudent = StudentId(1)
  print(show[StudentId](firstStudent))
}
