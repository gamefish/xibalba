package me.dannytatom.xibalba.helpers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import me.dannytatom.xibalba.Main;
import me.dannytatom.xibalba.components.*;
import me.dannytatom.xibalba.components.statuses.CharmedComponent;
import me.dannytatom.xibalba.components.statuses.PoisonedComponent;
import me.dannytatom.xibalba.utils.ComponentMappers;
import me.dannytatom.xibalba.world.Map;
import me.dannytatom.xibalba.world.MapCell;
import me.dannytatom.xibalba.world.ShadowCaster;
import me.dannytatom.xibalba.world.WorldManager;

import java.util.Objects;

public class EntityHelpers {
  private final ShadowCaster caster;

  /**
   * EntityHelpers constructor, clearly.
   */
  public EntityHelpers() {
    caster = new ShadowCaster();
  }

  /**
   * Should this entity skip it's turn? </p> They should skip a turn if they're crippled and the
   * turn turnCounter isn't 0 or if they're stuck.
   *
   * @param entity The entity to check
   * @return If they should
   */
  public boolean shouldSkipTurn(Entity entity) {
    return (ComponentMappers.encumbered.has(entity)
      && ComponentMappers.encumbered.get(entity).turnCounter != 0)
      || (ComponentMappers.crippled.has(entity)
      && ComponentMappers.crippled.get(entity).turnCounter != 0)
      || (ComponentMappers.stuck.has(entity));
  }

  public boolean hasTrait(Entity entity, String trait) {
    return ComponentMappers.traits.get(entity).traits.contains(trait, false);
  }

  public boolean hasDefect(Entity entity, String defect) {
    return ComponentMappers.defects.get(entity).defects.contains(defect, false);
  }

  /**
   * Checks if an entity is hidden or not.
   *
   * @param entity Entity to check
   * @return Whether or not it's visible
   */
  public boolean isVisible(Entity entity) {
    PositionComponent entityPosition = ComponentMappers.position.get(entity);

    return entityPosition != null
      && !WorldManager.mapHelpers.getCell(entityPosition.pos.x, entityPosition.pos.y).hidden
      && !WorldManager.mapHelpers.getCell(entityPosition.pos.x, entityPosition.pos.y).forgotten;
  }

  /**
   * Find if an entity is near the player.
   *
   * @param entity The entity to check
   * @return Whether or not they are
   */
  public boolean isNearPlayer(Entity entity) {
    return isNear(entity, WorldManager.player);
  }

  public boolean isNear(Entity entity, Entity target) {
    PositionComponent entityPosition = ComponentMappers.position.get(entity);
    PositionComponent targetPosition = ComponentMappers.position.get(target);

    return (entityPosition.pos.x == targetPosition.pos.x - 1
      || entityPosition.pos.x == targetPosition.pos.x
      || entityPosition.pos.x == targetPosition.pos.x + 1)
      && (entityPosition.pos.y == targetPosition.pos.y - 1
      || entityPosition.pos.y == targetPosition.pos.y
      || entityPosition.pos.y == targetPosition.pos.y + 1);
  }

  public boolean isPlayerAlive() {
    return ComponentMappers.attributes.get(WorldManager.player).health > 0;
  }

  /**
   * Update an entity's senses.
   *
   * @param entity The entity
   */
  public void updateSenses(Entity entity) {
    AttributesComponent attributes = ComponentMappers.attributes.get(entity);
    PositionComponent position = ComponentMappers.position.get(entity);

    float[][] fovMap = WorldManager.mapHelpers.createFovMap();

    attributes.hearingMap = caster.calculateFov(
      fovMap, (int) position.pos.x, (int) position.pos.y, attributes.hearing
    );

    if (!Main.debug.fieldOfViewEnabled && ComponentMappers.player.has(entity)) {
      Map map = WorldManager.world.getCurrentMap();
      float[][] visionMap = new float[map.width][map.height];

      for (int x = 0; x < map.width; x++) {
        for (int y = 0; y < map.height; y++) {
          visionMap[x][y] = 1;
        }
      }

      attributes.visionMap = visionMap;
    } else {
      attributes.visionMap = caster.calculateFov(
        fovMap, (int) position.pos.x, (int) position.pos.y, attributes.vision
      );
    }
  }

