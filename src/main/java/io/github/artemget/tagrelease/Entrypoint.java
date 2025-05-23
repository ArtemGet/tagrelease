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

package io.github.artemget.tagrelease;

import io.github.artemget.entrys.EntryException;
import io.github.artemget.entrys.system.EEnv;
import io.github.artemget.tagrelease.bot.Bot;
import io.github.artemget.tagrelease.bot.BotReg;
import io.github.artemget.tagrelease.command.CmdListStand;
import io.github.artemget.tagrelease.command.CmdListStands;
import io.github.artemget.tagrelease.domain.StandsGitlab;
import io.github.artemget.tagrelease.entry.ESplit;
import io.github.artemget.tagrelease.match.MatchAdmin;
import io.github.artemget.tagrelease.match.MatchChats;
import io.github.artemget.teleroute.match.MatchAll;
import io.github.artemget.teleroute.match.MatchRegex;
import io.github.artemget.teleroute.route.RouteDfs;
import io.github.artemget.teleroute.route.RouteEnd;
import io.github.artemget.teleroute.route.RouteFork;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Entrypoint. Application starts here.
 *
 * @since 0.1.0
 * @checkstyle HideUtilityClassConstructorCheck (20 lines)
 */
@SuppressWarnings(
    {
        "AnnotationUseStyleCheck",
        "PMD.HideUtilityClassConstructorCheck",
        "PMD.ProhibitPublicStaticMethods",
        "PMD.UseUtilityClass"
    }
)
public class Entrypoint {
    public static void main(final String[] args) throws EntryException, TelegramApiException {
        new BotReg(
            new Bot(
                new EEnv("BOT_NAME"),
                new EEnv("BOT_TOKEN"),
                new RouteFork<>(
                    new MatchAll<>(
                        new MatchChats(new ESplit(new EEnv("BOT_CHATS"))),
                        new MatchAdmin(new ESplit(new EEnv("BOT_ADMINS")))
                    ),
                    new RouteDfs<>(
                        new RouteFork<>(
                            new MatchRegex<>("Покажи сервисы"),
                            new CmdListStands(new StandsGitlab())
                        ),
                        new RouteFork<>(
                            new MatchRegex<>("Покажи сервис"),
                            new CmdListStand(new StandsGitlab())
                        ),
                        new RouteFork<>(
                            new MatchRegex<>("Собери тэги по стенду"),
                            new RouteEnd<>(),
                            new RouteFork<>(
                                new MatchRegex<>("Собери тэг"),
                                new RouteEnd<>()
                            )
                        )
                    )
                )
            )
        ).registered();
    }
}
