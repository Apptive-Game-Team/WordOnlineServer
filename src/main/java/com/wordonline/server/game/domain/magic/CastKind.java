package com.wordonline.server.game.domain.magic;

import java.util.Optional;

/**
 * 서버가 그 마법을 어떻게 만드는가. {@code magics.cast_kind} 열의 값이다.
 *
 * <p>조합에 쓰는 {@link CardType} 과는 다른 축이다 — {@code tornado_strike} 는 spawn 카드로
 * 시전하지만 만들어지는 것은 폭발 오브젝트 하나다.
 *
 * <p>{@link #Code} 는 Java 클래스가 필요한 마법이고, 나머지 다섯은 prefab 과 spawn height 만으로
 * {@code Abstract*Magic} 하나가 그대로 재현한다.
 */
public enum CastKind {
    Shot,
    Drop,
    Explosion,
    Summon,
    Spawn,
    Code;

    /**
     * 데이터에 적힌 이름을 계열로 바꾼다. 값이 비어 있거나 이 enum 에 없는 이름이면 비어 있는
     * Optional 을 돌려준다 — 부르는 쪽이 경고하고 그 마법을 건너뛴다.
     */
    public static Optional<CastKind> of(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        for (CastKind castKind : values()) {
            if (castKind.name().equalsIgnoreCase(name.trim())) {
                return Optional.of(castKind);
            }
        }
        return Optional.empty();
    }
}
