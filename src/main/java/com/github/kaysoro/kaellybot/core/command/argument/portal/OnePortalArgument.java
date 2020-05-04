package com.github.kaysoro.kaellybot.core.command.argument.portal;

import com.github.kaysoro.kaellybot.core.command.argument.model.AbstractCommandArgument;
import com.github.kaysoro.kaellybot.core.command.classic.PortalCommand;
import com.github.kaysoro.kaellybot.core.mapper.PortalMapper;
import com.github.kaysoro.kaellybot.core.model.constant.Constants;
import com.github.kaysoro.kaellybot.core.model.constant.Dimension;
import com.github.kaysoro.kaellybot.core.model.constant.Language;
import com.github.kaysoro.kaellybot.core.model.constant.Server;
import com.github.kaysoro.kaellybot.core.service.PortalService;
import com.github.kaysoro.kaellybot.core.util.PermissionScope;
import com.github.kaysoro.kaellybot.core.util.Translator;
import discord4j.core.object.entity.Message;

import java.util.regex.Matcher;

public class OnePortalArgument extends AbstractCommandArgument {

    private PortalService portalService;
    private PortalMapper portalMapper;

    public OnePortalArgument(PortalCommand parent, PortalService portalService, PortalMapper portalMapper, Translator translator){
        super(parent, "\\s+(\\w+)\\s+(.+)", true, PermissionScope.EMBED_PERMISSIONS, translator);
        this.portalService = portalService;
        this.portalMapper = portalMapper;
    }

    @Override
    public void execute(Message message, Matcher matcher) {
       // TODO determine server & dimension in the message
        Server server = Server.MERIANA;
        Dimension dimension = Dimension.ENUTROSOR;

        portalService.getPortal(server, dimension, Constants.DEFAULT_LANGUAGE)
                .doOnSuccess(portal -> message.getChannel()
                        .flatMap(channel -> channel.createEmbed(spec ->
                                portalMapper.decorateSpec(spec, portal, Constants.DEFAULT_LANGUAGE)))
                        .subscribe()
                )
                .doOnError(error -> manageUnknownException(message, error))
                .subscribe();
    }

    @Override
    public String help(Language lg, String prefix){
        return prefix + "`" + getParent().getName() + " dimension server` : "
                + translator.getLabel(lg, "pos.one_portal");
    }
}