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
import io.github.artemget.tagrelease.entry.EFetchArr;
import io.github.artemget.tagrelease.entry.EFunc;
import io.github.artemget.tagrelease.exception.DomainException;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

public final class TagsGl implements Tags {
    private final EFunc<Tag, JsonArray> tag;
    private final EFunc<Tag, JsonArray> create;

    public TagsGl(
        final Entry<String> url,
        final Entry<String> token
    ) {
        this(
            (tag) ->
                new EFetchArr(
                    new JdkRequest(
                        String.format(
                            "%s/api/v4/projects/%s/repository/tags?search=%%5E%s",
                            url.value(),
                            tag.repo(),
                            tag.name().replace(".*", "")
                        )
                    ).method(Request.GET)
                        .header("Accept", "application/json")
                        .header("PRIVATE-TOKEN", token.value())
                ).value(),
            (tag) ->
                new EFetchArr(
                    new JdkRequest(
                        String.format(
                            "%s/api/v4/projects/%s/repository/tags?ref=%s&tag_name=%s",
                            url.value(),
                            tag.repo(),
                            tag.name().replace(".*", "")
                        )
                    ).method(Request.POST)
                        .header("Accept", "application/json")
                        .header("PRIVATE-TOKEN", token.value())
                ).value()
        );
    }

    public TagsGl(final EFunc<Tag, JsonArray> tag, final EFunc<Tag, JsonArray> create) {
        this.tag = tag;
        this.create = create;
    }

    @Override
    public Tag buildNew(final String serviceId, final String branch, final String prefix) throws DomainException {
        final JsonArray response;
        try {
            response = this.tag.apply(new TagEa(serviceId, prefix, branch));
        } catch (final EntryException exception) {
            throw new DomainException(
                String.format(
                    "Failed to fetch tag with prefix:'%s' for service:'%s' from branch:'%s'",
                    prefix, serviceId, branch
                ),
                exception
            );
        }
        for (final JsonValue value : response) {
            String tag = value.asJsonObject().getString("name");

        }
        return null;
    }

    @Override
    public Tag current(final String serviceId, final String branch, final String prefix) throws DomainException {
        final JsonArray response;
        try {
            response = this.tag.apply(new TagEa(serviceId, prefix, branch));
        } catch (final EntryException exception) {
            throw new DomainException(
                String.format(
                    "Failed to fetch tag with prefix:'%s' for service:'%s' from branch:'%s'",
                    prefix, serviceId, branch
                ),
                exception
            );
        }
        return new TagEa(
            serviceId,
            TagsGl.found(response, prefix).getString("name"),
            branch
        );
    }

    public static JsonObject found(JsonArray array, String prefix) throws DomainException {
        return null;
    }

    public static String trimmed(String cell) {
        return null;
    }
}
