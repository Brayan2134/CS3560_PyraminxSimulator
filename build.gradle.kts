plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories { mavenCentral() }

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

javafx {
    version = "21.0.3"
    modules = listOf(
        "javafx.controls"   // <-- use dots, not hyphens
        // "javafx.fxml"     // add if you use FXML
    )
}

application {
    mainClass.set("org.example.app.Main")
}

tasks.test { useJUnitPlatform() }
