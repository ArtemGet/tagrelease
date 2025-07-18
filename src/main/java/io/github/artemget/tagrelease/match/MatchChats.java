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

package io.github.artemget.tagrelease.match;

import io.github.artemget.entrys.Entry;
import io.github.artemget.entrys.EntryException;
import io.github.artemget.entrys.EntryExceptionUnchecked;
import io.github.artemget.teleroute.update.Wrap;
import java.util.List;
import java.util.function.Predicate;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Matches chat/s in which bot works.
 *
 * @since 0.1.0
 */
public final class MatchChats implements Predicate<Wrap<Update>> {
    /**
     * Chat ids.
     */
    private final Entry<List<String>> chats;

    /**
     * Main ctor.
     *
     * @param chats Where bot is used.
     */
    public MatchChats(final Entry<List<String>> chats) {
        this.chats = chats;
    }

    @Override
    public boolean test(final Wrap<Update> update) {
        try {
            return this.chats.value()
                .contains(update.src().getMessage().getChatId().toString());
        } catch (final EntryException exception) {
            throw new EntryExceptionUnchecked(exception);
        }
    }
}
