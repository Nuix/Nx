import java.net.URI
import java.nio.file.Files
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
        artifactoryRepo => The internal artifactory repo used to publish team packages.  Publish defaults to the snapshot repo
        ArtifactoryUser => Username to authorize publishing to the Artifactory repo.  If not present as a property, get it from an environment variable,  If not present there then don't publish
       ArtifactoryToken => API Token used to authorize publishing to the Artifactory repo.  If not present as a property, get it from an environment variable.  If not present there then don't publish
     nuixDependencyRepo => Optional repository used to download dependencies when the nuixEngineDir is not provided.
         nuixEngineRepo => Optional repository to download the Nuix Engine distribution from
           nuixEngineOs => If using a repository for the engine, the OS for the engine.  Acceptable options are `linux` or `windows` and defaults to `windows`
      nuixEngineRelease => If using a repository for the engine, release version of the engine to use, in the form <major>.<minor>.<point>.<build>.  Default to 9.10.17.1073

There are two main alternate routes run these builds:
1) Provide a path to a Nuix Workstation build
    * Either provide a NUIX_ENGINE_DIR environment variable
    * Or run with `gradlew -PnuixEngineDir="..." ...`
    * The path must be to a Nuix Engine or Nuix Workstation path that includes GUI JAR files.  Normally, the Nuix Engine Distribution does not
2) Get the Nuix Engine and dependencies from a maven repository
    * Pass in the nuixDependencyRepo where some dependencies, like the GUI dependencies will be found.  If this isn't provided, mavenCentral will be used
    * Also pass in the nuixEngineRepo where the Nuix Engine will be downloaded from.
    * An example might be `gradlew -PnuixEngineRepo=https://my.artifactory.com/nuix-engine` or `gradlew -PnuixDependencyRepo=https://my.artifactory.com/nuix-dependencies -PnuixEngineRepo=https://my.artifactory.com/nuix-engine`


*/

plugins {
    id("java")
    id("maven-publish")
    id("com.jfrog.artifactory") version "5.+"
}

// Aparently in Kotlin, or this project in particular, findProperty may return an empty String for the group and version
group = findProperty("group") ?: "com.nuix.nx"
group = if (group.toString().length == 0) "com.nuix.nx" else group.toString()
// Add SNAPSHOT to default version number so a unique version will be created when publishing from an IDE
version = findProperty("version") ?: "1.19.0-SNAPSHOT"
version = if(version.toString().isEmpty() || "unspecified" == version.toString()) "1.19.0-SNAPSHOT" else version.toString()
println("Group: ${group} Version: ${version}")

val sourceCompatibility = findProperty("targetJreVersion") ?: 11
val targetCompatibility = findProperty("targetJreVersion") ?: 11

// The artifactory repo used for publishing.  Default to the snapshot repo when building from IDE, override to move to dev
val publish_artifactory_repo = findProperty("artifactoryRepo") ?: "innovation-proserv-snapshot-local"

// Directory containing Nuix Engine release.  We first attempt to pull from ENV
var nuixEngineDirectory: String? = System.getenv("NUIX_ENGINE_DIR")
// If we have it provided externally via a property, use that instead
if (properties.containsKey("nuixEngineDir")) {
    nuixEngineDirectory = findProperty("nuixEngineDir").toString()
}

// Alternatively, provide a repo where we can get dependencies from
var useRepository: Boolean = (null == nuixEngineDirectory || nuixEngineDirectory?.isEmpty() == true)

var nuixArtifactRepo: String? = null
if(properties.containsKey("nuixRepo")) {
    nuixArtifactRepo = findProperty("nuixRepo").toString()
}

// Finally, if we don't have a value, throw exception since we require this
println("NUIX_ENGINE_DIR: ${nuixEngineDirectory}\tNuix Repo: ${nuixArtifactRepo}")

