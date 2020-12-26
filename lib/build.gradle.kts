//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2020 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
//                                                                                                                     ~
// Licensed under the GNU Lesser General Public License v3.0 (the 'License'). You may not use this file except in      ~
// compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0  ~
// Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on ~
// an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the  ~
// specific language governing permissions and limitations under the License.                                          ~
//                                                                                                                     ~
// Maintainers:                                                                                                        ~
//     Wim Bast, Tom Brus, Ronald Krijgsheld                                                                           ~
// Contributors:                                                                                                       ~
//     Arjan Kok, Carel Bast                                                                                           ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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
    id("org.modelingvalue.gradle.corrector") version "0.3.30"
}

rootProject.defaultTasks("mvgCorrector","clean", "build", "publish", "mvgTagger")

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
