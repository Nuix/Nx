import java.nio.file.Paths
import kotlin.io.path.pathString

/*
External Configuration Properties
=================================
Below values can be overridden when invoking gradle build using property arguments, for example:

./gradlew build -Pversion=2.0

                  group => The group ID
                version => The version
       targetJreVersion => Version number assigned to sourceCompatibility and targetCompatibility
          nuixEngineDir => Overrides value to engine release otherwise pulled from ENV var NUIX_ENGINE_DIR
                tempDir => Used to override temp directory for testing which would otherwise default to dir in localappdata
            testDataDir => Directory tests can load test data they depend on from
  rubyExamplesDirectory => Directory containing Ruby script files used to test various aspects from scripting
testOutputDirectoryRoot => Root directory where tests may write data while running.  Each test run will create a timestamp subdirectory
           nuixUsername => Username used to authenticate with CLS (Cloud License Server).  Otherwise, would be pulled from ENV var NUIX_USERNAME
           nuixPassword => Password used to authenticate with CLS (Cloud License Server).  Otherwise, would be pulled from ENV var NUIX_PASSWORD
*/

plugins {
    id("java")
}

group = findProperty("group") ?: "com.nuix.nx"
version = findProperty("version") ?: "1.19.0"

val sourceCompatibility = findProperty("targetJreVersion") ?: 11
val targetCompatibility = findProperty("targetJreVersion") ?: 11

// Directory containing Nuix Engine release.  We first attempt to pull from ENV
var nuixEngineDirectory: String = System.getenv("NUIX_ENGINE_DIR")
// If we have it provided externally via a property, use that instead
if (properties.containsKey("nuixEngineDir")) {
    nuixEngineDirectory = findProperty("nuixEngineDir").toString()
}
// Finally, if we don't have a value, throw exception since we require this
println("NUIX_ENGINE_DIR: ${nuixEngineDirectory}")
if (nuixEngineDirectory.isEmpty()) {
    throw InvalidUserDataException("Please populate the environment variable 'NUIX_ENGINE_DIR' with directory containing a Nuix Engine release")
}

val engineLibDir = Paths.get(nuixEngineDirectory, "lib").pathString
println("engineLibDir: ${engineLibDir}")

repositories {
    mavenCentral()
}

// We can use this to define JAR files that we need copied to lib
val externalDependency: Configuration by configurations.creating {
    isTransitive = true
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.1")

    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    implementation(fileTree(baseDir = engineLibDir) {
        include(
                "**/*log*.jar",
                "**/*aspect*.jar",
                "**/*joda*.jar",
                "**/*commons*.jar",
                "**/*guava*.jar",
                "**/*gson*.jar",
                "**/nuix-*.jar",
                "**/*jruby*.jar",
                "**/*swing*.jar",
                "**/*jide*.jar",
                "**/*csv*.jar",
                "**/*beansbinding*.jar",
                "**/*xml.bind*.jar",
                "**/*itext*.jar",
        )
    })

    testRuntimeOnly(fileTree(baseDir = engineLibDir) {
        include("*.jar")
    })
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

fun configureTestEnvironment(test: Test) {
    println("Running 'configureTestEnvironment'...")

    // Engine runtime temp directory
    val nuixTempDirectory = findProperty("tempDir")
            ?: Paths.get(System.getenv("LOCALAPPDATA"), "Temp", "Nuix").pathString

    // Args passed to JVM running tests
    test.jvmArgs(
            "--add-exports=java.base/jdk.internal.loader=ALL-UNNAMED",  // Engine 9.6(?) and later require this
            "-Xmx4G",
            "-Djava.io.tmpdir=\"${nuixTempDirectory}\"",
            // "-verbose:class" // Can help troubleshoot weird dependency issues
    )

    // Directory used to store data a test may rely on (like sample data)
    val testDataDirectory = findProperty("testDataDir") ?: Paths.get("$projectDir", "..", "TestData").pathString

    // Directory used to store data a test may rely on (like sample data)
    val rubyExamplesDirectory = findProperty("rubyExamplesDirectory")
            ?: Paths.get("$projectDir", "..", "Examples").pathString

    // Directory that tests may write data to, unique to each test invocation
    val testOutputDirectoryRoot = findProperty("testOutputDirectoryRoot")
            ?: Paths.get("$projectDir", "..", "TestOutput").pathString
    val testOutputDirectory = Paths.get(testOutputDirectoryRoot.toString(), "${System.currentTimeMillis()}").pathString

    val binDir = Paths.get(nuixEngineDirectory, "bin").pathString
    val binX86Dir = Paths.get(nuixEngineDirectory, "bin", "x86").pathString

    // Configure ENV vars for JVM tests run in
    test.setEnvironment(
            // Add our engine release's bin and bin/x86 to PATH
            Pair("PATH", "${System.getenv("PATH")};${binDir};${binX86Dir}"),

            // Define where tests can place re-usable test data
            Pair("TEST_DATA_DIRECTORY", testDataDirectory),

            // Define where tests can write output produce for later review
            Pair("TEST_OUTPUT_DIRECTORY", testOutputDirectory),

            // Defines where example ruby scripts live
            Pair("RUBY_EXAMPLES_DIRECTORY", rubyExamplesDirectory),

            // Forward ENV username and password
            Pair("NUIX_USERNAME", findProperty("nuixUsername") ?: System.getenv("NUIX_USERNAME")),
            Pair("NUIX_PASSWORD", findProperty("nuixPassword") ?: System.getenv("NUIX_PASSWORD")),

            // Forward LOCALAPPDATA and APPDATA
            Pair("LOCALAPPDATA", System.getenv("LOCALAPPDATA")),
            Pair("APPDATA", System.getenv("APPDATA")),

            // We need to make sure we set these so workers will properly resolve temp dir
            // (when using a worker based operation via EngineWrapper).
            Pair("TEMP", nuixTempDirectory),
            Pair("TMP", nuixTempDirectory),

            Pair("NUIX_ENGINE_DIR", nuixEngineDirectory)
    )
}

// Builds JAR without any of the Engine wrapper stuff included
tasks.create<Jar>("nxOnlyJar") {
    println("Producing NX only JAR file")
    from(sourceSets.main.get().output)
    exclude("com/nuix/innovation/enginewrapper/**")
    destinationDirectory.set(Paths.get("$projectDir", "..", "JAR").toFile())
}

// Copies plug-in JAR to lib directory of engine release we're running against
tasks.register<Copy>("copyJarsToEngine") {
    val name = "@nx"
    val jarName = "${name}.jar"
    dependsOn(tasks.findByName("nxOnlyJar"))

    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.INCLUDE

    doFirst {
        val toDelete = File(engineLibDir, jarName)
        println("Deleting: " + toDelete.absolutePath)
        delete(toDelete)
    }

    println("Copying files engine to engine lib dir...")
    copy {
        into(File(engineLibDir))
        from(configurations.findByName("externalDependency"))
        rename("(.*)\\.jar", "${name}-Dependency-$1.jar")
    }

    copy {
        from(tasks.findByName("nxOnlyJar"))
        into(File(engineLibDir))
        rename(".*\\.jar", jarName)
    }
}

// Ensure that tests are ran by JUnit and that test environment gets configured
tasks.test {
    dependsOn(tasks.findByName("copyJarsToEngine"))
    useJUnitPlatform()
    configureTestEnvironment(this)
}

// Customize where Javadoc output is written to
tasks.getByName<Javadoc>("javadoc") {
    setDestinationDir(Paths.get("$projectDir", "..", "docs").toFile())
}