package io.github.halotukozak.bartusVersion

import upickle.default.*

final case class Version(major: Int, minor: Int, patch: Int, distance: Option[Int] = None, suffix: String = "") {

  import Version.*

  def bump(segment: String): Version = segment match {
    case _ if suffix != "" => throw new RuntimeException("Cannot bump a snapshot version")
    case "major" => this.copy(major + 1, 0, 0)
    case "minor" => this.copy(major, minor + 1, 0)
    case "patch" => this.copy(major, minor, patch + 1)
    case _ => throw new RuntimeException(s"Valid arguments for bump are: major, minor, patch")
  }

  def snapshot(hash: String, distance: Option[Int] = None): Version =
    this.copy(distance = distance, suffix = s"-${hash.take(hashLength)}-SNAPSHOT")

  override def toString: String = {
    val version = s"v$major.$minor.$patch"
    val distanceStr = distance.fold("")(d => s"-$d")
    version + distanceStr + suffix
  }
}

object Version {
  private final val releaseRegex = """^v(\d+)\.(\d+)\.(\d+)$""".r
  private final val snapshotRegex = """^v(\d+)\.(\d+)\.(\d+)(?:-(\d+)-([\da-f]+)-SNAPSHOT)?$""".r

  final val hashLength: Int = 7

  private def release(major: String, minor: String, patch: String) = Version(major.toInt, minor.toInt, patch.toInt)

  def of(str: String): Version = str match {
    case releaseRegex(major, minor, patch) => Version.release(major, minor, patch)
    case snapshotRegex(major, minor, patch, distance, hash) =>
      Version.release(major, minor, patch).snapshot(hash.take(hashLength), Some(distance.toInt))
    case _ =>
      throw new IllegalArgumentException(s"Invalid version string: $str")
  }

  implicit val readWriter: ReadWriter[Version] = implicitly[ReadWriter[String]].bimap(_.toString, Version.of)
}