if (useRepository && (null == nuixArtifactRepo || nuixArtifactRepo?.isEmpty() == true)) {
    throw InvalidUserDataException("Please populate the environment variable 'NUIX_ENGINE_DIR' with directory containing a Nuix Workstation release" +
            " or provide a property 'nuixRepo' that can be used to get dependencies.")
}

var engineLibDir = Paths.get(nuixEngineDirectory, "lib").pathString
println("engineLibDir: ${engineLibDir}")

if (useRepository) {
    downloadEngineIfNeeded()
}

repositories {
    if(useRepository) {
        maven {
            url = URI(nuixArtifactRepo)
        }
    }

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

    if(useRepository) {
        compileOnly("org.swinglabs.swingx:swingx-core:1.6.6-N1.2")
        compileOnly("com.jidesoft:jide-grids:3.7.10")
    }

    testRuntimeOnly(fileTree(baseDir = engineLibDir) {
        include("*.jar")
    })
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

fun downloadEngineIfNeeded() {
    println("Making the Engine")
    if(useRepository) {
        println("No Engine Path, downloading a new one.")

        val engineDownloadDir = file("engine/download")
        Files.createDirectories(engineDownloadDir.toPath())

        val engineDownloadUrl = "https://artifactory.uat.nuix.com/artifactory/builds-syd/nuix-engine"
        val majorMinorVersion = "9.10"
        val os = "win32-amd64"
        val pointVersion = "17.1073"
        val extension = "zip"

        val engineFileName = "engine-dist-${os}-${majorMinorVersion}.${pointVersion}.${extension}"
        val engineDownloadDestination = file("${engineDownloadDir}/${engineFileName}")
        println("Downloading Engine to ${engineDownloadDestination}")

        if (!engineDownloadDestination.exists()) {
            val engineDownloadSource = "${engineDownloadUrl}/${majorMinorVersion}/${engineFileName}"
            println("Downloading Engine from ${engineDownloadSource}")

            ant.invokeMethod("get", mapOf("src" to engineDownloadSource, "dest" to engineDownloadDestination))
            println("Engine Download Complete")
        } else {
            println("Engine already downloaded.  Skipping download step")
        }

        val unpackedEngine = file("engine/release")

        println("Unpacking Engine to ${unpackedEngine}")
        if (unpackedEngine.exists()) {
            println("Existing Engine being removed")
            unpackedEngine.deleteRecursively()
        }

        copy {
            from(zipTree(engineDownloadDestination.path))
            into(unpackedEngine.path)
        }
        println("Engine Unpacked.")

        nuixEngineDirectory = file("engine/release").absolutePath.toString()
        engineLibDir = Paths.get(nuixEngineDirectory, "lib").pathString
        println("Engine Directory: ${nuixEngineDirectory}")
        println("Engine Lib Dir: ${engineLibDir}")
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

publishing {
    publications {
        create<MavenPublication>("mavenJava"){
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}

artifactory {
    clientConfig.setIncludeEnvVars(false)

    val user = findProperty("ArtifactoryUser") ?: System.getenv("ArtifactoryUser")
    val token = findProperty("ArtifactoryToken") ?: System.getenv("ArtifactoryToken")

    publish {
        contextUrl = "https://artifactory.uat.nuix.com/artifactory"

        if (null != user && null != token) {
            repository {
                repoKey = publish_artifactory_repo.toString()
                username = user.toString()
                password = token.toString()
            }

            defaults {
                publications("mavenJava")
                setPublishArtifacts(true)
                isPublishBuildInfo = false
                setPublishPom(true)
                setPublishIvy(false)
            }
        } else {
            println("Package not published: No Artifactory Credentials")
        }
    }
}

tasks.artifactoryDeploy {
    val user = findProperty("ArtifactoryUser") ?: System.getenv("ArtifactoryUser")
    val token = findProperty("ArtifactoryToken") ?: System.getenv("ArtifactoryToken")
    onlyIf {
        null != user && null != token
    }

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