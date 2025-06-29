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
import io.github.artemget.entrys.Entry;
import io.github.artemget.entrys.EntryException;
import io.github.artemget.entrys.json.EJsonStr;
import io.github.artemget.tagrelease.entry.EFetchArr;
import io.github.artemget.tagrelease.entry.EFetchObj;
import io.github.artemget.tagrelease.entry.EFunc;
import io.github.artemget.tagrelease.exception.DomainException;
import java.util.ArrayList;
import java.util.List;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * Servers.
 *
 * @since 0.1.0
 */
public final class StandsGl implements Stands {
    /**
     * Gitlab request. List repository branches.
     * GET /projects/:id/repository/branches
     */
    private final Entry<JsonArray> stands;

    /**
     * Gitlab request. List repository branch by name.
     * GET /projects/:id/repository/branches
     */
    private final EFunc<String, JsonObject> stand;

    private final EFunc<String, Services> services;


    public StandsGl(
        final Entry<String> url,
        final Entry<String> project,
        final Entry<String> repo,
        final Entry<String> token
    ) {
        this(
            () -> new EFetchArr(
                new JdkRequest(url.value()).uri()
                    .path(String.format("api/v4/projects/:%s/repository/branches", repo.value()))
                    .back().method(Request.GET)
                    .header("Accept", "application/json")
                    .header("PRIVATE-TOKEN:", token.value())
            ).value(),
            (name) -> new EFetchObj(
                new JdkRequest(url.value()).uri()
                    .path(
                        String.format(
                            "api/v4/projects/%s/repository/branches/%s", repo.value(), name
                        )
                    ).back()
                    .method(Request.GET)
                    .header("Accept", "application/json")
                    .header("PRIVATE-TOKEN:", token.value())
            ).value(),
            (branch) -> new ServicesGl(url, project, () -> branch, token)
        );
    }

    public StandsGl(
        final Entry<JsonArray> stands,
        final EFunc<String, JsonObject> stand,
        final EFunc<String, Services> services
    ) {
        this.stands = stands;
        this.stand = stand;
        this.services = services;
    }

    @Override
    public List<Stand> stands() throws DomainException {
        final JsonArray response;
        try {
            response = this.stands.value();
        } catch (EntryException exception) {
            throw new DomainException("Failed to fetch all services from gitlab", exception);
        }
        final List<Stand> stands = new ArrayList<>();
        for (final JsonValue branch : response) {
            stands.add(this.enriched(branch.asJsonObject()));
        }
        return stands;
    }

    @Override
    public Stand stand(final String name) throws DomainException {
        try {
            return this.enriched(this.stand.apply(name));
        } catch (final EntryException exception) {
            throw new DomainException("Failed to fetch all services from gitlab", exception);
        }
    }

    private Stand enriched(final JsonObject service) throws DomainException {
        try {
            final String branch = new EJsonStr(service, "name").value();
            return new StandGl(branch, () -> this.services.apply(branch));
        } catch (final EntryException exception) {
            throw new DomainException("Failed to parse service from gitlab", exception);
        }
    }
}
