package com.wordonline.server.game.dto.input;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.Vector3;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InputRequestDto {
    private String type;
    private CardType card;
    private long magicId;
    private int id;
    private Vector3 position;

    public MagicUseRequestDto toMagicUse() {
        if (!type.equals("useMagic")) {
            throw new IllegalArgumentException("InputRequestDto type is not 'useMagic'");
        }

        return new MagicUseRequestDto("useMagic", magicId, id, position);
    }

    public CardSelectRequestDto toCardSelect() {
        if (!type.equals("selectCard") && !type.equals("toggleCard")) {
            throw new IllegalArgumentException("InputRequestDto type is not 'selectCard'");
        }

        return new CardSelectRequestDto(type, resolveCard(), id);
    }

    public CardUnselectRequestDto toCardUnselect() {
        if (!type.equals("unselectCard")) {
            throw new IllegalArgumentException("InputRequestDto type is not 'unselectCard'");
        }

        return new CardUnselectRequestDto(type, resolveCard(), id);
    }

    public CardCancelRequestDto toCardCancel() {
        if (!type.equals("cancelCard")) {
            throw new IllegalArgumentException("InputRequestDto type is not 'cancelCard'");
        }

        return new CardCancelRequestDto(type, id);
    }

    private CardType resolveCard() {
        if (card == null) {
            throw new IllegalArgumentException("InputRequestDto card is missing");
        }
        return card;
    }
}
