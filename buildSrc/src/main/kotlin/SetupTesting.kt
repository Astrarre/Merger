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

fun Project.setupTesting() {
    val destination = sourceSets["test"].resources.srcDirs.first()
    val originalJar = tasks.create("originalJar", Jar::class.java) {
        group = "testing"
        from(sourceSets["testOriginalJar"].output)

        destinationDirectory.set(destination)
        archiveBaseName.set("original-jar")
    }

    val patchJar = tasks.create("patchJar", Jar::class.java) {
        group = "testing"
        from(sourceSets["testPatchJar"].output)

        destinationDirectory.set(destination)
        archiveBaseName.set("patch-jar")
    }

    tasks.withType<Test> {
        dependsOn(originalJar, patchJar)
    }


}

val Project.sourceSets: SourceSetContainer
    get() = the<JavaPluginConvention>().sourceSets