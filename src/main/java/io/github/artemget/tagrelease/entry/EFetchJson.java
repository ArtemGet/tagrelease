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

package io.github.artemget.tagrelease.entry;

import com.jcabi.http.Request;
import io.github.artemget.entrys.Entry;
import io.github.artemget.entrys.EntryException;
import java.io.IOException;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonStructure;

public class EFetchJson implements Entry<JsonStructure> {
    private final Request request;

    public EFetchJson(final Request request) {
        this.request = request;
    }

    @Override
    public JsonStructure value() throws EntryException {
        try {
            return Json.createReader(new StringReader(request.fetch().body())).read();
        } catch (final IOException exception) {
            throw new EntryException(
                String.format("Failed to fetch json array from resource:%s", this.request.uri()),
                exception
            );
        }
    }
}
