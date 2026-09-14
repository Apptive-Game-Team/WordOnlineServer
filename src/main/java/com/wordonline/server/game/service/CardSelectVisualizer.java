package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.statuseffect.IdleAuraStatusEffect;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Puts an idle aura of the aimed card's element on the caster, so the opponent can see a cast
 * coming. The element comes from {@code magics.element} now rather than from an element card.
 */
@RequiredArgsConstructor
public class CardSelectVisualizer {

    private final DatabaseMagicParser magicParser;

    public void selectCard(GameContext gameContext, long userId, long magicId) {
        toElement(magicId).ifPresent(element -> {
            GameObject player = findPlayer(gameContext, userId);
            if (hasIdleAura(player, element)) {
                return;
            }
            player.addComponent(new IdleAuraStatusEffect(player, element));
        });
    }

    public void unselectCard(GameContext gameContext, long userId, long magicId) {
        toElement(magicId).ifPresent(element -> findIdleAuras(findPlayer(gameContext, userId))
                .filter(effect -> effect.getElement() == element)
                .forEach(IdleAuraStatusEffect::cancel));
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

    /** A magic with no element gets no aura, the way a cast type card used to get none. */
    private Optional<ElementType> toElement(long magicId) {
        Magic magic = magicParser.getMagic(magicId);
        if (magic == null || magic.element == null || magic.element == ElementType.NONE) {
            return Optional.empty();
        }
        return Optional.of(magic.element);
    }
}
