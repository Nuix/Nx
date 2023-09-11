import java.net.URI
import java.nio.file.Files
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
         nuixEngineRepo => Optional repository to download the Nuix Engine distribution from
           nuixEngineOs => If using a repository for the engine, the OS for the engine.  Acceptable options are `linux` or `windows` and defaults to `windows`
      nuixEngineRelease => If using a repository for the engine, release version of the engine to use, in the form <major>.<minor>.<point>.<build>.  Default to 9.10.17.1073

There are two main alternate routes run these builds:
1) Provide a path to a Nuix Workstation build
    * Either provide a NUIX_ENGINE_DIR environment variable
    * Or run with the ${nuixEngineDir} property
    * The path must be to a Nuix Engine or Nuix Workstation path that includes GUI JAR files.  Normally, the Nuix Engine Distribution does not
    * If both an environment variable and property are provided, the property will be used.
    * An example might be `gradlew -PnuixEngineDir="C:\Program Files\Nuix\Nuix 9.10" ...
2) Download the Nuix Engine from some location and get GUI dependencies from a maven repository
    * Pass in the nuixEngineRepo where the Nuix Engine will be downloaded from.
    * Optionally pass in the nuixDependencyRepo where some dependencies, like the GUI dependencies will be found.  If this isn't provided, mavenCentral will be used
    * An example might be `gradlew -PnuixEngineRepo=https://my.artifactory.com/nuix-engine` or `gradlew -PnuixDependencyRepo=https://my.artifactory.com/nuix-dependencies -PnuixEngineRepo=https://my.artifactory.com/nuix-engine`
    * Note, the full path for download will be '${nuixEngineRepo}/${major}.${minor}/engine-dist-${os}-${major}.${minor}.${release}.${build}.${extension}
      o The ${major} ${minor} ${release} and ${build} variables are calculated from the ${nuixEngineRelease} property
      o The ${os} and ${extension} variables are calculated from the ${nuixEngineOs} property
If both the Nuix Engine Directory (either through the property or an environment variable) and ${nuixEngineRepo} properties are provided, the Nuix Engine Directory will be used.
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

if (configs.isUseRepository && configs.engineDistro.nuixEngineRepo.isNullOrEmpty()) {
    throw InvalidUserDataException("Please populate the environment variable 'NUIX_ENGINE_DIR' with directory " +
            "containing a Nuix Workstation release or provide the properties 'nuixEngineRepo' and 'nuixDepencencyRepo' " +
            "that can be used to get dependencies.")
}

if (configs.isUseRepository) {
    downloadEngineIfNeeded()
}

repositories {
    if(configs.isUseRepository) {
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

    if(configs.isUseRepository) {
        compileOnly("org.swinglabs.swingx:swingx-core:1.6.6-N1.2")
        compileOnly("com.jidesoft:jide-grids:3.7.10")
    }

    testRuntimeOnly(fileTree(baseDir = configs.nuixEngineLib) {
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
    if(configs.isUseRepository) {
        println("No Engine Path, downloading a new one.")

        val engineDownloadDir = file("engine/download")
        Files.createDirectories(engineDownloadDir.toPath())

        val engineFileName = configs.engineDistro.getEngineDistributionName()
        val engineDownloadDestination = file("${engineDownloadDir}/${engineFileName}")
        println("Downloading Engine to ${engineDownloadDestination}")

        if (!engineDownloadDestination.exists()) {
            val engineDownloadSource = configs.engineDistro.getEngineDistributionPath()
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

        configs.setNuixEngineDirectory(file("engine/release").absolutePath.toString())
    }

}

fun configureTestEnvironment(test: Test) {
    // Args passed to JVM running tests
    test.jvmArgs(
            "--add-exports=java.base/jdk.internal.loader=ALL-UNNAMED",  // Engine 9.6(?) and later require this
            "-Xmx4G",
            "-Djava.io.tmpdir=\"${configs.testing.tempDirectory}\"",
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
}

// Customize where Javadoc output is written to
tasks.getByName<Javadoc>("javadoc") {
    setDestinationDir(Paths.get("$projectDir", "..", "docs").toFile())
}