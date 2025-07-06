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
import io.github.artemget.entrys.EntryException;
import io.github.artemget.entrys.operation.ESplit;
import io.github.artemget.entrys.operation.EUnwrap;
import io.github.artemget.tagrelease.domain.Service;
import io.github.artemget.tagrelease.domain.Services;
import io.github.artemget.tagrelease.domain.ServicesAll;
import io.github.artemget.tagrelease.domain.Tag;
import io.github.artemget.tagrelease.domain.Tags;
import io.github.artemget.tagrelease.domain.TagsGl;
import io.github.artemget.tagrelease.exception.DomainException;
import io.github.artemget.teleroute.command.Cmd;
import io.github.artemget.teleroute.command.CmdException;
import io.github.artemget.teleroute.send.Send;
import io.github.artemget.teleroute.telegrambots.send.SendMessageWrap;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class CmdBuildTags implements Cmd<Update, AbsSender> {
    private final Logger log = LoggerFactory.getLogger(CmdBuildTags.class);
    private final Services services;
    private final Tags tags;

    public CmdBuildTags(
        final Entry<String> host,
        final Entry<String> project,
        final Entry<String> token
    ) {
        this(new ServicesAll(host, project, token), new TagsGl(host, token));
    }

    public CmdBuildTags(final Services services, final Tags tags) {
        this.services = services;
        this.tags = tags;
    }

    @Override
    public Send<AbsSender> execute(Update update) throws CmdException {
        final List<String> names;
        final String branch;
        final String prefix;
        final String[] values = StringUtils.substringsBetween(update.getMessage().getText(), "{", "}");
        try {
            names = new ESplit(new EUnwrap(values[0]), ",").value();
        } catch (final EntryException exception) {
            throw new CmdException("Failed to parse service names for tag build", exception);
        }
        try {
            prefix = new EUnwrap(values[1]).value();
        } catch (final EntryException exception) {
            throw new CmdException("Failed to parse prefix name for tag build", exception);
        }
        try {
            if (values.length >= 3) {
                branch = new EUnwrap(values[2]).value();
            } else {
                branch = "develop";
            }
        } catch (final EntryException exception) {
            throw new CmdException("Failed to parse branch name for tag build", exception);
        }
        final List<Tag> succeed = new ArrayList<>();
        final List<String> failed = new ArrayList<>();
        for (final String name : names) {
            Service service;
            try {
                service = this.services.service(name);
            } catch (final DomainException exception) {
                log.error("Failed to fetch service:'{}' for tag build", name, exception);
                failed.add(name);
                continue;
            }
            final Tag tag;
            try {
                tag = this.tags.buildNew(service.id(), branch, prefix);
            } catch (final DomainException exception) {
                log.error("Failed to build tag for service:'{}'", name, exception);
                failed.add(name);
                continue;
            }
            succeed.add(tag);
        }
        final SendMessage message = new SendMessage(
            update.getMessage().getChatId().toString(),
            String.format(
                "Собраны сервисы:\n%s\nОшибка сборки сервисов:\n%s",
                new Tag.Printed(succeed).asString(),
                String.join("\n", failed)
            )
        );
        message.setReplyToMessageId(update.getMessage().getMessageId());
        message.enableMarkdownV2(true);
        return new SendMessageWrap<>(message);
    }
}
