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
import org.cactoos.Text;

/**
 * Server.
 *
 * @since 0.1.0
 */
public interface Stand {
    /**
     * Returns server name.
     *
     * @return Name
     */
    String name();

    /**
     * Returns server's services.
     *
     * @return Services
     */
    Services services() throws DomainException;

    /**
     * Printed server.
     * Format:
     *  Стенд: %s
     *  Сервисы:
     *      %s
     *
     * @since 0.1.0
     */
    final class Printed implements Text {
        /**
         * Server.
         */
        private final Stand stand;

        /**
         * Main ctor.
         *
         * @param stand Stand
         */
        public Printed(final Stand stand) {
            this.stand = stand;
        }

        @Override
        public String asString() throws DomainException {
            return String.format(
                """
                    Стенд: %s
                    Сервисы:
                        %s
                    """,
                this.stand.name(),
                new Services.Printed(this.stand.services())
            );
        }
    }
}
