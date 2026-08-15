import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.process.CommandLineArgumentProvider

/**
 * Supplies shared fixture directory system properties to test JVMs while fingerprinting directory
 * contents with relative path sensitivity (not absolute path strings).
 */
abstract class FixtureDirectoryArgumentProvider : CommandLineArgumentProvider {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val fixturesDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val schemaFixturesDir: DirectoryProperty

    override fun asArguments(): Iterable<String> =
        listOf(
            "-Djsonapi.fixtures.dir=${fixturesDir.get().asFile.absolutePath}",
            "-Djsonapi.schema.fixtures.dir=${schemaFixturesDir.get().asFile.absolutePath}",
        )
}