  /**
   * Check if an entity can see another entity.
   *
   * @param looker Who looking
   * @param target Who they looking for
   * @return Yes/no
   */
  public boolean canSee(Entity looker, Entity target) {
    if (target == null) {
      return false;
    }

    AttributesComponent attributes = ComponentMappers.attributes.get(looker);
    PositionComponent targetPosition = ComponentMappers.position.get(target);

    int cellX = (int) targetPosition.pos.x;
    int cellY = (int) targetPosition.pos.y;

    return attributes.visionMap[cellX][cellY] > 0;
  }

  /**
   * Small convenience method to check if an entity can see the player.
   *
   * @param entity Who looking
   * @return Yes/no
   */
  private boolean canSeePlayer(Entity entity) {
    return canSee(entity, WorldManager.player);
  }

  /**
   * Check if an entity can hear another entity.
   *
   * @param listener Who listening
   * @param target   Who they listening for
   * @return Yes/no
   */
  public boolean canHear(Entity listener, Entity target) {
    AttributesComponent attributes = ComponentMappers.attributes.get(listener);
    PositionComponent targetPosition = ComponentMappers.position.get(target);

    return attributes.hearingMap[(int) targetPosition.pos.x][(int) targetPosition.pos.y] > 0;
  }

  /**
   * Small convenience method to check if an entity can hear the player.
   *
   * @param entity Who listening
   * @return Yes/no
   */
  private boolean canHearPlayer(Entity entity) {
    return canHear(entity, WorldManager.player);
  }

  /**
   * Can the entity see or hear the player.
   *
   * @param entity Who tryna find the player
   * @return Yes/no
   */
  public boolean canSensePlayer(Entity entity) {
    return canSeePlayer(entity) || canHearPlayer(entity);
  }

  public boolean canSense(Entity entity, Entity target) {
    return canSee(entity, target) || canHear(entity, target);
  }

  public boolean enemyInSight(Entity entity) {
    AttributesComponent attributes = ComponentMappers.attributes.get(entity);
    float[][] vision = attributes.visionMap;

    for (int x = 0; x < vision.length; x++) {
      for (int y = 0; y < vision[x].length; y++) {
        Entity enemy = WorldManager.mapHelpers.getEnemyAt(x, y);

        if (enemy != null && canSee(entity, enemy)) {
          return true;
        }
      }
    }

    return false;
  }

  public boolean anEnemyCanSensePlayer() {
    boolean canThey = false;

    Family family = Family.all(EnemyComponent.class).get();
    ImmutableArray<Entity> entities = WorldManager.engine.getEntitiesFor(family);

    for (Entity entity : entities) {
      if (canSensePlayer(entity)) {
        canThey = true;
        break;
      }
    }

    return canThey;
  }

  public boolean isAquatic(Entity entity) {
    BrainComponent brain = ComponentMappers.brain.get(entity);
    return brain != null && brain.dna.contains(BrainComponent.DNA.AQUATIC, false);
  }

  /**
   * Entity's toughness + armor defense.
   *
   * @param entity Who we're getting defense of
   * @return Their combined defense
   */
  int getCombinedDefense(Entity entity) {
    AttributesComponent attributes = ComponentMappers.attributes.get(entity);

    return MathUtils.random(1, attributes.toughness) + getArmorDefense(entity);
  }

  /**
   * Just armor defense.
   *
   * @param entity Who we're getting defense of
   * @return Their armor defense
   */
  public int getArmorDefense(Entity entity) {
    EquipmentComponent equipment = ComponentMappers.equipment.get(entity);

    int defense = 0;

    if (equipment != null) {
      for (Entity item : equipment.slots.values()) {
        if (item != null) {
          ItemComponent itemDetails = ComponentMappers.item.get(item);

          if (Objects.equals(itemDetails.type, "armor")) {
            defense += itemDetails.attributes.get("defense");
          }
        }
      }
    }

    return defense;
  }

  /**
   * Update an entity's position.
   *
   * @param entity The entity
   * @param cellX  x position
   * @param cellY  y position
   */
  public void updatePosition(Entity entity, float cellX, float cellY) {
    if (!ComponentMappers.position.has(entity)) {
      entity.add(new PositionComponent((int) cellX, (int) cellY));
    } else {
      ComponentMappers.position.get(entity).pos.set(cellX, cellY);
    }
  }

