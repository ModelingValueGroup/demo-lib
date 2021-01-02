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
    packageRepo = "$COMPANY/packages"
} else {
    group = "snapshots." + GROUP
    version = String.format("%08x", GITHUB_REF.hashCode()) + "-SNAPSHOT"
    packageRepo = "$COMPANY/packages-snapshots"
}

println("@@@@@@@@@@@     GITHUB_REF=$GITHUB_REF")
println("@@@@@@@@@@@        version=$version")
println("@@@@@@@@@@@          group=$group")
println("@@@@@@@@@@@    packageRepo=$packageRepo")

plugins {
    `java-library`
    `maven-publish`
    id("org.modelingvalue.gradle.corrector") version "0.3.32"
}

rootProject.defaultTasks("clean", "build", "publish", "mvgTagger")

repositories {
    jcenter()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    api("org.apache.commons:commons-math3:3.6.1")
    implementation("com.google.guava:guava:29.0-jre")
}

tasks.test {
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
