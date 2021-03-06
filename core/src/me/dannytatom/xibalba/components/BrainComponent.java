package me.dannytatom.xibalba.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import me.dannytatom.xibalba.brain.Brain;

public class BrainComponent implements Component {
  public final StateMachine<Entity, Brain> stateMachine;

  public Array<DNA> dna;
  public Array<String> hates;
  public Array<String> avoids;
  public Array<String> afraidOf;

  public float aggression;
  public float fear;
  public float fearThreshold;

  public Array<Vector2> path;
  public Entity target;

  public BrainComponent(Entity entity) {
    stateMachine = new DefaultStateMachine<>(entity, Brain.SLEEP);

    dna = new Array<>();
    hates = new Array<>();
    avoids = new Array<>();
    afraidOf = new Array<>();

    aggression = 0.5f;
    fear = 0f;
    fearThreshold = 0.5f;
  }

  public enum DNA {
    TERRESTRIAL,
    AQUATIC,
    SWIMMER,
    FLYER,

    STEALTHY,
    PACK,
    SOLO
  }
}