  /**
   * Update sprite position (called after turn is over, after all tweens etc), and update the sprite
   * color based on cell they're in.
   *
   * @param entity The entity
   * @param cellX  x position
   * @param cellY  y position
   */
  public void updateSprite(Entity entity, float cellX, float cellY) {
    VisualComponent visual = ComponentMappers.visual.get(entity);
    visual.sprite.setPosition(cellX * Main.SPRITE_WIDTH, cellY * Main.SPRITE_HEIGHT);

    MapCell cell = WorldManager.mapHelpers.getCell(cellX, cellY);

    if (cell.isWater()) {
      Color tinted = visual.color.cpy().lerp(cell.sprite.getColor(), .5f);

      if (visual.sprite.getColor() != tinted) {
        visual.sprite.setColor(tinted);
      }
    } else {
      if (visual.sprite.getColor() != visual.color) {
        visual.sprite.setColor(visual.color);
      }
    }
  }

  /**
   * Apply an effect to an entity.
   *
   * @param entity The entity
   * @param effect The effect. This is a string like "raiseHealth:5" where the part before colon is
   *               the method on EffectsHelpers, and the part after is the parameters (split by
   *               commas)
   */
  void applyEffect(Entity entity, String effect) {
    String[] split = effect.split(":");
    String name = split[0];
    String[] params = split[1].split(",");

    switch (name) {
      case "raiseHealth":
        raiseHealth(entity, Integer.parseInt(params[0]));
        break;
      case "raiseStrength":
        raiseStrength(entity, Integer.parseInt(params[0]));
        break;
      case "takeDamage":
        takeDamage(entity, Integer.parseInt(params[0]));
        break;
      case "poison":
        poison(entity, Integer.parseInt(params[0]),
          Integer.parseInt(params[1]), Integer.parseInt(params[2]));
        break;
      default:
    }
  }

  /**
   * Raise health.
   *
   * @param entity Entity whose health we're raising
   * @param amount How much
   */
  public void raiseHealth(Entity entity, int amount) {
    AttributesComponent attributes = ComponentMappers.attributes.get(entity);

    if (attributes.health + amount < attributes.maxHealth) {
      attributes.health += amount;
    } else {
      attributes.health = attributes.maxHealth;
    }

    if (ComponentMappers.player.has(entity)) {
      WorldManager.log.add("stats.healthRaised", "You", amount);

      ComponentMappers.player.get(entity).totalDamageHealed += amount;
    } else {
      WorldManager.log.add("stats.healthRaised", attributes.name, amount);
    }
  }

  /**
   * Raise strength.
   *
   * @param entity Entity whose strength we're raising
   * @param amount How much
   */
  private void raiseStrength(Entity entity, int amount) {
    AttributesComponent attributes = ComponentMappers.attributes.get(entity);

    if (attributes.strength < 12) {
      attributes.strength += amount;

      if (ComponentMappers.player.has(entity)) {
        WorldManager.log.add("stats.strengthRaised", "You");
      } else {
        WorldManager.log.add("stats.strengthRaised", attributes.name);
      }
    }
  }

  /**
   * Take some damage.
   *
   * @param entity Who gettin' hurt
   * @param amount How much
   */
  public void takeDamage(Entity entity, int amount) {
    AttributesComponent attributes = ComponentMappers.attributes.get(entity);

    attributes.health -= amount;

    if (ComponentMappers.player.has(entity)) {
      ComponentMappers.player.get(entity).totalDamageReceived += amount;
    }
  }

  private void poison(Entity entity, int chance, int damage, int turns) {
    if (ComponentMappers.poisoned.has(entity)) {
      return;
    }

    if (MathUtils.random() > chance / 100) {
      entity.add(new PoisonedComponent(damage, turns));

      if (ComponentMappers.player.has(entity)) {
        WorldManager.log.add("effects.poisoned.started", "You", "are");
      } else {
        AttributesComponent attributes = ComponentMappers.attributes.get(entity);

        WorldManager.log.add("effects.poisoned.started", attributes.name, "is");
      }
    }
  }

  public void charm(Entity starter, Entity target, int turns) {
    target.add(new CharmedComponent(turns));

    AttributesComponent targetAttributes = ComponentMappers.attributes.get(target);

    if (ComponentMappers.player.has(starter)) {
      WorldManager.log.add("effects.charmed.started", "You", targetAttributes.name);
    } else {
      WorldManager.log.add("effects.charmed.started", targetAttributes.name, "you");
    }
  }

  /**
   * Throw up a little bit.
   *
   * @param entity Who's vomiting
   * @param damage How much damage they take when they vomit
   */
  public void vomit(Entity entity, int damage) {
    takeDamage(entity, damage);

    PositionComponent position = ComponentMappers.position.get(entity);
    WorldManager.mapHelpers.makeFloorVomit(position.pos);
  }
}
