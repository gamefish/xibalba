package me.dannytatom.xibalba.systems.actions;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import me.dannytatom.xibalba.WorldManager;
import me.dannytatom.xibalba.components.AttributesComponent;
import me.dannytatom.xibalba.components.MouseMovementComponent;
import me.dannytatom.xibalba.components.PositionComponent;
import me.dannytatom.xibalba.components.VisualComponent;
import me.dannytatom.xibalba.components.actions.MovementComponent;
import me.dannytatom.xibalba.systems.UsesEnergySystem;
import me.dannytatom.xibalba.utils.ComponentMappers;

public class MovementSystem extends UsesEnergySystem {
  public MovementSystem() {
    super(Family.all(MovementComponent.class).get());
  }

  /**
   * If the entities have a move action in queue, and can move where they're wanting to, move 'em.
   *
   * @param entity    The entity to process
   * @param deltaTime Time since last frame
   */
  public void processEntity(Entity entity, float deltaTime) {
    PositionComponent position = ComponentMappers.position.get(entity);
    VisualComponent visual = ComponentMappers.visual.get(entity);
    MovementComponent movement = ComponentMappers.movement.get(entity);
    AttributesComponent attributes = ComponentMappers.attributes.get(entity);

    if (movement.pos.x < position.pos.x) {
      visual.sprite.setFlip(true, false);
    } else {
      visual.sprite.setFlip(false, false);
    }

    // If we can move, move
    if (!WorldManager.mapHelpers.isBlocked(WorldManager.world.currentMapIndex, movement.pos)) {
      position.pos = movement.pos;
    } else {
      // If we can't, and the entity is the player, figure out what to do instead
      if (ComponentMappers.player.has(entity)) {
        Entity thing = WorldManager.entityHelpers.getEntityAt(movement.pos);

        if (WorldManager.entityHelpers.isItem(thing)) {
          WorldManager.inventoryHelpers.addItem(WorldManager.player, thing);

          position.pos = movement.pos;
        } else if (WorldManager.entityHelpers.isEnemy(thing)) {
          WorldManager.combatHelpers.preparePlayerForMelee(thing, "body");
        } else if (WorldManager.entityHelpers.isExit(thing)) {
          WorldManager.world.currentMapIndex += 1;

          entity.remove(MouseMovementComponent.class);
          position.map = WorldManager.world.currentMapIndex;
          WorldManager.state = WorldManager.State.PLAYING;
        } else if (WorldManager.entityHelpers.isEntrance(thing)) {
          WorldManager.world.currentMapIndex -= 1;

          entity.remove(MouseMovementComponent.class);
          position.map = WorldManager.world.currentMapIndex;
          WorldManager.state = WorldManager.State.PLAYING;
        }
      }
    }

    attributes.energy -= MovementComponent.COST;
    entity.remove(MovementComponent.class);
  }
}
