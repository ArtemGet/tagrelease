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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TagsGlTest {

    @Test
    void returnsLastIncremented() {
        Assertions.assertEquals(
            "4.3.1.1",
            TagsGl.next("4.3.1.0", "4.3.1.*")
        );
    }

    @Test
    void returnsLastIncrementedWithStarting() {
        Assertions.assertEquals(
            "v4.3.1.1",
            TagsGl.next("v4.3.1.0", "v4.3.1.*")
        );
    }

    @Test
    void returnsLastIncrementedWithVersionAndTrailingText() {
        Assertions.assertEquals(
            "v4.3.1.1_bla_bla",
            TagsGl.next("v4.3.1.0_bla_bla", "v4.3.1.*")
        );
    }

    @Test
    void returnsLastIncrementedOverPlaceNumber() {
        Assertions.assertEquals(
            "4.3.1.11",
            TagsGl.next("4.3.1.10", "4.3.1.*")
        );
    }

    @Test
    void returnsThirdIncremented() {
        Assertions.assertEquals(
            "4.3.2.0",
            TagsGl.next("4.3.1.0", "4.3.*")
        );
    }

    @Test
    void returnsThirdIncrementedOverPlaceNumber() {
        Assertions.assertEquals(
            "4.3.11.0",
            TagsGl.next("4.3.10.0", "4.3.*")
        );
    }

    @Test
    void returnsSecondIncremented() {
        Assertions.assertEquals(
            "4.4.0.0",
            TagsGl.next("4.3.1.0", "4.*")
        );
    }

    @Test
    void returnsFirstIncremented() {
        Assertions.assertEquals(
            "5.0.0.0",
            TagsGl.next("4.3.1.0", "*")
        );
    }

    @Test
    void returnsFirstIncrementedOver() {
        Assertions.assertEquals(
            "10.0.0.0",
            TagsGl.next("9.3.1.0", "*")
        );
    }

    @Test
    void returnsFirstIncrementedOverPlaceNumber() {
        Assertions.assertEquals(
            "11.0.0.0",
            TagsGl.next("10.3.1.0", "*")
        );
    }
}
