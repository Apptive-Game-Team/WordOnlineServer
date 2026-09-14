package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.statuseffect.CardSelectedStatusEffect;
import com.wordonline.server.game.domain.object.component.effect.statuseffect.IdleAuraStatusEffect;

import java.util.Optional;
import java.util.stream.Stream;

public class CardSelectVisualizer {

    public void selectCard(GameContext gameContext, long userId, CardType card) {
        GameObject player = findPlayer(gameContext, userId);
        selectIdleAura(player, card);
        findCardSelected(player).ifPresentOrElse(
                CardSelectedStatusEffect::select,
                () -> player.addComponent(new CardSelectedStatusEffect(player)));
    }

    public void unselectCard(GameContext gameContext, long userId, CardType card) {
        GameObject player = findPlayer(gameContext, userId);
        unselectIdleAura(player, card);
        findCardSelected(player).ifPresent(CardSelectedStatusEffect::unselect);
    }

    public void unselectAll(GameContext gameContext, long userId) {
        GameObject player = findPlayer(gameContext, userId);
        findIdleAuras(player).forEach(IdleAuraStatusEffect::cancel);
        findCardSelected(player).ifPresent(CardSelectedStatusEffect::cancel);
    }

    private void selectIdleAura(GameObject player, CardType card) {
        toElement(card).ifPresent(element -> {
            if (hasIdleAura(player, element)) {
                return;
            }
            player.addComponent(new IdleAuraStatusEffect(player, element));
        });
    }

    private void unselectIdleAura(GameObject player, CardType card) {
        toElement(card).ifPresent(element -> findIdleAuras(player)
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

    // An expired marker stays in the component list until the loop flushes removals at the end of
    // the frame, so skip it: selecting a card in the same frame as a cast must build a new one
    // rather than count up an effect the player object has already dropped.
    private Optional<CardSelectedStatusEffect> findCardSelected(GameObject player) {
        return Stream.concat(
                        player.getComponents(CardSelectedStatusEffect.class).stream(),
                        player.getComponentsToAdd().stream()
                                .filter(CardSelectedStatusEffect.class::isInstance)
                                .map(CardSelectedStatusEffect.class::cast))
                .filter(effect -> !effect.isExpired())
                .findFirst();
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
