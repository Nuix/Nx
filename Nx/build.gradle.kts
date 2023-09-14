import java.net.URI
import java.nio.file.Paths
import kotlin.io.path.pathString
import com.nuix.nx.build.EnvironmentConfiguration /* <-- In the buildSrc module */

/*
External Configuration Properties
=================================
Below values can be overridden when invoking gradle build using property arguments, for example:

./gradlew build -Pversion=2.0

                  group => The group ID
                version => The version
       targetJreVersion => Version number assigned to sourceCompatibility and targetCompatibility
          nuixEngineDir => Overrides value to engine release otherwise pulled from ENV var NUIX_ENGINE_DIR
            userDataDir => Directory to use for the user data directory.  Default to %NUIX_ENGINE_DIR%/user-data if not set
                tempDir => Used to override temp directory for testing which would otherwise default to dir in localappdata
            testDataDir => Directory tests can load test data they depend on from
  rubyExamplesDirectory => Directory containing Ruby script files used to test various aspects from scripting
testOutputDirectoryRoot => Root directory where tests may write data while running.  Each test run will create a timestamp subdirectory
           nuixUsername => Username used to authenticate with CLS (Cloud License Server).  Otherwise, would be pulled from ENV var NUIX_USERNAME
           nuixPassword => Password used to authenticate with CLS (Cloud License Server).  Otherwise, would be pulled from ENV var NUIX_PASSWORD
           artifactRepo => The internal artifactory repo used to publish team packages.  Publish defaults to the snapshot repo
           artifactUser => Username to authorize publishing to the Artifactory repo.  If not present as a property, get it from an environment variable,  If not present there then don't publish
          artifactToken => API Token used to authorize publishing to the Artifactory repo.  If not present as a property, get it from an environment variable.  If not present there then don't publish
     nuixDependencyRepo => Optional repository used to download dependencies when the nuixEngineDir is not provided.

*/
val configs = EnvironmentConfiguration(project.properties)

plugins {
    id("java")
    id("maven-publish")
    id("com.jfrog.artifactory") version "5.+"
}

group = configs.baseConfigs.groupName
version = configs.baseConfigs.versionString
println("Group: ${group} Version: ${version}")

val sourceCompatibility = configs.baseConfigs.targetJreVersion
val targetCompatibility = configs.baseConfigs.targetJreVersion

println("NUIX_ENGINE_DIR: ${configs.nuixEngineDirectory}\tNuix Repo: ${configs.engineDistro.nuixEngineRepo}")

val tasksThatDontNeedDependencies = listOf("clean", "jar", "artifactoryPublish")
val needDependencies = gradle.startParameter.taskNames.any { !tasksThatDontNeedDependencies.contains(it) };
if (configs.nuixEngineDirectory.isNullOrEmpty() && needDependencies) {
    throw InvalidUserDataException("Please populate the environment variable 'NUIX_ENGINE_DIR' with directory " +
            "containing a Nuix Workstation release")
}

repositories {
    if(needDependencies && !configs.artifactory.dependencyRepository.isNullOrEmpty()) {
        maven {
            url = URI(configs.artifactory.dependencyRepository)
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

        implementation(fileTree(baseDir = configs.nuixEngineLib) {
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

        if (!configs.artifactory.dependencyRepository.isNullOrEmpty()) {
            compileOnly("org.swinglabs.swingx:swingx-core:1.6.6-N1.2")
            compileOnly("com.jidesoft:jide-grids:3.7.10")
        }

        testRuntimeOnly(fileTree(baseDir = configs.nuixEngineLib) {
            println("Runtime Test Library: ${configs.nuixEngineLib}")
            include("*.jar")
        })
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(configs.baseConfigs.targetJreVersion))
    }
}


fun configureTestEnvironment(test: Test) {
    // Args passed to JVM running tests
    test.jvmArgs(
            "--add-exports=java.base/jdk.internal.loader=ALL-UNNAMED",  // Engine 9.6(?) and later require this
            "-Xmx4G",
            "-Djava.io.tmpdir=\"${configs.testing.tempDirectory}\"",
            "-Dnuix.userDataDirs=\"${configs.testing.userDataDirectory}\""
            // "-verbose:class" // Can help troubleshoot weird dependency issues
    )

    // Directory used to store data a test may rely on (like sample data)
    val testOutputDirectory = Paths.get(configs.testing.testOutputDirectoryRoot, "${System.currentTimeMillis()}").pathString
    // Configure ENV vars for JVM tests run in
    test.setEnvironment(
            // Add our engine release's bin and bin/x86 to PATH
            Pair("PATH", "${System.getenv("PATH")};${configs.getNuixBinDirectory()};${configs.getNuixBinX86Directory()}"),

            // Define where tests can place re-usable test data
            Pair("TEST_DATA_DIRECTORY", configs.testing.testDataDirectory),

            // Define where tests can write output produce for later review
            Pair("TEST_OUTPUT_DIRECTORY", testOutputDirectory),

            // Defines where example ruby scripts live
            Pair("RUBY_EXAMPLES_DIRECTORY", configs.testing.rubyExamplesDirectory),

            // Forward ENV username and password
            Pair("NUIX_USERNAME", configs.testing.nuixUsername),
            Pair("NUIX_PASSWORD", configs.testing.nuixPassword),

            // Forward LOCALAPPDATA and APPDATA
            Pair("LOCALAPPDATA", System.getenv("LOCALAPPDATA")),
            Pair("APPDATA", System.getenv("APPDATA")),

            // We need to make sure we set these so workers will properly resolve temp dir
            // (when using a worker based operation via EngineWrapper).
            Pair("TEMP", configs.testing.tempDirectory),
            Pair("TMP", configs.testing.tempDirectory),

            Pair("NUIX_ENGINE_DIR", configs.nuixEngineDirectory)
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

    publish {
        contextUrl = "https://artifactory.uat.nuix.com/artifactory"

        if (null != configs.artifactory.publishArtifactUser && null != configs.artifactory.publishArtifactToken) {
            repository {
                repoKey = configs.artifactory.publishArtifactRepository
                username = configs.artifactory.publishArtifactUser
                password = configs.artifactory.publishArtifactToken
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
    val user = configs.artifactory.publishArtifactUser
    val token = configs.artifactory.publishArtifactToken
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
        val toDelete = File(configs.nuixEngineLib, jarName)
        println("Deleting: " + toDelete.absolutePath)
        delete(toDelete)
    }

    println("Copying files engine to engine lib dir...")
    copy {
        into(File(configs.nuixEngineLib))
        from(configurations.findByName("externalDependency"))
        rename("(.*)\\.jar", "${name}-Dependency-$1.jar")
    }

    copy {
        from(tasks.findByName("nxOnlyJar"))
        into(File(configs.nuixEngineLib))
        rename(".*\\.jar", jarName)
    }
}

// Ensure that tests are ran by JUnit and that test environment gets configured
tasks.test {
    dependsOn(tasks.findByName("copyJarsToEngine"))
    useJUnitPlatform()
    configureTestEnvironment(this)

    println("Test: ${this}")
    println("Test Args: ${jvmArgs}")
    println("Test Environment: ${environment}")
}

// Customize where Javadoc output is written to
tasks.getByName<Javadoc>("javadoc") {
    setDestinationDir(Paths.get("$projectDir", "..", "docs").toFile())
}