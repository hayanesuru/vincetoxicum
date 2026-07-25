import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    application
    idea
    `java-library`
    id("com.gradleup.shadow") version "9.2.2"
}

dependencies {
    implementation(libs.bundles.server) {
        exclude("org.slf4j")
    }
    implementation("com.velocitypowered:velocity-native:3.4.0-SNAPSHOT") {
        isTransitive = false
    }
}

application {
    mainClass = "hayanesuru.vincetoxicum.Bootstrap"
}

tasks.named<JavaExec>("run") {
    workingDir = file("${rootDir}/run")
    standardInput = System.`in`
    mainClass = application.mainClass
    args("--nogui")
    jvmArgs("--add-modules=jdk.incubator.vector")
    maxHeapSize = "4G"
    minHeapSize = "4G"
    classpath(configurations.runtimeClasspath)
    doFirst {
        workingDir.mkdirs()
    }
    outputs.upToDateWhen { false }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    minimize()
}

repositories {
    maven {
        name = "Fabric Maven"
        url = uri("https://maven.fabricmc.net/")
    }
    mavenCentral()
    maven {
        name = "Minecraft"
        url = uri("https://libraries.minecraft.net")
    }
    maven {
        name = "Paper Maven"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

sourceSets {
    main {
        java {
            srcDir("vincetoxicum")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = Charsets.UTF_8.name()
    options.compilerArgs.add("--add-modules=jdk.incubator.vector")
}

tasks.withType<Javadoc> {
    options.encoding = Charsets.UTF_8.name()
}

tasks.withType<ProcessResources> {
    filteringCharset = Charsets.UTF_8.name()
}

tasks.withType<Test> {
    testLogging {
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events(TestLogEvent.STANDARD_OUT)
    }
}
