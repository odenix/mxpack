# Development

## Build

* Tool: [Gradle](https://gradle.org)
* Prerequisites
  * Windows, macOS, or Linux with JDK 11 or higher on `PATH`
* Config files
  * [gradle/libs.version.toml](gradle/libs.versions.toml)
  * [gradle/wrapper/gradle-wrapper.properties](gradle/wrapper/gradle-wrapper.properties)
  * [gradle.properties](gradle.properties)
  * [settings.gradle.kts](settings.gradle.kts)
  * [benchmark/build.gradle.kts](benchmark/build.gradle.kts)
  * [mxpack-core/build.gradle.kts](mxpack-core/build.gradle.kts)
  * [mxpack-kotlin/build.gradle.kts](mxpack-kotlin/build.gradle.kts)

### Run full build

* `./gradlew build`

### Build libraries

* `./gradlew assemble`
* Sources
  * [mxpack-core/src/main/java/](mxpack-core/src/main/java/)
  * [mxpack-kotlin/src/main/kotlin/](mxpack-kotlin/src/main/kotlin/)
* Outputs
  * [mxpack-core/build/libs/](mxpack-core/build/libs)
  * [mxpack-kotlin/build/libs/](mxpack-kotlin/build/libs)

### Format code

* `./gradlew spotApply`
  
### Run tests

* `./gradlew test`
* Tools
  * [JUnit 5](https://junit.org/junit5/)
  * [jqwik](https://jqwik.net/)
* Sources
  * [mxpack-core/src/test/java/](mxpack-core/src/test/java/)
  * [mxpack-kotlin/src/test/kotlin/](mxpack-kotlin/src/test/kotlin/)
* Outputs
  * [mxpack-core/build/reports/tests/test/index.html](mxpack-core/build/reports/tests/test/index.html)
  * [mxpack-kotlin/build/reports/tests/test/index.html](mxpack-kotlin/build/reports/tests/test/index.html)

### Run benchmarks

* `./gradlew jmh` (check which benchmarks are enabled in config file)
* Tool: [JMH](https://github.com/openjdk/jmh)
* Config file: [benchmark/build.gradle.kts](benchmark/build.gradle.kts) section `jmh {}`
* Sources: [benchmark/src/jmh/java](benchmark/src/jmh/java/)

### Generate API docs

* `./gradlew javadoc`
* `./gradlew dokkaGen`
* Tools
  * Javadoc
  * [Dokka](https://kotlinlang.org/docs/dokka-introduction.html)
* Outputs
  * [mxpack-core/build/docs/javadoc/index.html](mxpack-core/build/docs/javadoc/index.html)
  * [mxpack-kotlin/build/dokka/html/index.html](mxpack-kotlin/build/dokka/html/index.html)

## Website

* Tool: [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)
  * `pip install mkdocs-material`
  * `pip install mkdocs-linkcheck`
* Prerequisites
  * [Python](https://www.python.org/)
  * [pip](https://pip.pypa.io/en/stable/installation/)
* Config file: [mkdocs.yml](mkdocs.yml)
* Sources: [docs/](docs/)

### Preview website

* `mkdocs serve`

### Validate links

* `mkdocs-linkcheck`

## Continuous Integration

* Tool: [GitHub Actions](https://github.com/features/actions)
* Config files: [.github/workflows/](.github/workflows/)

### Delete all workflow runs

Requirements: PowerShell, gh

```shell
gh run list --limit 999 --json databaseId -q '.[].databaseId' |
ForEach-Object {
gh api "repos/$(gh repo view --json nameWithOwner -q .nameWithOwner)/actions/runs/$_" -X DELETE
}
```

## Dependency Updates

* Tool: [Renovate](https://docs.renovatebot.com/)
* Config file: [renovate.json](renovate.json)
