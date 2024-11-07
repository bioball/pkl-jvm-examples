import org.pkl.gradle.task.EvalTask
import org.pkl.gradle.task.PkldocTask
import org.pkl.gradle.task.ProjectPackageTask
import kotlin.io.path.readText

plugins {
  // apply the Pkl plugin
  id("org.pkl-lang") version("0.27.0")
  base
}

val fakeModuleCacheDir = layout.buildDirectory.dir("pkl-cache")

pkl {
  evaluators {
    register("evalPackageUri") {
      sourceModules = files(layout.projectDirectory.file("src/PklProject"))
      expression = "package.uri"
      outputFile = layout.buildDirectory.file("pkl/packageUri.txt")
    }
  }
  project {
    packagers {
      register("createBirdsPackage") {
        projectDirectories.from(layout.projectDirectory.dir("src/"))
      }
    }
  }
  pkldocGenerators {
    register("pkldoc") {
      moduleCacheDir = fakeModuleCacheDir
      // workaround for a bug where the import graph analyzer tries to resolve imports of a package
      transitiveModules.from(file("src/PklProject"))
      outputDir = layout.buildDirectory.dir("pkldoc")
    }
  }
}

val evalPackageUri by tasks.existing(EvalTask::class)

val pkldoc by tasks.existing(PkldocTask::class) {
  dependsOn(evalPackageUri)
  dependsOn(prepareCacheDir)
  sourceModules.set(evalPackageUri.map { listOf(it.outputs.files.singleFile.toPath().readText()) })
}

repositories { mavenCentral() }

val createBirdsPackage by tasks.existing(ProjectPackageTask::class)

// Create a cache dir so pkldoc can create documentation without them needing to be published
val prepareCacheDir by tasks.registering(Copy::class) {
  dependsOn(createBirdsPackage)
  dependsOn(evalPackageUri)
  from(createBirdsPackage.get().outputPath)
  into(fakeModuleCacheDir.map {
    val packageUri = evalPackageUri.get().outputFile.get().asFile.toPath().readText()
    it.dir(packageUri.replace("package://", "package-2/"))
  })
  rename { file ->
    when  {
      file.endsWith(".sha256") || file.endsWith(".zip") -> file
      else -> "$file.json"
    }
  }
}
