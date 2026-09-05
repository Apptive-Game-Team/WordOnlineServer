package com.wordonline.server.game.domain.bot.view;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

import java.util.Set;

/**
 * Where the world view gets a prefab's gameplay tags.
 *
 * <p>An interface rather than the service itself so the bot can be built without a database
 * behind it. A bot that cannot read tags still plays - it just scores without them - and a test
 * that is not about tags should not have to stand up a repository to say so.
 */
@FunctionalInterface
public interface GameObjectTags {

    /** The tags of a prefab, empty when it has none. Must not touch the database. */
    Set<String> tagsOf(PrefabType prefabType);

    /** No tag data at all. Every tag-based condition simply fails to match. */
    static GameObjectTags none() {
        return prefabType -> Set.of();
    }
}
