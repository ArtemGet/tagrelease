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

import io.github.artemget.tagrelease.exception.DomainException;
import java.util.List;
import java.util.stream.Collectors;
import org.cactoos.Text;

/**
 * Servers.
 *
 * @since 0.1.0
 */
public interface Stands {
    /**
     * Returns available servers.
     *
     * @return Servers
     */
    List<Stand> stands() throws DomainException;

    /**
     * Returns server by it's name.
     *
     * @param name Name
     * @return Server
     */
    Stand stand(String name) throws DomainException;

    /**
     * Printed servers.
     *
     * @since 0.1.0
     */
    final class Printed implements Text {
        /**
         * Stands.
         */
        private final Stands stands;

        /**
         * Main ctor.
         * @param stands Stands
         */
        public Printed(final Stands stands) {
            this.stands = stands;
        }

        @Override
        public String asString() throws DomainException {
            return this.stands.stands().stream()
                .map(st -> new Stand.Printed(st).toString())
                .collect(Collectors.joining());
        }
    }
}
