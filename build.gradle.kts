import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")

    kotlin("jvm") version "1.5.10"

    // Shadow jar: permette di creare una jar (eseguibile con java) contenente tutto il nostro codice + quello delle "dependencies" listate sotto
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

// Dico a Java dove si trova il metodo main per eseguire successivamente la jar quando verrà creata
application.mainClass.set("BotKt")
// Non importante
group = "dev.giuliopime.kubernetes-discord-bot"
version = "1.0"

// Repositories da dove prendere le dependencies (librerie)
repositories {
    mavenCentral()
    maven {
        name = "m2-dv8tion"
        url = uri("https://m2.dv8tion.net/releases")
    }
    maven("https://jitpack.io/")
}

dependencies {
    // JDA: Permette di interagire con le API di Discord con il linguaggio Java
    // Fornisce già dei metodi e delle classi che facilitano il tutto
    implementation("net.dv8tion:JDA:4.3.0_335") {
        exclude(module = "opus-java")
    }

    // Aggiunge alcune funzionalità per l'utilizzo di JDA con kotlin in modo più facile e veloce
    implementation("com.github.minndevelopment:jda-ktx:1a45395")

    // Permette di leggere il file .env per impostazioni e chiavi private
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
}

tasks {
    withType<KotlinCompile>() {
        kotlinOptions.jvmTarget = "1.8"
    }

    shadowJar {
        // Imposto il nome della jar
        archiveFileName.set("kubernetes-discord-bot.jar")
    }
}

// Qualsiasi task che esegue la build deve includere le dependencies per questo deve usare la shadowjar
tasks {
    build {
        dependsOn(shadowJar)
    }
}
