package com.wordonline.server.game.domain.object.prefab.implement.explode;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.magic.RazorGale;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("razor_gale_prefab")
public class RazorGalePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public RazorGalePrefabInitializer(Parameters parameters) {
        super(PrefabType.RazorGale);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        float radius = (float) parameters.getValue("razor_gale", "radius");
        gameObject.addCollider(new CircleCollider(gameObject, radius, true));
        gameObject.setElement(ElementType.WIND);
        gameObject.addComponent(new RazorGale(
                gameObject,
                (int) parameters.getValue("razor_gale", "damage"),
                radius,
                (float) parameters.getValue("razor_gale", "attack_interval")));
        gameObject.addComponent(new TimedSelfDestroyer(gameObject, (float) parameters.getValue("razor_gale", "duration")));
    }
}
