package com.wordonline.server.game.service.bot;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.repository.TagRepository;
import com.wordonline.server.game.service.MagicMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BotCounterEvaluator {

    private final MagicMetadataService magicMetadataService;
    private final TagRepository tagRepository;

    public double evaluate(List<CardType> recipe, List<GameObject> enemies) {
        Magic magic = magicMetadataService.findMagic(recipe).orElse(null);
        if (magic == null || magic.id <= 0 || enemies.isEmpty()) {
            return 0.0;
        }

        Set<String> magicTags = magicMetadataService.getMagicTags(magic.id);
        if (magicTags.isEmpty()) {
            return 0.0;
        }

        double score = 0.0;
        for (GameObject enemy : enemies) {
            Set<String> targetTags = tagRepository.getGameObjectTags(enemy.getType());
            score += tagRepository.getCounterWeight(magicTags, targetTags);
        }
        return score;
    }
}
