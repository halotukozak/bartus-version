package io.github.halotukozak.bartusVersion

import mill._
import mill.api.Result
import mill.api.Result.OuterStack
import mill.define.Command
import mill.define.Task.workspace
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryBuilder
import upickle.default._

import scala.util.{Success, Try}

trait BartusVersionModule extends Module {

  import Version._

  /** Main branch to release */
  def mainBranch: T[String] = "main"

  /** The current version obtained from git. */
  def currentVersion: T[Version] = Task {
    val description =
      git().describe().setTarget(mainBranch()).setTags(true).setMatch("v[0-9]*").setAlways(true).setAbbrev(0).call()

    Try(Version.of(description)) match {
      case Success(version) => Result.Success(version)
      case _ =>
        val exception = new IllegalStateException(s"Unexpected git describe output: $description")
        Result.Exception(exception, new OuterStack(exception.getStackTrace.toIndexedSeq))
    }
  }

  def publishVersion: T[String] = Task.Anon {
    currentVersion().toString
  }

  /** The next snapshot version. */
  def nextVersion(bump: String): Command[Version] = Task.Command {
    if (isMainBranch()) currentVersion().bump(bump)
    else currentVersion().snapshot(uncommittedHash())
  }

  /** Set the next version. */
  def setNextVersion(bump: String): Command[Unit] = Task.Command {
    setVersionTask(nextVersion(bump))()
  }

  /** Set the initial version. */
  def setInitialVersion: Task[Unit] =
    setVersionTask(Task.Anon(Version(0, 0, 0)))

  protected def isMainBranch: T[Boolean] = Task.Input {
    val branch = git().getRepository.getBranch
    branch == mainBranch()
  }

  protected def setVersionTask(version: Task[Version]) = Task.Anon {
    T.log.info(generateCommitMessage(version()))
    val tagName = s"v${version()}"
    git().tag().setAnnotated(true).setName(tagName).call()
    git().push()
    ()
  }

  private def git = Task.Anon {
    Git.open(workspace.toIO)
  }

  private def uncommittedHash = Task.Anon {
    val indexCopy = os.temp.dir() / "index"
    val _ = Try(os.copy(os.pwd / ".git" / "index", indexCopy, replaceExisting = true, createFolders = true))

    // Use different index file to avoid messing up current git status
    val altGit = Git.wrap(
      new RepositoryBuilder()
        .setFS(git().getRepository.getFS)
        .setGitDir(git().getRepository.getDirectory)
        .setIndexFile(indexCopy.toIO)
        .build(),
    )
    val cache = altGit.add().addFilepattern(".").call()
    cache.writeTree(altGit.getRepository.newObjectInserter()).abbreviate(hashLength).name()
  }

  private def generateCommitMessage(version: Version): String =
    version match {
      case Version(_, _, _, None, "") => s"Setting release version to $version"
      case _ => s"Setting next version to $version"
    }

  implicit val shellableReadWriter: ReadWriter[os.Shellable] = readwriter[Seq[String]].bimap(_.value, os.Shellable(_))

  implicit val procReadWriter: ReadWriter[os.proc] = readwriter[Seq[os.Shellable]].bimap(_.command, os.proc(_))
}
