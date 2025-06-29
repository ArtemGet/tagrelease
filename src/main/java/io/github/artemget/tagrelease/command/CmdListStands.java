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

package io.github.artemget.tagrelease.command;

import io.github.artemget.tagrelease.domain.Stand;
import io.github.artemget.tagrelease.domain.Stands;
import io.github.artemget.tagrelease.exception.DomainException;
import io.github.artemget.teleroute.command.Cmd;
import io.github.artemget.teleroute.command.CmdException;
import io.github.artemget.teleroute.send.Send;
import io.github.artemget.teleroute.telegrambots.send.SendMessageWrap;
import java.util.List;
import java.util.stream.Collectors;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

/**
 * Lists available stands and it's services.
 *
 * @since 0.1.0
 */
public final class CmdListStands implements Cmd<Update, AbsSender> {
    /**
     * Available stands.
     */
    private final Stands stands;

    /**
     * Main ctor.
     * @param stands Available stands.
     */
    public CmdListStands(final Stands stands) {
        this.stands = stands;
    }

    @Override
    public Send<AbsSender> execute(final Update update) throws CmdException {
        final List<Stand> stands;
        try {
            stands = this.stands.stands();
        } catch (final DomainException exception) {
            throw new CmdException(
                String.format(
                    "Error list all stands. From user:'%s', userId:'%s' in chat:'%s'",
                    update.getMessage().getFrom().getUserName(),
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getChatId()
                ),
                exception
            );
        }
        final SendMessage message = new SendMessage(
            update.getMessage().getChatId().toString(),
            stands.stream().map(Stand::name).collect(Collectors.joining("\n"))
        );
        message.setReplyToMessageId(update.getMessage().getMessageId());
        message.enableMarkdownV2(true);
        return new SendMessageWrap<>(message);
    }
}
