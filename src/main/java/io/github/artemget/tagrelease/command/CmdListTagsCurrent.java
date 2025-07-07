package io.github.artemget.tagrelease.command;

import io.github.artemget.entrys.Entry;
import io.github.artemget.entrys.EntryException;
import io.github.artemget.entrys.fake.EFake;
import io.github.artemget.entrys.operation.ESplit;
import io.github.artemget.tagrelease.domain.Service;
import io.github.artemget.tagrelease.domain.Services;
import io.github.artemget.tagrelease.domain.ServicesAll;
import io.github.artemget.tagrelease.domain.Tag;
import io.github.artemget.tagrelease.domain.TagEa;
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

public class CmdListTagsCurrent implements Cmd<Update, AbsSender> {
    private final Logger log = LoggerFactory.getLogger(CmdListTagsCurrent.class);
    private final Services services;
    private final Tags tags;

    public CmdListTagsCurrent(
        final Entry<String> host,
        final Entry<String> project,
        final Entry<String> token
    ) {
        this(new ServicesAll(host, project, token), new TagsGl(host, token));
    }

    public CmdListTagsCurrent(final Services services, final Tags tags) {
        this.services = services;
        this.tags = tags;
    }

    @Override
    public Send<AbsSender> execute(Update update) throws CmdException {
        final List<String> names;
        final String[] values = StringUtils.substringsBetween(update.getMessage().getText(), "{", "}");
        try {
            names = new ESplit(new EFake<>(values[0]), ",").value();
        } catch (final EntryException exception) {
            throw new CmdException("Failed to parse service names for tag fetch", exception);
        }
        final String prefix = values[1];
        final String branch;
        if (values.length >= 3) {
            branch = values[2];
        } else {
            branch = "develop";
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
                tag = this.tags.current(service.id(), branch, prefix);
            } catch (final DomainException exception) {
                log.error("Failed to fetch current tag for service:'{}'", name, exception);
                failed.add(name);
                continue;
            }
            succeed.add(new TagEa(service.name(), tag.name(), tag.branch(), tag.fromCommit(), tag.message(), tag.created()));
        }
        final SendMessage message = new SendMessage(
            update.getMessage().getChatId().toString(),
            String.format("Найдены теги:\n%s", new Tag.Printed(succeed).asString())
                .concat(CmdListTagsCurrent.checked(failed))
        );
        message.setReplyToMessageId(update.getMessage().getMessageId());
        message.enableMarkdownV2(true);
        return new SendMessageWrap<>(message);
    }

    private static String checked(List<String> failed) {
        final String message;
        if (failed.isEmpty()) {
            message = "";
        } else {
            message = String.format(
                "\nОшибка поиска тегов по сервисам:\n%s",
                String.format("```\n%s\n```", String.join("\n", failed))
            );
        }
        return message;
    }
}
