/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.unprotect.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.UntrackedTask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;

@UntrackedTask(because = "It publishes files, which is not cacheable")
public abstract class UploadDefaultCentralRepository extends DefaultTask {
    private static final String API_URL = "https://ossrh-staging-api.central.sonatype.com";
    private static final String API_ENDPOINT = "/manual/upload/defaultRepository/";

    @Input
    public abstract Property<String> getNamespace();

    @Internal
    public abstract Property<String> getUsername();

    @Internal
    public abstract Property<String> getPassword();

    public UploadDefaultCentralRepository() {
        getNamespace().convention(getProject().provider(() -> Objects.toString(getProject().getGroup())));
    }

    @TaskAction
    public void run() throws IOException, InterruptedException {
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + API_ENDPOINT + getNamespace().get()))
                .header("Authorization", getAuthHeader())
                .POST(HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofMinutes(2))
                .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Could not upload default repository to Maven Central portal. " + response.statusCode() + ": " + response.body());
            }
        }
    }

    private String getAuthHeader() {
        byte[] tokenBytes = (getUsername().get() + ":" + getPassword().get()).getBytes(StandardCharsets.UTF_8);
        return "Bearer " + Base64.getEncoder().encodeToString(tokenBytes);
    }
}
