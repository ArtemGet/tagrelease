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
import com.jcabi.http.response.RestResponse;
import io.github.artemget.entrys.Entry;
import io.github.artemget.entrys.EntryException;
import io.github.artemget.entrys.json.EJsonStr;
import io.github.artemget.tagrelease.exception.DomainException;
import java.io.IOException;
import java.io.StringReader;
import javax.json.Json;
import org.cactoos.Scalar;

/**
 * Application's source code in gitlab.
 *
 * @since 0.1.0
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class ServiceEa implements Service {
    /**
     * Application name.
     */
    private final String name;

    /**
     * Application tag.
     */
    private final String tag;

    //TODO: remove eager and req in ctor
    public ServiceEa(final Entry<String> name, final Entry<Request> tag) throws EntryException, IOException {
        this(
            name.value(),
            Yaml.createYamlInput(
                    new EJsonStr(
                        Json.createReader(
                            new StringReader(
                                tag.value().fetch().as(RestResponse.class).body()
                            )
                        ).readObject()
                        , "content"
                    ).value()
                ).readYamlMapping()
                .yamlMapping("image")
                .string("tag")
        );
    }

    /**
     * Main ctor.
     *
     * @param name Of application
     * @param tag Of application
     */
    public ServiceEa(final String name, final String tag) {
        this.name = name;
        this.tag = tag;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String tag() {
        return this.tag;
    }

    // @checkstyle IllegalCatchCheck (50 lines)
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    @Override
    public Service tagged(final Scalar<String> rule) throws DomainException {
        try {
            return new ServiceEa(this.name, rule.value());
        } catch (final Exception exception) {
            throw new DomainException(
                String.format("Failed to create tag for service: %s", this.name),
                exception
            );
        }
    }
}
