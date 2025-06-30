/*
 * MIT License
 *
 * Copyright (c) 2024-2025. Artem Getmanskii
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.artemget.tagrelease.domain;

import com.amihaiemil.eoyaml.Yaml;
import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import io.github.artemget.entrys.Entry;
import io.github.artemget.entrys.EntryException;
import io.github.artemget.entrys.json.EJsonStr;
import io.github.artemget.tagrelease.entry.EFetchArr;
import io.github.artemget.tagrelease.entry.EFetchObj;
import io.github.artemget.tagrelease.entry.EFunc;
import io.github.artemget.tagrelease.exception.DomainException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * Applications from gitlab.
 *
 * @since 0.1.0
 */
public final class ServicesGl implements Services {
    /**
     * List repository tree. Each directory with name not starting with _ prefix - is a service.
     * GET /projects/:id/repository/tree
     */
    private final Entry<JsonArray> services;

    /**
     * Get file from repository. Tag name placed under image:tag: in this file.
     * GET /projects/:id/repository/files/:file_path
     */
    private final EFunc<String, JsonObject> tag;


    /**
     * Ctor configures https req to gitlab services.
     *
     * @param url Of gitlab
     * @param release Where to search services
     * @param branch Project which services belong to
     * @param token Api token
     */
    public ServicesGl(
        final Entry<String> url,
        final Entry<String> release,
        final Entry<String> branch,
        final Entry<String> token
    ) {
        this(() -> new EFetchArr(
                new JdkRequest(
                    String.format(
                        "%s/api/v4/projects/%s/repository/tree?ref=%s&per_page=100",
                        url.value(), release.value(), branch.value()
                    )
                ).method(Request.GET)
                    .header("Accept", "application/json")
                    .header("PRIVATE-TOKEN:", token.value())
            ).value(),
            (service) -> new EFetchObj(
                new JdkRequest(
                    String.format(
                        "%s/api/v4/projects/%s/repository/files/%s?ref=%s",
                        url.value(), release.value(), service.concat("%2Fvalues.yaml"), branch.value()
                    )
                ).method(Request.GET)
                    .header("Accept", "application/json")
                    .header("PRIVATE-TOKEN:", token.value())
            ).value()
        );
    }

    /**
     * Main ctor.
     *
     * @param services At gitlab
     */
    public ServicesGl(final Entry<JsonArray> services, final EFunc<String, JsonObject> tag) {
        this.services = services;
        this.tag = tag;
    }

    @Override
    public List<Service> services() throws DomainException {
        final JsonArray array;
        try {
            array = this.services.value();
        } catch (final EntryException exception) {
            throw new DomainException("Failed to fetch services from stand", exception);
        }
        final List<Service> services = new ArrayList<>();
        for (final JsonValue dir : array) {
            final JsonObject json = dir.asJsonObject();
            final String name = json.getString("name");
            final boolean directory = json.getString("type").equals("tree");
            final boolean service = !name.startsWith("_");
            if (directory && service) {
                services.add(this.service(name));
            }
        }
        return services;
    }

    @Override
    public Service service(final String name) throws DomainException {
        final JsonObject json;
        try {
            json = this.tag.apply(name);
        } catch (EntryException exception) {
            throw new DomainException(
                String.format("Failed to fetch '%s' service from stand", name),
                exception
            );
        }
        final String content;
        try {
            content = new EJsonStr(json, "content").value();
        } catch (final EntryException exception) {
            throw new DomainException(
                String.format("Failed to get content for service:'%s' from:'%s'", name, json),
                exception
            );
        }
        try {
            return new ServiceEa(
                "",
                name,
                Yaml.createYamlInput(content).readYamlMapping()
                    .yamlMapping("image")
                    .string("tag")
            );
        } catch (final IOException exception) {
            throw new DomainException(
                String.format("Failed to get content for service:'%s' from:'%s'", name, json),
                exception
            );
        }
    }
}
