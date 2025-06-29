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
import io.github.artemget.tagrelease.exception.DomainException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;

/**
 * Fetch repositories(services) from gitlab project.
 *
 * @since 0.0.1
 */
public final class ServicesAll implements Services {
    private final EFunc<String, JsonArray> named;
    private final Entry<JsonArray> all;

    public ServicesAll(final Entry<String> host, final Entry<String> project) {
        this(
            (service) -> ServicesAll.fetched(
                new JdkRequest(host.value()).uri()
                    .path(String.format("api/v4/groups/%s/projects", project.value()))
                    .queryParam("search", service)
                    .back().method(Request.GET).header("Accept", "application/json")
            ),
            () -> ServicesAll.fetched(
                new JdkRequest(host.value()).uri()
                    .path(String.format("api/v4/groups/%s/projects", project.value()))
                    .back().method(Request.GET).header("Accept", "application/json")
            )
        );
    }

    public ServicesAll(final EFunc<String, JsonArray> named, final Entry<JsonArray> all) {
        this.named = named;
        this.all = all;
    }

    private static JsonArray fetched(final Request request) throws EntryException {
        try {
            return Json.createReader(
                new StringReader(request.fetch().as(RestResponse.class).body())
            ).readArray();
        } catch (final IOException exception) {
            throw new EntryException(
                "Failed to fetch from gitlab",
                exception
            );
        }
    }

    @Override
    public List<Service> services() throws DomainException {
        final JsonArray response;
        try {
            response = this.all.value();
        } catch (final EntryException exception) {
            throw new DomainException("Failed to fetch all services from gitlab", exception);
        }
        final List<Service> services = new ArrayList<>();
        for (final JsonValue service : response) {
            services.add(this.parsed(service));
        }
        return services;
    }

    @Override
    public Service service(String name) throws DomainException {
        final JsonArray response;
        try {
            response = this.named.apply(name);
        } catch (final EntryException exception) {
            throw new DomainException(
                String.format("Failed to fetch '%s' service from gitlab", name),
                exception
            );
        }
        for (final JsonValue value : response) {
            final Service service = this.parsed(value);
            if (name.equals(service.name())) {
                return service;
            }
        }
        throw new DomainException(String.format("No service with name: %s", name));
    }

    private Service parsed(final JsonValue service) throws DomainException {
        try {
            return new ServiceEa(new EJsonStr(service.asJsonObject(), "name").value(), "");
        } catch (final EntryException exception) {
            throw new DomainException("Failed to parse service from gitlab", exception);
        }
    }
}
