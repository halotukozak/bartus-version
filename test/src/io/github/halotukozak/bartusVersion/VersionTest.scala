package io.github.halotukozak.bartusVersion

import upickle.default.*
import utest.*

object VersionTest extends TestSuite {
  def tests: Tests = Tests {
    "Version" - {
      "correctly bump versions" - {
        val baseVersion = Version(1, 2, 3)

        "Bump major" - {
          "increment major and reset minor/patch" - {
            baseVersion.bump("major") ==> Version(2, 0, 0)
          }
        }

        "Bump minor" - {
          "increment minor and reset patch" - {
            baseVersion.bump("minor") ==> Version(1, 3, 0)
          }
        }

        "Bump patch" - {
          "increment patch version" - {
            baseVersion.bump("patch") ==> Version(1, 2, 4)
          }
        }

        "Invalid bump argument" - {
          "throw RuntimeException" - {
            intercept[RuntimeException] {
              val _ = baseVersion.bump("invalid")
            }
          }
        }

        "Bumping snapshot version" - {
          "throw RuntimeException" - {
            intercept[RuntimeException] {
              val _ = baseVersion.bump("invalid")
            }
          }
        }
      }

      "create snapshots properly" - {
        val baseVersion = Version(1, 2, 3)
        val fullHash = "abcdefghijklmnop"

        "Creating snapshot" - {
          "truncate hash to 7 characters" - {
            val snap = baseVersion.snapshot(fullHash)
            snap.suffix ==> "-abcdefg-SNAPSHOT"
          }

          "maintain shorter hashes" - {
            val snap = baseVersion.snapshot("abcd")
            snap.suffix ==> "-abcd-SNAPSHOT"
          }

          "set distance when provided" - {
            val snap = baseVersion.snapshot(fullHash, Some(5))
            snap.distance ==> Some(5)
          }

          "allow no distance" - {
            val snap = baseVersion.snapshot(fullHash)
            snap.distance ==> None
          }
        }
      }
      "produce correct string representations" - {
        "Release version" - {
          "format correctly" - {
            Version(1, 2, 3).toString ==> "1.2.3"
          }
        }

        "Snapshot version" - {
          "format with distance" - {
            Version(1, 2, 3, Some(5), "-abc123-SNAPSHOT")
              .toString ==> "1.2.3-5-abc123-SNAPSHOT"
          }

          "format without distance" - {
            Version(1, 2, 3, None, "-def456-SNAPSHOT")
              .toString ==> "1.2.3-def456-SNAPSHOT"
          }
        }
      }
      "parse versions from strings" - {
        "Valid release version" - {
          "parse correctly" - {
            Version.of("v1.2.3") ==> Version(1, 2, 3)
          }
        }

        "Valid snapshot version" - {
          "parse with distance and hash" - {
            Version.of("v1.2.3-5-abc1234-SNAPSHOT") ==>
              Version(1, 2, 3, Some(5), "-abc1234-SNAPSHOT")
          }

          "parse with zero distance" - {
            Version.of("v1.2.3-0-abc-SNAPSHOT") ==>
              Version(1, 2, 3, Some(0), "-abc-SNAPSHOT")
          }
        }

        "Invalid versions" - {
          "throw exceptions" - {
            List("g1.2.3", "v1.2", "v1.2.3-SNAPSHOT", "v1.2.x").foreach { invalid =>
              intercept[Exception] {
                val _ = Version.of(invalid)
              }
            }
          }
        }
      }

      "serialize/deserialize correctly" - {
        "Release version" - {
          "round-trip" - {
            val original = Version(1, 2, 3)
            val str = write(original)
            read[Version](str) ==> original
          }
        }

        "Snapshot version" - {
          "round-trip" - {
            val original = Version(1, 2, 3, Some(5), "-abc123-SNAPSHOT")
            val str = write(original)
            read[Version](str) ==> original
          }
        }
      }
    }
  }
}
