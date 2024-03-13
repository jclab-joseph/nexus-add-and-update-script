plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation("org.sonatype.nexus:nexus-rest-client:3.9.0-01")
    implementation("org.sonatype.nexus:nexus-rest-jackson2:3.9.0-01")
    implementation("org.sonatype.nexus:nexus-script:3.9.0-01")
    implementation("org.jboss.spec.javax.servlet:jboss-servlet-api_3.1_spec:1.0.0.Final")
    implementation("com.fasterxml.jackson.core:jackson-core:2.8.6")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.8.6")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.8.6")
    implementation("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.8.6")
    implementation("org.jboss.spec.javax.ws.rs:jboss-jaxrs-api_2.0_spec:1.0.1.Beta1")
    implementation("org.jboss.spec.javax.annotation:jboss-annotations-api_1.2_spec:1.0.0.Final")
    implementation("javax.activation:activation:1.1.1")
    implementation("net.jcip:jcip-annotations:1.0")
    implementation("org.jboss.logging:jboss-logging-annotations:2.0.1.Final")
    implementation("org.jboss.logging:jboss-logging-processor:2.0.1.Final")
    implementation("com.sun.xml.bind:jaxb-impl:2.2.7")
    implementation("com.sun.mail:javax.mail:1.5.6")
    implementation("org.apache.james:apache-mime4j:0.6")
    implementation("org.codehaus.groovy:groovy-all:3.0.9")
    // Exclude statement
//    configurations {
//        all {
//            exclude(group = "org.codehaus.groovy", module = "groovy-all")
//        }
//    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

val fatJar = task("fatJar", type = Jar::class) {
    archiveBaseName.set("${project.name}-fat")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "kr.jclab.nexus.provisioner.addandupdatescript.AddUpdateScript"
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}