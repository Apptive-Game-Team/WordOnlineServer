package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.domain.magic.ElementType;
import java.util.*;
import static java.util.Collections.unmodifiableSet;

public class Element {
    private final EnumSet<ElementType> nativeSet = EnumSet.noneOf(ElementType.class);
    private final EnumMap<ElementType, Set<Object>> bonusSources = new EnumMap<>(ElementType.class);

    public Set<ElementType> total() {
        EnumSet<ElementType> out = EnumSet.copyOf(nativeSet);
        for (var e : bonusSources.entrySet()) {
            if (!e.getValue().isEmpty()) out.add(e.getKey());
        }
        if (out.isEmpty()) out.add(ElementType.NONE);
        return unmodifiableSet(out);
    }

    public boolean has(ElementType e) {
        if (nativeSet.contains(e)) return true;
        var src = bonusSources.get(e);
        return src != null && !src.isEmpty();
    }

    public void addNative(ElementType e) { nativeSet.add(e); }
    public void removeNative(ElementType e) { nativeSet.remove(e); }

    public void addBonus(ElementType e, Object source) {
        bonusSources.computeIfAbsent(e, k -> java.util.Collections.newSetFromMap(new IdentityHashMap<>()))
                .add(source);
    }

    public void removeBonus(ElementType e, Object source) {
        var set = bonusSources.get(e);
        if (set == null) return;
        set.remove(source);
        if (set.isEmpty()) bonusSources.remove(e);
    }

    public void clearBonusFrom(Object source) {
        bonusSources.values().forEach(s -> s.remove(source));
        bonusSources.values().removeIf(Set::isEmpty);
    }
}
