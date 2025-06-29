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

import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import io.github.artemget.entrys.Entry;
import io.github.artemget.entrys.EntryException;
import io.github.artemget.entrys.json.EJsonStr;
import io.github.artemget.tagrelease.entry.EFunc;
import io.github.artemget.tagrelease.exception.CommunicationException;
import io.github.artemget.tagrelease.exception.DomainException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

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
    private final Request services;

    /**
     * Get file from repository. Tag name placed under image:tag: in this file.
     * GET /projects/:id/repository/files/:file_path
     */
    private final EFunc<String, Request> tag;


    /**
     * Ctor configures https req to gitlab services.
     *
     * @param url Of gitlab.
     * @param release Where to search services.
     * @param branch Project which services belong to.
     * @throws EntryException If no entry data
     */
    public ServicesGl(
        final Entry<String> url,
        final Entry<String> release,
        final Entry<String> branch,
        final Entry<String> token
    ) throws EntryException {
        this(
            new JdkRequest(url.value()).uri()
                .path(
                    String.format(
                        "api/v4/projects/%s/repository/tree",
                        release.value()
                    )
                ).queryParam(
                    "ref",
                    branch.value()
                ).back()
                .method(Request.GET)
                .header("Accept", "application/json"),
            (service) -> new JdkRequest(url.value()).uri()
                .path(
                    String.format(
                        "api/v4/projects/%s/repository/files/%s",
                        release.value(),
                        service.concat("%2Fvalues.yaml")
                    )
                ).queryParam(
                    "ref",
                    branch.value()
                ).back()
                .method(Request.GET)
                .header("Accept", "application/json")
        );
    }

    /**
     * Main ctor.
     *
     * @param services At gitlab
     */
    public ServicesGl(final Request services, final EFunc<String, Request> tag) {
        this.services = services;
        this.tag = tag;
    }

    @Override
    public List<Service> services() throws DomainException {
        return null;
//        return this.directories().stream()
//            .filter(dir -> {
//                    boolean pass;
//                    try {
//                        final JsonObject json = dir.asJsonObject();
//                        final boolean directory = new EJsonStr(json, "type").value().equals("tree");
//                        final boolean service = !new EJsonStr(json, "name").value().startsWith("_");
//                        pass = directory && service;
//                    } catch (final EntryException exception) {
//                        pass = false;
//                        //TODO: add logging
//                    }
//                    return pass;
//                }
//            ).map(dir -> {
//                    final String directory = new EJsonStr(dir.asJsonObject(), "name").value();
//                    return new ServiceGitlabEager(
//                        () -> directory,
//                        () -> this.tag.apply(directory)
//                    );
//                }
//            ).collect(Collectors.toList());
    }

    @Override
    public Service service(final String name) throws DomainException {
        return null;
//        return this.directories().stream()
//            .filter(service -> {
//                    boolean pass;
//                    try {
//                        JsonObject json = service.asJsonObject();
//                        final boolean directory = new EJsonStr(json, "type").value().equals("tree");
//                        final boolean named = new EJsonStr(json, "name").value().equals(name);
//                        pass = directory && named;
//                    } catch (final EntryException exception) {
//                        pass = false;
//                        //TODO: add logging
//                    }
//                    return pass;
//                }
//            ).findFirst()
//            .map(dir -> {
//                final String directory = new EJsonStr(dir.asJsonObject(), "name").value();
//                return new ServiceGitlabEager(
//                    () -> directory,
//                    () -> this.tag.apply(directory)
//                );
//            })
//            .orElseThrow();
    }

    private Service created(final JsonObject dir) throws DomainException {
        try {
            final String directory = new EJsonStr(dir.asJsonObject(), "name").value();
            return new ServiceEa(
                () -> directory,
                () -> this.tag.apply(directory)
            );
        } catch (final EntryException exception) {
            throw new DomainException(exception);
        } catch (final IOException exception) {
            throw new CommunicationException("Failed to fetch file from gitlab.", exception);
        }
    }

    private JsonArray directories() throws DomainException {
        try {
            return Json.createReader(
                new StringReader(
                    this.services.fetch().as(RestResponse.class).body()
                )
            ).readArray();
        } catch (final IOException exception) {
            throw new CommunicationException(
                String.format(
                    "Failed to fetch services names from gitlab. Request: %s",
                    this.services.uri()
                ),
                exception
            );
        }
    }
}
