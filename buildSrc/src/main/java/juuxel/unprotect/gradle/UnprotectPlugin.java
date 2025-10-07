/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.unprotect.gradle;

import com.diffplug.gradle.spotless.SpotlessExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.plugins.signing.SigningExtension;

public final class UnprotectPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().apply("java-library");

        var java = project.getExtensions().getByType(JavaPluginExtension.class);
        java.withSourcesJar();
        java.withJavadocJar();
        project.getTasks().withType(JavaCompile.class).configureEach(task -> task.getOptions().setEncoding("UTF-8"));
        project.getTasks().named("jar", Jar.class, task -> task.from(project.getRootProject().file("LICENSE")));

        setupPublishing(project);

        project.getPlugins().apply("com.diffplug.spotless");

        project.getExtensions().getByType(SpotlessExtension.class).java(config -> {
            config.licenseHeaderFile(project.getRootProject().file("HEADER.txt"));
        });

        project.getExtensions().create("unprotect", UnprotectExtension.class);
    }

    private static void setupPublishing(Project project) {
        project.getPlugins().apply("maven-publish");
        project.getPlugins().apply("signing");

        var publishing = project.getExtensions().getByType(PublishingExtension.class);
        publishing.getPublications().register("maven", MavenPublication.class, pub -> {
            pub.from(project.getComponents().getByName("java"));
            pub.pom(pom -> {
                pom.getName().set(project.getName());
                pom.getDescription().set("A bytecode transformer that makes protected and package-private code public.");
                pom.getUrl().set("https://github.com/Juuxel/Unprotect");

                pom.licenses(spec -> {
                    spec.license(license -> {
                        license.getName().set("Mozilla Public License Version 2.0");
                        license.getUrl().set("https://www.mozilla.org/en-US/MPL/2.0/");
                    });
                });

                pom.developers(spec -> {
                    spec.developer(dev -> {
                        dev.getId().set("Juuxel");
                        dev.getName().set("Juuxel");
                        dev.getEmail().set("juuzsmods@gmail.com");
                    });
                });

                pom.scm(scm -> {
                    scm.getConnection().set("scm:git:git://github.com/Juuxel/Unprotect.git");
                    scm.getDeveloperConnection().set("scm:git:ssh://github.com/Juuxel/Unprotect.git");
                    scm.getUrl().set("https://github.com/Juuxel/Unprotect");
                });
            });
        });

        var env = System.getenv();

        if (env.containsKey("MAVEN_CENTRAL_USERNAME")) {
            var centralUsername = env.get("MAVEN_CENTRAL_USERNAME");
            var centralPassword = env.get("MAVEN_CENTRAL_PASSWORD");

            publishing.getRepositories().maven(repo -> {
                repo.setName("ossrh-staging-api");
                repo.setUrl("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/");
                repo.credentials(credentials -> {
                    credentials.setUsername(centralUsername);
                    credentials.setPassword(centralPassword);
                });
            });

            var uploadTask = project.getTasks().register("uploadDefaultCentralRepository", UploadDefaultCentralRepository.class, task -> {
                task.dependsOn("publishMavenPublicationToOssrh-staging-apiRepository");
                task.getUsername().set(centralUsername);
                task.getPassword().set(centralPassword);
            });

            project.getTasks().named("publish", task -> {
                task.dependsOn(uploadTask);
            });
        }

        if (project.hasProperty("signing.keyId")) {
            project.getExtensions().getByType(SigningExtension.class).sign(publishing.getPublications());
        }
    }
}
