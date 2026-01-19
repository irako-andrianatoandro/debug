plugins {
    id("java")
    id("com.google.protobuf") version "0.9.5"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "dev.irako"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // gRPC dependencies
    implementation("io.grpc:grpc-netty-shaded:1.60.1")
    implementation("io.grpc:grpc-protobuf:1.60.1")
    implementation("io.grpc:grpc-stub:1.60.1")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53") // for @Generated annotation

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.grpc:grpc-testing:1.60.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

// Protobuf configuration
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.60.1"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
            }
        }
    }
}

// Ensure proto generation happens before compilation
tasks.named("compileJava") {
    dependsOn("generateProto")
}

// Code formatting with Spotless
spotless {
    java {
        target("src/**/*.java")
        // Use Eclipse formatter for Java 25 compatibility
        // Note: Google Java Format and Palantir don't support Java 25 yet
        eclipse("4.31")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlin {
        target("**/*.kts")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("proto") {
        target("**/*.proto")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
