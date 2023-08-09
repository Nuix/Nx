plugins {
    id("java")
}

group = "com.nuix.nx"
version = "1.19.0-SNAPSHOT"

val sourceCompatibility = 11
val targetCompatibility = 11

// Directory containing Nuix Engine release
val nuixEngineDirectory: String = System.getenv("NUIX_ENGINE_DIR")
println("NUIX_ENGINE_DIR: ${nuixEngineDirectory}")
if (nuixEngineDirectory.isEmpty()) {
    throw InvalidUserDataException("Please populate the environment variable 'NUIX_ENGINE_DIR' with directory containing a Nuix Engine release")
}

val engineLibDir = "${nuixEngineDirectory}\\lib"
println("engineLibDir: ${engineLibDir}")

val nuixAppLibDir = "C:\\Program Files\\Nuix\\Nuix 100.2\\lib"
println("nuixAppLibDir: ${nuixAppLibDir}")

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

    // ====================
    // Engine Dependencies
    // ====================

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
    val nuixTempDirectory = findProperty("tempDir") ?: "${System.getenv("LOCALAPPDATA")}\\Temp\\Nuix"

    // Args passed to JVM running tests
    test.jvmArgs(
            "--add-exports=java.base/jdk.internal.loader=ALL-UNNAMED",  // Engine 9.6(?) and later require this
            "-Xmx4G",
            "-Djava.io.tmpdir=\"${nuixTempDirectory}\"",
            // "-verbose:class" // Can help troubleshoot weird dependency issues
    )

    // Directory used to store data a test may rely on (like sample data)
    val testDataDirectory = "${projectDir}\\..\\..\\TestData"

    // Directory used to store data a test may rely on (like sample data)
    val rubyExamplesDirectory = "${projectDir}\\..\\..\\Examples"

    // Directory that tests may write data to, unique to each test invocation
    val testOutputDirectory = "${projectDir}\\..\\..\\TestOutput\\${System.currentTimeMillis()}"

    // Configure ENV vars for JVM tests run in
    test.setEnvironment(
            // Add our engine release's bin and bin/x86 to PATH
            Pair("PATH", "${System.getenv("PATH")};${nuixEngineDirectory}\\bin;${nuixEngineDirectory}\\bin\\x86"),

            // Define where tests can place re-usable test data
            Pair("TEST_DATA_DIRECTORY", testDataDirectory),

            // Define where tests can write output produce for later review
            Pair("TEST_OUTPUT_DIRECTORY", testOutputDirectory),

            // Defines where example ruby scripts live
            Pair("RUBY_EXAMPLES_DIRECTORY", rubyExamplesDirectory),

            // Forward ENV username and password
            Pair("NUIX_USERNAME", System.getenv("NUIX_USERNAME")),
            Pair("NUIX_PASSWORD", System.getenv("NUIX_PASSWORD")),

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
tasks.create<Jar>("pluginOnlyJar") {
    println("Producing plug-in only JAR file")
    from(sourceSets.main.get().output)
    exclude("com.nuix.innovation.enginewrapper.*")
}

// Copies plug-in JAR to lib directory of engine release we're running against
tasks.register<Copy>("copyJarsToEngine") {
    val name = "@nx"
    val jarName = "${name}.jar"
    dependsOn(tasks.findByName("pluginOnlyJar"))

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
        from(tasks.findByName("pluginOnlyJar"))
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
    setDestinationDir(File("${projectDir}/../../docs"))
    exclude("com/nuix/innovation/enginewrapper/**")
}