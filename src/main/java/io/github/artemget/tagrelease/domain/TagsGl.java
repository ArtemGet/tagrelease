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
import io.github.artemget.tagrelease.entry.EFetchObj;
import io.github.artemget.tagrelease.entry.EFunc;
import io.github.artemget.tagrelease.exception.DomainException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TagsGl implements Tags {
    private final Logger log = LoggerFactory.getLogger(TagsGl.class);
    private final EFunc<Tag, JsonArray> tag;
    private final EFunc<Tag, JsonObject> create;
    private final EFunc<Tag, String> message;

    public TagsGl(
        final Entry<String> url,
        final Entry<String> token
    ) {
        this(
            (tag) ->
                new EFetchArr(
                    new JdkRequest(
                        String.format(
                            "%s/api/v4/projects/%s/repository/tags?order_by=version&search=%%5E%s",
                            url.value(),
                            tag.repo(),
                            tag.name().replace(".*", "")
                        )
                    ).method(Request.GET)
                        .header("Accept", "application/json")
                        .header("PRIVATE-TOKEN", token.value())
                ).value(),
            (tag) ->
                new EFetchObj(
                    new JdkRequest(
                        String.format(
                            "%s/api/v4/projects/%s/repository/tags?ref=%s&tag_name=%s&message=%s",
                            url.value(),
                            tag.repo(),
                            tag.branch(),
                            tag.name(),
                            tag.message()
                        )
                    ).method(Request.POST)
                        .header("Accept", "application/json")
                        .header("PRIVATE-TOKEN", token.value())
                ).value(),
            (tag) -> {
                final StringBuilder message = new StringBuilder();
                int page = 1;
                boolean isFound = false;
                while (page <= 5 && !isFound) {
                    final JsonArray mrs = TagsGl.mrs(tag, url.value(), token.value(), String.valueOf(page));
                    for (final JsonValue value : mrs) {
                        final JsonObject mr = value.asJsonObject();
                        final boolean isSquash = !mr.isNull("squash_commit_sha")
                            && tag.fromCommit().equals(mr.getString("squash_commit_sha"));
                        final boolean isMerge = !mr.isNull("merge_commit_sha")
                            && tag.fromCommit().equals(mr.getString("merge_commit_sha"));
                        final boolean isMr = !mr.isNull("sha")
                            && tag.fromCommit().equals(mr.getString("sha"));
                        if (isMerge || isSquash || isMr) {
                            isFound = true;
                            break;
                        }
                        final String trimmed;
                        if (mr.getString("title").length() >= 30) {
                            trimmed = mr.getString("title").substring(0, 30).concat("...");
                        } else {
                            trimmed = mr.getString("title");
                        }
                        message.append(trimmed).append("\n");
                    }
                    page++;
                }
                return message.toString();
            }
        );
    }

    public TagsGl(
        final EFunc<Tag, JsonArray> tag,
        final EFunc<Tag, JsonObject> create,
        EFunc<Tag, String> message
    ) {
        this.tag = tag;
        this.create = create;
        this.message = message;
    }

    @Override
    public Tag buildNew(final String serviceId, final String branch, final String prefix) throws
        DomainException {
        final Tag current = this.current(serviceId, branch, prefix);
        final String next = TagsGl.next(current.name(), prefix);
        String message;
        try {
            message = URLEncoder.encode(
                String.format(
                    "Данное сообщение было сгенерировано автоматически.\nИзменения '%s' -> '%s':\n%s",
                    current.name(),
                    next,
                    this.message.apply(current)
                ),
                StandardCharsets.UTF_8
            );
            if (message.length() > 5000) {
                message = message.substring(0, 4996).concat("\n...");
            }
        } catch (final EntryException exception) {
            message = "";
            log.error(
                "Failed to fetch merge requests for service:'{}', branch:'{}'. Between tags '{}' - '{}'",
                serviceId,
                branch,
                current.name(),
                next,
                exception
            );
        }
        final JsonObject created;
        try {
            created = this.create.apply(
                new TagEa(
                    current.repo(),
                    next,
                    current.branch(),
                    current.fromCommit(),
                    message,
                    current.created()
                )
            );
        } catch (final EntryException exception) {
            throw new DomainException(
                String.format(
                    "Failed to create tag with prefix:'%s' for service:'%s' from branch:'%s'. Latest tag:%s",
                    prefix, serviceId, branch, current.name()
                ),
                exception
            );
        }
        final JsonObject commit = created.getJsonObject("commit");
        return new TagEa(
            serviceId,
            created.getString("name"),
            branch,
            commit.getString("id"),
            message,
            commit.getString("created_at")
        );
    }

    @Override
    public Tag current(final String serviceId, final String branch, final String prefix) throws
        DomainException {
        final JsonArray response;
        try {
            response = this.tag.apply(new TagEa(serviceId, prefix, branch, "", "", ""));
        } catch (final EntryException exception) {
            throw new DomainException(
                String.format("Failed to fetch tag with prefix:'%s' for service:'%s'", prefix, serviceId),
                exception
            );
        }
        //TODO: add branch check
        if (response.isEmpty()) {
            throw new DomainException(
                String.format("Failed to find tag with prefix:'%s' for service:'%s'", prefix, serviceId)
            );
        }
        final JsonObject current = response.getJsonObject(0);
        final JsonObject commit = current.getJsonObject("commit");
        return new TagEa(
            serviceId,
            current.getString("name"),
            branch,
            commit.getString("id"),
            commit.getString("message"),
            commit.getString("created_at")
        );
    }

    public static String next(final String current, final String prefix) {
        final String trimmed = prefix.replace("*", "");
        final StringBuilder next = new StringBuilder();
        final int length = current.toCharArray().length;
        for (int index = 0; index < length; index++) {
            if (trimmed.length() == index) {
                final StringBuilder digit = new StringBuilder();
                while (index < length && Character.isDigit(current.charAt(index))) {
                    digit.append(current.charAt(index));
                    index++;
                }
                next.append(Integer.parseInt(digit.toString()) + 1);
                if (index < length) {
                    next.append(current.charAt(index));
                }
                continue;
            }
            if (trimmed.length() < index && Character.isDigit(current.charAt(index))) {
                next.append('0');
            } else {
                next.append(current.charAt(index));
            }
        }
        return next.toString();
    }

    private static JsonArray mrs(final Tag tag, String url, final String token, final String page)
        throws EntryException {
        return new EFetchArr(
            new JdkRequest(
                String.format(
                    "%s/api/v4/projects/%s/merge_requests?state=merged&scope=all&created_after=%s&target_branch=%s&per_page=%s&page=%s",
                    url,
                    tag.repo(),
                    tag.created(),
                    tag.branch(),
                    "20",
                    page
                )
            ).method(Request.GET)
                .header("Accept", "application/json")
                .header("PRIVATE-TOKEN", token)
        ).value();
    }
}
