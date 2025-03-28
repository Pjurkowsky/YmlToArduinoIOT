plugins {
    kotlin("jvm") version "2.1.10"
    java
    antlr
    application
}

group = "essa"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:ST4:4.3.4")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.generateGrammarSource {
    outputDirectory = file("./build/generated/sources/main/kotlin/antlr")
    arguments = listOf("-package", "essa", "-visitor", "-no-listener")
}

sourceSets {
    main {
        java {
            srcDir(tasks.generateGrammarSource)
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("MainKt")
}