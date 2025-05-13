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

import java.util.List;
import java.util.stream.Collectors;
import org.cactoos.Text;

/**
 * Applications.
 *
 * @since 0.1.0
 */
public interface Services {
    /**
     * Returns all services from stand.
     *
     * @return Services
     */
    List<Service> services();

    /**
     * Returns service from stand by it's name.
     *
     * @param name Of service
     * @return Service
     */
    Service service(final String name);

    /**
     * Printed Services.
     * Format: ```java %s:%s```\n
     *
     * @since 0.1.0
     */
    final class Printed implements Text {
        /**
         * Services.
         */
        private final Services services;

        /**
         * Main ctor.
         *
         * @param services Services
         */
        public Printed(final Services services) {
            this.services = services;
        }

        @Override
        public String asString() {
            return this.services.services().stream()
                .map(service -> String.format("```java %s:%s```\n    ", service.name(), service.tag()))
                .collect(Collectors.joining());
        }
    }
}
