package io.github.artemget.tagrelease.command;

import io.github.artemget.teleroute.command.Cmd;
import io.github.artemget.teleroute.command.CmdException;
import io.github.artemget.teleroute.send.Send;
import io.github.artemget.teleroute.telegrambots.send.SendMessageWrap;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
public class CmdHelp implements Cmd<Update, AbsSender> {
    @Override
    public Send<AbsSender> execute(Update update) throws CmdException {
        final SendMessage message = new SendMessage(
            update.getMessage().getChatId().toString(),
            """
                ```
                Эхо
                ```
                Показать telegram id пользователя
                                
                ```
                Покажи сервисы
                ```
                Показать список всех сервисов
                
                ```
                Покажи стенды
                ```
                Показать список стендов
                
                ```
                Покажи сервисы {стенд}
                ```
                Показать список сервисов на конкретном стенде
                
                ```
                Покажи тег {сервис1,сервис2} префикс {v4.*}
                ```
                Показать список текущих тегов по префиксу
                
                ```
                Собери тег {сервис1,сервис2} префикс {v4.*}
                Собери тег {сервис1,сервис2} префикс {v4.*} ветка {ветка}
                ```
                Собрать тег от ветки develop/выбранной ветки, номер версии текущая плюс 1 в разряде плейсхолдера
                
                """
        );
        message.enableMarkdownV2(true);
        return new SendMessageWrap<>(message);
    }
}
