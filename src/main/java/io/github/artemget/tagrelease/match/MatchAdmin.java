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
import java.util.Set;
import java.util.function.Predicate;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Matches admin/s or not.
 *
 * @since 0.1.0
 */
public final class MatchAdmin implements Predicate<Wrap<Update>> {
    /**
     * Admin ids.
     */
    private final Entry<Set<String>> admins;

    /**
     * Main ctor.
     *
     * @param admins Of bot.
     */
    public MatchAdmin(final Entry<Set<String>> admins) {
        this.admins = admins;
    }

    @Override
    public boolean test(final Wrap<Update> update) {
        try {
            return this.admins.value()
                .contains(update.src().getMessage().getFrom().getId().toString());
        } catch (final EntryException exception) {
            throw new EntryExceptionUnchecked(exception);
        }
    }
}
