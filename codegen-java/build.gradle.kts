import org.pkl.core.ImportGraph
import org.pkl.gradle.task.AnalyzeImportsTask
import org.pkl.gradle.task.JavaCodeGenTask
import kotlin.io.path.readText

plugins {
  // apply the Pkl plugin
  id("org.pkl-lang") version ("0.30.0")
  // if the `idea` plugin is applied, the Pkl plugin makes generated code visible to IntelliJ IDEA
  idea
  `java-library`
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.pkl-lang:pkl-config-java:0.30.0")
}

// Register a code generator named "configClasses".
// This adds a task with the same name.
//
// The generated classes are automatically added to the `main` source set,
// which means they are accessible from production and test code.
// Alternatively, a different `sourceSet` can be configured.
//
// By default, generated classes are written to `outputDir` "$projectDir/generated/$generatorName".
// This tends to work better for IDEs than writing generated classes
// to the build output directory, which is typically excluded by IDEs.
pkl {
  javaCodeGenerators {
    register("configClasses") {
      // rest of this task is configured down below
      generateJavadoc.set(true)
    }
  }
  analyzers.imports {
    register("birdsImports") {
      sourceModules.set(files("src/main/resources/Birds.pkl"))
      outputFile = layout.buildDirectory.file("pkl/birdsImports.json")
      outputFormat = "json"
    }
  }
  evaluators {
    // Create an evaluator that evaluates the application's runtime configuration.
    // This creates an equally named task called "testPklConfig".
    //
    // This task is registered as a dependency to `check`, so that Pkl modules are checked for
    // correctness as part of a project's tests.
    register("testPklConfig") {
      sourceModules.set(files("src/main/resources/config.pkl"))
      modulePath.from(sourceSets.main.get().runtimeClasspath)
      outputFile.set(file("${layout.buildDirectory.get()}/testPklConfig/config"))
    }
  }
}

val configClasses by tasks.existing(JavaCodeGenTask::class) {
  val birdsImportsTask = tasks.named("birdsImports", AnalyzeImportsTask::class)
  dependsOn(birdsImportsTask)
  sourceModules = birdsImportsTask.map { task ->
    val outputPath = task.outputFile.get().asFile.toPath()
    val importGraph = ImportGraph.parseFromJson(outputPath.readText())
    importGraph.resolvedImports.values.filter { uri ->
      uri.scheme == "file" || uri.scheme == "package"
    }
  }
}

tasks.check {
  dependsOn(tasks.named("testPklConfig"))
}

// Runs this example.
// This task is specific to this project and not generally required.
val runExample by tasks.registering(JavaExec::class) {
  mainClass.set("example.JavaCodeGeneratorExample")
  classpath = sourceSets.main.get().runtimeClasspath
}

tasks.check {
  dependsOn(runExample)
}
