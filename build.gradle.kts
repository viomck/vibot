import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.6.21"
	application
}

group = "com.viomck"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("dev.kord:kord-core:0.8.0-M14")
}

application {
	mainClassName = "com.viomck.vibot.VibotApplicationKt"
}

tasks.jar {
	manifest {
		attributes["Main-Class"] = "com.viomck.vibot.VibotApplicationKt"
	}

	duplicatesStrategy = DuplicatesStrategy.EXCLUDE

	from(sourceSets.main.get().output)
	dependsOn(configurations.runtimeClasspath)
	from({
		configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
	})
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

kotlin.sourceSets.all {
	languageSettings.optIn("kotlin.RequiresOptIn")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
