package com.wordonline.server.deck.repository;

import com.wordonline.server.deck.domain.DeckInfo;
import com.wordonline.server.deck.dto.CardDto;
import com.wordonline.server.deck.dto.CardsDto;
import com.wordonline.server.deck.dto.DeckCardDto;
import com.wordonline.server.game.domain.magic.CardType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DeckRepository {

    private static final String GET_DECKS_BY_ID = """
        SELECT
         dd.deck_id,
         dd.deck_name,
         c.id AS card_id,
         c.name AS card_name,
         c.card_type AS card_type,
         dd.count AS count
         FROM cards c
         JOIN (
             SELECT
                 d.id AS deck_id,
                 d.name AS deck_name,
                 dc.card_id,
                 dc.count,
                 d.user_id
             FROM decks d
             JOIN deck_cards dc ON d.id = dc.deck_id
         ) AS dd ON c.id = dd.card_id
         WHERE dd.user_id = :id
         ORDER BY dd.deck_id;
         """;

    private static final String GET_DECK_BY_ID = """
            SELECT
              d.id AS deck_id,
              d.name AS deck_name,
              c.id AS card_id,
              c.name AS card_name,
              c.card_type AS card_type,
              dc.count AS count
            FROM decks d
            JOIN deck_cards dc ON d.id = dc.deck_id
            JOIN cards c ON c.id = dc.card_id
            WHERE d.id = :id
            """;

    private static final String GET_CARD_BY_ID = """
           SELECT id, name, card_type
           FROM cards
           WHERE id = :id;
           """;

    private static final String GET_CARDS = """
           SELECT id, name, card_type
           FROM cards
           """;

    private static final String SET_DECK_NAME = """
            UPDATE decks
            SET name = :name
            WHERE id = :deckId;
            """;

    private static final String SAVE_DECK = """
            INSERT INTO decks(name, user_id)
            VALUES
            (:name, :userId)
            RETURNING id;
            """;

    private static final String SAVE_CARDS_TO_USER = """
            INSERT INTO user_cards(user_id, card_id, count)
            VALUES
            (:userId, :cardId, :count)
            """;

    private static final String UPDATE_CARDS_TO_USER = """
            UPDATE user_cards
            SET count = :count
            WHERE user_id = :userId AND card_id = :cardId;
            """;

    private static final String DELETE_CARDS_TO_USER = """
            DELETE FROM user_cards
            WHERE user_id = :userId AND card_id = :cardId;
            """;

    private static final String SAVE_CARDS_TO_DECK = """
            INSERT INTO deck_cards(deck_id, card_id, count)
            VALUES
            (:deckId, :cardId, :count)
            """;

    private static final String UPDATE_CARDS_TO_DECK = """
            UPDATE deck_cards
            SET count = :count
            WHERE deck_id = :deckId AND card_id = :cardId;
            """;

    private static final String DELETE_CARDS_TO_DECK = """
            DELETE FROM deck_cards
            WHERE deck_id = :deckId;
            """;

    private static final String GET_CARDS_BY_USER_ID = """
            SELECT cards.id AS id, cards.name AS name, card_type, count
            FROM cards JOIN
                user_cards ON cards.id = user_cards.card_id
            WHERE user_cards.user_id = :id;
            """;

    private static final String GET_DECK_INFO = """
            SELECT id, user_id, name
            FROM decks
            WHERE id = :deckId
            """;

    private static final String SELECT_DECK = """
            UPDATE users
            SET selected_deck_id = :deckId
            WHERE id = :userId
            """;

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

    private static final String GET_SELECTED_DECK_ID = """
            SELECT selected_deck_id
            FROM users
            WHERE id = :userId;
            """;

    private final JdbcClient jdbcClient;

    public void setDeckName(long deckId, String deckName) {
        jdbcClient.sql(SET_DECK_NAME)
                .param("deckId", deckId)
                .param("name", deckName)
                .update();
    }

    public Optional<Long> getSelectedDeckId(long userId) {
        return jdbcClient.sql(GET_SELECTED_DECK_ID)
                .param("userId", userId)
                .query(Long.class)
                .optional();
    }

    public List<CardsDto> getSelectedDeck(long userId) {
        return jdbcClient.sql(GET_SELECTED_DECK)
                .param("userId", userId)
                .query((rs, num) ->
                    new CardsDto(rs.getLong("id"),
                            CardType.valueOf(rs.getString("name")),
                            rs.getInt("count"))
                ).list();
    }

    public List<CardDto> getCards() {
        return jdbcClient.sql(GET_CARDS)
            .query((rs, num) ->
                new CardDto(
                    rs.getLong("id"),
                    CardType.valueOf(rs.getString("name"))
                ))
            .list();
    }

    public long saveDeck(long userId, String name){

        return jdbcClient.sql(SAVE_DECK)
                .param("userId", userId)
                .param("name", name)
                .query(Long.class)
                .single();
    }

    public void saveCardToUser(long userId, long cardId, int count) {
        boolean successful = jdbcClient.sql(SAVE_CARDS_TO_USER)
                .param("userId", userId)
                .param("cardId", cardId)
                .param("count", count)
                .update() > 0;

        if (!successful)
            throw new IllegalArgumentException();
    }

    public void updateCardToUser(long userId, long cardId, int count) {
        jdbcClient.sql(UPDATE_CARDS_TO_USER)
                .param("userId", userId)
                .param("cardId", cardId)
                .param("count", count)
                .update();
    }

    public void deleteCardInUser(long userId, long cardId) {
        jdbcClient.sql(DELETE_CARDS_TO_USER)
                .param("userId", userId)
                .param("cardId", cardId)
                .update();
    }

    public void saveCardToDeck(long deckId, long cardId, int count) {
        jdbcClient.sql(SAVE_CARDS_TO_DECK)
                .param("deckId", deckId)
                .param("cardId", cardId)
                .param("count", count)
                .update();
    }


    public void deleteCardInDeck(long deckId) {
        jdbcClient.sql(DELETE_CARDS_TO_DECK)
                .param("deckId", deckId)
                .update();
    }

    public Optional<DeckInfo> getDeckInfo(long deckId) {
        return jdbcClient.sql(GET_DECK_INFO)
            .param("deckId", deckId)
            .query((rs, num) ->
                new DeckInfo(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("name")
                ))
            .optional();
    }

    public void setSelectDeck(long userId, long deckId) {
        jdbcClient.sql(SELECT_DECK)
                .param("userId", userId)
                .param("deckId", deckId)
                .update();
    }

    public List<CardsDto> getCardPool(long userId) {
        List<CardsDto> cardsDtos = jdbcClient.sql(GET_CARDS_BY_USER_ID)
                .param("id", userId)
                .query((rs, num) ->
                        new CardsDto(
                                rs.getLong("id"),
                                CardType.valueOf(rs.getString("name")),
                                rs.getInt("count")
                        ))
                .list();

        System.out.println(cardsDtos);
        return cardsDtos;
    }

    public List<DeckCardDto> getDecks(long userId) {
        return jdbcClient.sql(GET_DECKS_BY_ID)
                .param("id", userId)
                .query((rs, num) ->
                        new DeckCardDto(
                                rs.getLong("deck_id"),
                                rs.getLong("card_id"),
                                rs.getInt("count"),
                                rs.getString("deck_name"),
                                CardType.valueOf(rs.getString("card_name")),
                                CardType.Type.valueOf(rs.getString("card_type"))
                        ))
                .list();
    }

    public List<DeckCardDto> getDeck(long deckId) {
        return jdbcClient.sql(GET_DECK_BY_ID)
                .param("id", deckId)
                .query((rs, num) ->
                        new DeckCardDto(
                                rs.getLong("deck_id"),
                                rs.getLong("card_id"),
                                rs.getInt("count"),
                                rs.getString("deck_name"),
                                CardType.valueOf(rs.getString("card_name")),
                                CardType.Type.valueOf(rs.getString("card_type"))
                        ))
                .list();
    }
}
