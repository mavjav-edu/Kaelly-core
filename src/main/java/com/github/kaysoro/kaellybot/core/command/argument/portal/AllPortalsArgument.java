package com.github.kaysoro.kaellybot.core.command.argument.portal;

import com.github.kaysoro.kaellybot.core.command.argument.model.AbstractCommandArgument;
import com.github.kaysoro.kaellybot.core.command.classic.PortalCommand;
import com.github.kaysoro.kaellybot.core.mapper.PortalMapper;
import com.github.kaysoro.kaellybot.core.model.constant.Constants;
import com.github.kaysoro.kaellybot.core.model.constant.Language;
import com.github.kaysoro.kaellybot.core.model.constant.Server;
import com.github.kaysoro.kaellybot.core.service.PortalService;
import com.github.kaysoro.kaellybot.core.util.Translator;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Flux;

import java.util.regex.Matcher;

public class AllPortalsArgument extends AbstractCommandArgument {

    private PortalService portalService;
    private PortalMapper portalMapper;

    public AllPortalsArgument(PortalCommand parent, PortalService portalService, PortalMapper portalMapper, Translator translator){
        super(parent, "\\s+(\\w+)", true, translator);
        this.portalService = portalService;
        this.portalMapper = portalMapper;
    }

    @Override
    public void execute(Message message, Matcher matcher) {
       // TODO determine server in the message
        Server server = Server.MERIANA;

        portalService.getPortals(server, Constants.DEFAULT_LANGUAGE)
                .collectList()
                .doOnSuccess(portals -> message.getChannel()
                        .flatMap(channel -> Flux.fromIterable(portals)
                                .flatMap(portal -> channel.createEmbed(spec -> portalMapper
                                        .decorateSpec(spec, portal, Constants.DEFAULT_LANGUAGE)))
                                .collectList())
                        .subscribe())
                .doOnError(error -> manageUnknownException(message, error))
                .subscribe();
    }

    @Override
    public String help(Language lg, String prefix){
        return prefix + "`" + getParent().getName() + " server` : "
                + translator.getLabel(lg, "pos.all_portals");
    }
}