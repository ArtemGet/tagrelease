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
import javax.json.JsonObject;
import javax.json.JsonStructure;

public class EFetchObj implements Entry<JsonObject> {
    private final Entry<JsonStructure> origin;

    public EFetchObj(final Request request) {
        this(new EFetchJson(request));
    }

    public EFetchObj(final Entry<JsonStructure> origin) {
        this.origin = origin;
    }

    @Override
    public JsonObject value() throws EntryException {
        try {
            return this.origin.value().asJsonObject();
        } catch (final ClassCastException exception) {
            throw new EntryException("Failed to map json structure to JsonObject", exception);
        }
    }
}
