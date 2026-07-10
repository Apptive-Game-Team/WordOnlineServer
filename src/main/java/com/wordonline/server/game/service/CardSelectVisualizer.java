package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.statuseffect.IdleAuraStatusEffect;

import java.util.Optional;
import java.util.stream.Stream;

public class CardSelectVisualizer {

    public void selectCard(GameContext gameContext, long userId, CardType card) {
        toElement(card).ifPresent(element -> {
            GameObject player = findPlayer(gameContext, userId);
            if (hasIdleAura(player, element)) {
                return;
            }
            player.addComponent(new IdleAuraStatusEffect(player, element));
        });
    }

    public void unselectCard(GameContext gameContext, long userId, CardType card) {
        toElement(card).ifPresent(element -> findIdleAuras(findPlayer(gameContext, userId))
                .filter(effect -> effect.getElement() == element)
                .forEach(IdleAuraStatusEffect::cancel));
    }

    public void unselectAll(GameContext gameContext, long userId) {
        findIdleAuras(findPlayer(gameContext, userId))
                .forEach(IdleAuraStatusEffect::cancel);
    }

    private GameObject findPlayer(GameContext gameContext, long userId) {
        return gameContext.findPlayerGameObject(userId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + userId));
    }

    private boolean hasIdleAura(GameObject player, ElementType element) {
        return findIdleAuras(player)
                .anyMatch(effect -> effect.getElement() == element);
    }

    private Stream<IdleAuraStatusEffect> findIdleAuras(GameObject player) {
        return Stream.concat(
                player.getComponents(IdleAuraStatusEffect.class).stream(),
                player.getComponentsToAdd().stream()
                        .filter(IdleAuraStatusEffect.class::isInstance)
                        .map(IdleAuraStatusEffect.class::cast)
        );
    }

    private Optional<ElementType> toElement(CardType card) {
        return switch (card) {
            case Fire -> Optional.of(ElementType.FIRE);
            case Water -> Optional.of(ElementType.WATER);
            case Nature -> Optional.of(ElementType.NATURE);
            case Lightning -> Optional.of(ElementType.LIGHTNING);
            case Rock -> Optional.of(ElementType.ROCK);
            case Wind -> Optional.of(ElementType.WIND);
            default -> Optional.empty();
        };
    }
}
