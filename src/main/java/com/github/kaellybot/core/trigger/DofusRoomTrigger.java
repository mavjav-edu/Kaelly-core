package com.github.kaellybot.core.trigger;

import com.github.kaellybot.core.mapper.DofusRoomPreviewMapper;
import com.github.kaellybot.core.model.constant.Constants;
import com.github.kaellybot.core.payload.dofusroom.StatusDto;
import com.github.kaellybot.core.service.DofusRoomService;
import com.github.kaellybot.core.util.DiscordTranslator;
import com.github.kaellybot.core.util.PermissionScope;
import discord4j.core.object.entity.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DofusRoomTrigger extends AbstractTrigger {

    private final List<Pattern> dofusRoomUrlPatterns;

    private final DofusRoomService dofusRoomService;

    private final DofusRoomPreviewMapper dofusRoomPreviewMapper;

    public DofusRoomTrigger(DiscordTranslator translator, DofusRoomService dofusRoomService,
                            DofusRoomPreviewMapper dofusRoomPreviewMapper){
        super(translator, PermissionScope.EMBED_PERMISSIONS);
        this.dofusRoomService = dofusRoomService;
        this.dofusRoomPreviewMapper = dofusRoomPreviewMapper;
        this.dofusRoomUrlPatterns = Constants.DOFUS_ROOM_BUILD_URL.parallelStream()
                .map(url -> Pattern.compile(url + "(\\d+)"))
                .collect(Collectors.toList());
    }

    @Override
    protected boolean isPatternFound(Message message){
        return dofusRoomUrlPatterns.parallelStream()
                .map(pattern -> pattern.matcher(message.getContent()).find())
                .reduce(Boolean::logicalOr)
                .orElse(false);
    }

    @Override
    public Flux<Message> execute(Message message) {
        return Flux.fromStream(dofusRoomUrlPatterns.parallelStream()
                .map(pattern -> pattern.matcher(message.getContent()))
                .flatMap(this::findAllDofusRoomIds)
                .distinct()).collectList()
                .zipWith(translator.getLanguage(message))
                .flatMapMany(tuple -> Flux.fromIterable(tuple.getT1())
                        .flatMap(id -> dofusRoomService.getDofusRoomPreview(id, tuple.getT2()))
                        .filter(preview -> StatusDto.SUCCESS.equals(preview.getStatus()))
                        .collectList()
                        .zipWith(message.getChannel())
                        .flatMapMany(tuple2 -> Flux.fromIterable(tuple2.getT1())
                                .flatMap(preview -> tuple2.getT2().createMessage(spec -> dofusRoomPreviewMapper
                                        .decorateSpec(spec, preview, tuple.getT2())))))
                .onErrorResume(error -> manageUnknownException(message, error));
    }

    private Stream<String> findAllDofusRoomIds(Matcher m){
        List<String> ids = new ArrayList<>();
        while(m.find()) ids.add(m.group(1));
        return ids.parallelStream();
    }
}