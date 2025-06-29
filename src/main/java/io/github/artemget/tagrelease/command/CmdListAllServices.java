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

import io.github.artemget.entrys.Entry;
import io.github.artemget.tagrelease.domain.Service;
import io.github.artemget.tagrelease.domain.Services;
import io.github.artemget.tagrelease.domain.ServicesAll;
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
 * Bot command.
 * List all repositories in gitlab and send reply to user message.
 *
 * @since 0.0.1
 */
public final class CmdListAllServices implements Cmd<Update, AbsSender> {
    private final Services services;

    public CmdListAllServices(final Entry<String> host, final Entry<String> project) {
        this(new ServicesAll(host, project));
    }

    public CmdListAllServices(final Services services) {
        this.services = services;
    }

    @Override
    public Send<AbsSender> execute(final Update update) throws CmdException {
        List<Service> srvs;
        try {
            srvs = this.services.services();
        } catch (final DomainException exception) {
            throw new CmdException(
                String.format(
                    "Error list all services. From user:'%s', userId:'%s' in chat:'%s'",
                    update.getMessage().getFrom().getUserName(),
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getChatId()
                ),
                exception
            );
        }
        final SendMessage message = new SendMessage(
            update.getMessage().getChatId().toString(),
            String.format(
                "```\n%s```",
                srvs.stream().map(Service::name).collect(Collectors.joining("\n"))
            )
        );
        message.setReplyToMessageId(update.getMessage().getMessageId());
        message.enableMarkdownV2(true);
        return new SendMessageWrap<>(message);
    }
}
