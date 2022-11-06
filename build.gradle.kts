import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.4"
	id("io.spring.dependency-management") version "1.0.14.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
}

group = "com.github.nmicra"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("com.zaxxer:HikariCP:5.0.0")
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.liquibase:liquibase-core")
	implementation("org.springframework:spring-jdbc")
	implementation("org.springdoc:springdoc-openapi-webflux-ui:1.6.11")
	implementation("org.springdoc:springdoc-openapi-kotlin:1.6.11")

	implementation("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")
	implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.0")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
	implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.0")
	implementation ("org.tribuo:tribuo-all:4.2.1@pom") {
		isTransitive = true // for build.gradle.kts (i.e., Kotlin)
	}

	implementation("com.squareup.okhttp3:okhttp:4.9.3")
	implementation("org.jetbrains.kotlinx:dataframe:0.8.0-rc-8")

	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("org.postgresql:r2dbc-postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation ("org.testcontainers:postgresql:1.17.1")
	testImplementation("org.testcontainers:testcontainers:1.16.2")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
