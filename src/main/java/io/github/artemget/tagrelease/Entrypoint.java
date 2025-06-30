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

import io.github.artemget.entrys.Entry;
import io.github.artemget.entrys.EntryException;
import io.github.artemget.entrys.file.EVal;
import io.github.artemget.entrys.operation.ESplit;
import io.github.artemget.tagrelease.bot.Bot;
import io.github.artemget.tagrelease.bot.BotReg;
import io.github.artemget.tagrelease.command.CmdBuildTags;
import io.github.artemget.tagrelease.command.CmdListServices;
import io.github.artemget.tagrelease.command.CmdListServicesAll;
import io.github.artemget.tagrelease.command.CmdListStands;
import io.github.artemget.tagrelease.domain.Services;
import io.github.artemget.tagrelease.domain.ServicesAll;
import io.github.artemget.tagrelease.domain.Stands;
import io.github.artemget.tagrelease.domain.StandsGl;
import io.github.artemget.tagrelease.match.MatchAdmin;
import io.github.artemget.teleroute.match.MatchRegex;
import io.github.artemget.teleroute.route.RouteDfs;
import io.github.artemget.teleroute.route.RouteFork;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Entrypoint. Application starts here.
 * @checkstyle HideUtilityClassConstructorCheck (20 lines)
 * @since 0.1.0
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
        final Entry<String> host = new EVal("provider.host");
        final Entry<String> release = new EVal("provider.release");
        final Entry<String> repo = new EVal("provider.token");
        final Entry<String> token = new EVal("provider.token");
        final Entry<String> project = new EVal("provider.project");
        final Services all = new ServicesAll(host, project, token);
        final Stands stands = new StandsGl(host, release, repo, token);
        new BotReg(
            new Bot(
                new EVal("bot.name"),
                new EVal("bot.token"),
                new RouteFork<>(
                    new MatchAdmin(new ESplit(new EVal("bot.admins"))),
                    new RouteDfs<>(
                        new RouteFork<>(
                            new MatchRegex<>("[Пп]окажи сервисы"),
                            new CmdListServicesAll(all)
                        ),
                        new RouteFork<>(
                            new MatchRegex<>("[Пп]окажи сервисы \\{([^{}]*)\\}$"),
                            new CmdListServices(stands)
                        ),
                        new RouteFork<>(
                            new MatchRegex<>("[Пп]окажи стенды"),
                            new CmdListStands(stands)
                        ),
                        new RouteFork<>(
                            new MatchRegex<>("[Сс]обери сервисы \\{([^{}]*)\\}$ префикс \\{([^{}]*)\\}$"),
                            new CmdBuildTags(all)
                        )
//                        new RouteFork<>(
//                            new MatchRegex<>("Покажи стенд"),
//                            new CmdListStand(new StandsGitlab())
//                        ),
//                        new RouteFork<>(
//                            new MatchRegex<>("Собери тэги по стенду"),
//                            new RouteEnd<>(),
//                            new RouteFork<>(
//                                new MatchRegex<>("Собери тэг"),
//                                new RouteEnd<>()
//                            )
//                        )
                    )
                )
            )
        ).registered();
    }
}
