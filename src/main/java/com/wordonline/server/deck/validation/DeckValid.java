package com.wordonline.server.deck.validation;

import jakarta.validation.Constraint;
import org.springframework.messaging.handler.annotation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DeckValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DeckValid {
    String message() default "Deck must contain at least " +
            DeckValidator.LEAST_NUM_OF_MAGIC_CARD +
            " magic cards and " +
            DeckValidator.LEAST_NUM_OF_TYPE_CARD +
            " attribute cards";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}