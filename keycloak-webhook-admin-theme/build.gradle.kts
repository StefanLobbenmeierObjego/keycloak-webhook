plugins {
    base
}

group = "com.vymalo.keycloak.webhook"
version = "0.10.0-rc.1"

val themeName = "keycloak-webhook"
val buildDirPath = layout.projectDirectory.dir("build")
val resourcesDir = buildDirPath.dir("theme/$themeName/admin/resources")

tasks.register<Exec>("npmInstall") {
    commandLine("npm", "install")
}

tasks.register<Exec>("buildThemeAssets") {
    dependsOn("npmInstall")
    commandLine("npm", "run", "build")
}

tasks.register<Copy>("prepareThemeArchive") {
    dependsOn("buildThemeAssets")
    from(resourcesDir)
    into(layout.buildDirectory.dir("theme-archive/theme/$themeName/admin/resources"))
    from(layout.projectDirectory.file("src/main/resources/theme/$themeName/admin/index.ftl")) {
        into("theme/$themeName/admin")
    }
    from(layout.projectDirectory.file("src/main/resources/theme/$themeName/admin/theme.properties")) {
        into("theme/$themeName/admin")
    }
    from(layout.projectDirectory.file("src/main/resources/META-INF/keycloak-themes.json")) {
        into("META-INF")
    }
}

tasks.register<Jar>("themeJar") {
    dependsOn("prepareThemeArchive")
    archiveBaseName.set("keycloak-webhook-admin-theme")
    archiveVersion.set(project.version.toString())
    from(layout.buildDirectory.dir("theme-archive"))
}

tasks.assemble {
    dependsOn("themeJar")
}
