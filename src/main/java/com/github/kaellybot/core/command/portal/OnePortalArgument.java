package com.github.kaellybot.core.command.portal;

import com.github.kaellybot.commons.model.constants.Language;
import com.github.kaellybot.commons.model.entity.Dimension;
import com.github.kaellybot.commons.model.entity.Server;
import com.github.kaellybot.core.command.model.Command;
import com.github.kaellybot.core.util.PermissionScope;
import com.github.kaellybot.core.command.model.AbstractCommandArgument;
import com.github.kaellybot.core.mapper.PortalMapper;
import com.github.kaellybot.core.service.PortalService;
import com.github.kaellybot.core.util.DiscordTranslator;
import discord4j.core.object.entity.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.regex.Matcher;

@Component
@Qualifier(PortalCommand.COMMAND_QUALIFIER)
public class OnePortalArgument extends AbstractCommandArgument {

    private final PortalService portalService;
    private final PortalMapper portalMapper;

    public OnePortalArgument(@Qualifier(PortalCommand.COMMAND_QUALIFIER) Command parent, PortalService portalService,
                             PortalMapper portalMapper, DiscordTranslator translator){
        super(parent, "\\s+(\\w+)\\s+(.+)", true, PermissionScope.EMBED_PERMISSIONS, translator);
        this.portalService = portalService;
        this.portalMapper = portalMapper;
    }

    @Override
    public Flux<Message> execute(Message message, String prefix, Language language, Matcher matcher) {
       // TODO determine server & dimension in the message
        Server server = Server.builder().labels(Map.of(language, "Mériana")).build();
        Dimension dimension = Dimension.builder().labels(Map.of(language, "Enutrosor")).build();

        return portalService.getPortal(server, dimension, language)
                .flatMap(portal -> message.getChannel().flatMap(channel -> channel
                        .createEmbed(spec -> portalMapper.decorateSpec(spec, portal, language))))
                .flatMapMany(Flux::just);
    }

    @Override
    public String help(Language lg, String prefix){
        return prefix + "`" + getParent().getName() + " dimension server` : "
                + translator.getLabel(lg, "pos.one_portal");
    }
}