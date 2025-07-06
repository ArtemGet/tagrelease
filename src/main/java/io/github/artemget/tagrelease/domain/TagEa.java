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

public final class TagEa implements Tag {
    private final String repo;
    private final String name;
    private final String branch;
    private final String commit;
    private final String message;
    private final String created;

    public TagEa(
        final String repo,
        final String name,
        final String branch,
        final String commit,
        final String message,
        final String created
    ) {
        this.repo = repo;
        this.name = name;
        this.branch = branch;
        this.commit = commit;
        this.message = message;
        this.created = created;
    }

    @Override
    public String repo() {
        return this.repo;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String branch() {
        return this.branch;
    }

    @Override
    public String fromCommit() {
        return this.commit;
    }

    @Override
    public String message() {
        return this.message;
    }

    @Override
    public String created() {
        return this.created;
    }
}
