package pl.touk.esp.engine.compile

import cats.{Applicative, SemigroupK, Traverse, _}
import cats.data.{NonEmptyList, _}

import scala.language.{higherKinds, reflectiveCalls}

class ValidatedSyntax[Err] {

  implicit val nelSemigroup: Semigroup[NonEmptyList[Err]] =
    SemigroupK[NonEmptyList].algebra[Err]

  // Using Cartesian syntax causes IntelliJ idea errors
  val A = Applicative[({type V[B] = ValidatedNel[Err, B]})#V]

  // Using travers syntax causes IntelliJ idea errors
  implicit class ValidationTraverseOps[T[_]: Traverse, B](traverse: T[ValidatedNel[Err, B]]) {
    def sequence: ValidatedNel[Err, T[B]] =
      Traverse[T].sequence[({type V[C] = ValidatedNel[Err, C]})#V, B](traverse)
  }
}

object ValidatedSyntax {

  def apply[Err]: ValidatedSyntax[Err] =
    new ValidatedSyntax[Err]

}