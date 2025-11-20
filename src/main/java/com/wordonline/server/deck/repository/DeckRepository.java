package com.wordonline.server.deck.repository;

import com.wordonline.server.deck.dto.CardsDto;
import com.wordonline.server.game.domain.magic.CardType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DeckRepository {

    private static final String GET_SELECTED_DECK = """
            SELECT
              cards.id AS id,
              cards.name AS name,
              cards.card_type AS card_type,
              deck_cards.count AS count
            FROM
              users
            JOIN decks ON decks.id = users.selected_deck_id
            JOIN deck_cards ON deck_cards.deck_id = decks.id
            JOIN cards ON cards.id = deck_cards.card_id
            WHERE users.id = :userId;
            """;

    private static final String GET_DECK = """
            SELECT
              cards.id AS id,
              cards.name AS name,
              cards.card_type AS card_type,
              deck_cards.count AS count
            FROM
            decks
            JOIN deck_cards ON deck_cards.deck_id = decks.id
            JOIN cards ON cards.id = deck_cards.card_id
            WHERE decks.id = :deckId;
            """;

    private final JdbcClient jdbcClient;

    public List<CardsDto> getSelectedDeck(long userId) {
        return jdbcClient.sql(GET_SELECTED_DECK)
                .param("userId", userId)
                .query((rs, num) ->
                    new CardsDto(rs.getLong("id"),
                            CardType.valueOf(rs.getString("name")),
                            rs.getInt("count"))
                ).list();
    }

    public List<CardsDto> getDeck(long deckId) {
        return jdbcClient.sql(GET_DECK)
                .param("deckId", deckId)
                .query((rs, num) ->
                        new CardsDto(rs.getLong("id"),
                                CardType.valueOf(rs.getString("name")),
                                rs.getInt("count"))
                ).list();
    }
}
