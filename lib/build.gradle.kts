/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java library project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/6.7.1/userguide/building_java_projects.html
 */
val VERSION: String by project
val GROUP: String by project
val COMPANY: String by project

val CI: Boolean = "true".equals(System.getenv("CI"))
val TOKEN: String = System.getenv("TOKEN") ?: "DRY"
val GITHUB_REF: String = rootProject.projectDir.toPath().resolve(".git/HEAD").toFile().readLines()[0].replaceFirst(Regex("^ref: "), "")
val isMaster: Boolean = GITHUB_REF.equals("refs/heads/master")
var packageRepo: String

if (CI && isMaster) {
    group = GROUP
    version = VERSION
    packageRepo = "$COMPANY/$GROUP"
} else {
    group = "snapshots." + GROUP
    version = String.format("%08x", GITHUB_REF.hashCode()) + "-SNAPSHOT"
    packageRepo = "$COMPANY/tmp-snapshots"
}

println("@@@@@@@@@@@     GITHUB_REF=$GITHUB_REF")
println("@@@@@@@@@@@        version=$version")
println("@@@@@@@@@@@          group=$group")
println("@@@@@@@@@@@    packageRepo=$packageRepo")

plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    `maven-publish`
}

repositories {
    // Use JCenter for resolving dependencies.
    jcenter()
}

dependencies {
    // Use JUnit Jupiter API for testing.
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")

    // Use JUnit Jupiter Engine for testing.
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.apache.commons:commons-math3:3.6.1")

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation("com.google.guava:guava:29.0-jre")
}

tasks.test {
    // Use junit platform for unit tests.
    useJUnitPlatform()
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("lib") {
            from(components["java"])
        }
    }
    repositories {
        if (CI) {
            maven {
                url = uri("https://maven.pkg.github.com/$packageRepo")
                credentials {
                    username = "" // can be anything but plugin requires it
                    password = TOKEN
                }
            }
        } else {
            mavenLocal()
        }
    }
}