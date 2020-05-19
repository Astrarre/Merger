import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

class SetupTesting : Plugin<Project> {
    override fun apply(project: Project) {
        project.setupTesting()
    }
}

private fun Project.createTestSourceSet(name: String): Jar {
    val sourceSet = sourceSets.create(name)
    return tasks.create(name, Jar::class.java) {
        group = "testing"
        from(sourceSet.output)

        destinationDirectory.set(sourceSets["test"].resources.srcDirs.first())
        archiveFileName.set("$name.jar")
    }
}

fun Project.setupTesting() {
    val original = createTestSourceSet("testOriginalJar")
    val patch = createTestSourceSet("testPatchJar")
    val erroring = createTestSourceSet("testErrorPatchJar")

    sourceSets["testPatchJar"].compileClasspath += sourceSets["main"].output
    sourceSets["testErrorPatchJar"].compileClasspath += sourceSets["main"].output

    tasks.named("processTestResources") {
        dependsOn(original, patch, erroring)
    }
}

val Project.sourceSets: SourceSetContainer
    get() = the<JavaPluginConvention>().sourceSets