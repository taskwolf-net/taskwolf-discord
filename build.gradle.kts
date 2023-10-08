plugins {
  id("java")
}

group = "de.flexpedite"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
  maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/Flexpedite/flexpedite")
    credentials {
      username = providers.gradleProperty("githubUsername").get()
      password = providers.gradleProperty("githubAccessToken").get()
    }
  }
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.10.0"))
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

  implementation("de.flexpedite:core:1.0.0-SNAPSHOT")

  implementation("com.google.inject:guice:7.0.0")

  implementation("com.google.guava:guava:32.1.2-jre")

  implementation("org.projectlombok:lombok:1.18.30")
  annotationProcessor("org.projectlombok:lombok:1.18.30")
  testImplementation("org.projectlombok:lombok:1.18.30")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

  implementation("com.datastax.cassandra:cassandra-driver-core:4.0.0")

  implementation("org.json:json:20230618")
  implementation("commons-io:commons-io:2.14.0")
}

tasks.test {
  useJUnitPlatform()
}