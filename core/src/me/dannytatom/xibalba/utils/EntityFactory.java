package me.dannytatom.xibalba.utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import me.dannytatom.xibalba.Main;
import me.dannytatom.xibalba.components.*;
import me.dannytatom.xibalba.components.items.AmmunitionComponent;
import me.dannytatom.xibalba.components.items.ArmorComponent;
import me.dannytatom.xibalba.components.items.WeaponComponent;
import me.dannytatom.xibalba.components.traps.SpiderWebComponent;
import me.dannytatom.xibalba.utils.yaml.EnemyData;
import me.dannytatom.xibalba.utils.yaml.ItemData;
import me.dannytatom.xibalba.world.Map;
import me.dannytatom.xibalba.world.WorldManager;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class EntityFactory {
  private final I18NBundle i18n;

  public EntityFactory() {
    i18n = Main.assets.get("i18n/xibalba", I18NBundle.class);
  }

  /**
   * Create an enemy.
   *
   * @param name     Name of enemy to create
   * @param position Vector2 of their position
   * @return The enemy
   */
  public Entity createEnemy(String name, Vector2 position) {
    Yaml yaml = new Yaml(new Constructor(EnemyData.class));
    EnemyData data = (EnemyData) yaml.load(
      Gdx.files.internal("data/enemies/" + name + ".yaml").reader()
    );

    Entity entity = new Entity();

    entity.add(new PositionComponent(position));
    entity.add(new EnemyComponent());
    entity.add(new SkillsComponent());
    entity.add(new BodyComponent(data.bodyParts, data.wearableBodyParts));

    entity.add(new VisualComponent(
        Main.asciiAtlas.createSprite(
          data.visual.get("character")), position, Main.parseColor(data.visual.get("color"))
      )
    );

    entity.add(new AttributesComponent(
      i18n.get("entities.enemies." + name + ".name"),
      i18n.get("entities.enemies." + name + ".description"),
      data.type,
      data.attributes.get("speed"),
      data.attributes.get("vision"),
      data.attributes.get("hearing"),
      data.attributes.get("toughness"),
      data.attributes.get("strength"),
      data.attributes.get("agility")
    ));

    BrainComponent brain = new BrainComponent(entity);
    entity.add(brain);

    brain.aggression = data.brain.aggression;
    brain.fearThreshold = data.brain.fearThreshold;

    brain.dna = new Array<>();
    data.brain.dna.forEach((str) -> {
      brain.dna.add(BrainComponent.DNA.valueOf(str));
    });

    if (data.effects != null) {
      entity.add(new EffectsComponent(data));
    }

    return entity;
  }

  /**
   * Create an item.
   *
   * @param name     Mame of item to create
   * @param position Vector2 of their position
   * @return The item
   */
  public Entity createItem(String name, Vector2 position) {
    Yaml yaml = new Yaml(new Constructor(ItemData.class));
    ItemData data = (ItemData) yaml.load(
      Gdx.files.internal("data/items/" + name + ".yaml").reader()
    );

    Entity entity = new Entity();

    entity.add(new PositionComponent(position));

    ItemComponent.Quality[] qualities = ItemComponent.Quality.values();

    entity.add(
      new ItemComponent(
        i18n.get("entities.items." + name + ".name"),
        i18n.get("entities.items." + name + ".description"),
        qualities[MathUtils.random(0, qualities.length - 1)],
        data
      )
    );

    switch (data.type) {
      case "armor":
        entity.add(new ArmorComponent(data));
        break;
      case "weapon":
        entity.add(new WeaponComponent(data));
        break;
      case "light":
        entity.add(new LightComponent(data));
        break;
      default:
    }

    if (data.ammunitionType != null) {
      entity.add(new AmmunitionComponent(data));
    }

    if (data.effects != null) {
      entity.add(new EffectsComponent(data));
    }

    if (ComponentMappers.weapon.has(entity)) {
      WeaponComponent weapon = ComponentMappers.weapon.get(entity);
      WeaponComponent.Material[] materials = WeaponComponent.Material.values();

      weapon.material = materials[MathUtils.random(0, materials.length - 1)];
    }

    entity.add(new VisualComponent(
      Main.asciiAtlas.createSprite(data.visual.get("character")),
      position, Main.parseColor(data.visual.get("color"))
    ));

    return entity;
  }

  /**
   * Create a corpse.
   *
   * @param enemy    The enemy who's corpse we're creating
   * @param position Where to place it
   * @return The corpse
   */
  public Entity createCorpse(Entity enemy, Vector2 position) {
    Entity entity = createItem("corpse", position);
    String name = ComponentMappers.attributes.get(enemy).name;

    ComponentMappers.item.get(entity).name = name + " corpse";

    BodyComponent body = ComponentMappers.body.get(enemy);
    AttributesComponent.Type type = ComponentMappers.attributes.get(enemy).type;
    entity.add(new CorpseComponent(name, type, body.parts, body.wearable));

    return entity;
  }

  /**
   * Create skin.
   *
   * @param corpse   The corpse we're skinning
   * @param position Where to place the skin
   * @return The skin
   */
  public Entity createSkin(Entity corpse, Vector2 position) {
    Entity entity = createItem("skin", position);

    ComponentMappers.item.get(entity).name
      = ComponentMappers.corpse.get(corpse).entity + " skin";

    return entity;
  }

  /**
   * Create limb.
   *
   * @param corpse   The corpse we're dismembering
   * @param part     What part of the body we're dismembering
   * @param position Where to place the limb
   * @return The skin
   */
  public Entity createLimb(Entity corpse, String part, Vector2 position) {
    Entity entity = createItem("limb", position);

    ItemComponent item = ComponentMappers.item.get(entity);

    item.name = ComponentMappers.corpse.get(corpse).entity
      + " " + part.replace("left ", "").replace("right ", "");

    entity.add(new LimbComponent(ComponentMappers.corpse.get(corpse).type));

    CorpseComponent body = ComponentMappers.corpse.get(corpse);

    if (body.wearable != null && body.wearable.get(part) != null) {
      item.location = part;
      item.actions.add("wear");

      EffectsComponent effects = new EffectsComponent();
      effects.effects.put("passive", body.wearable.get(part));

      entity.add(effects);
    }

    return entity;
  }

  /**
   * Create trap.
   *
   * @param name     Name of the trap we're creating
   * @param position Where to place it
   * @return The trap
   */
  public Entity createTrap(String name, Vector2 position) {
    Entity entity = new Entity();

    entity.add(new TrapComponent());
    entity.add(new PositionComponent(position));

    switch (name) {
      case "spiderWeb":
        entity.add(new SpiderWebComponent());

        entity.add(new VisualComponent(
          Main.asciiAtlas.createSprite("0302"), position, Color.WHITE,
          WorldManager.entityHelpers.hasTrait(WorldManager.player, "Perceptive") ? .5f : .1f
        ));

        break;
      default:
    }

    return entity;
  }

  /**
   * Create entrance entity.
   *
   * @param mapIndex Map to place it on
   * @return The entrance entity
   */
  public Entity createEntrance(int mapIndex) {
    Map map = WorldManager.world.getMap(mapIndex);

    int cellX;
    int cellY;

    do {
      cellX = MathUtils.random(0, map.width - 1);
      cellY = MathUtils.random(0, map.height - 1);
    }
    while (WorldManager.mapHelpers.isBlocked(mapIndex, new Vector2(cellX, cellY))
      && WorldManager.mapHelpers.getWallNeighbours(mapIndex, cellX, cellY) >= 4);

    Vector2 position = new Vector2(cellX, cellY);
    Entity entity = new Entity();
    entity.add(new EntranceComponent());
    entity.add(new PositionComponent(position));

    entity.add(new VisualComponent(
      Main.asciiAtlas.createSprite("1203"), position
    ));

    return entity;
  }

  /**
   * Create exit entity.
   *
   * @param mapIndex Map to place it on
   * @return The exit entity
   */
  public Entity createExit(int mapIndex) {
    Map map = WorldManager.world.getMap(mapIndex);

    int cellX;
    int cellY;

    do {
      cellX = MathUtils.random(0, map.width - 1);
      cellY = MathUtils.random(0, map.height - 1);
    }
    while (WorldManager.mapHelpers.isBlocked(mapIndex, new Vector2(cellX, cellY))
      && WorldManager.mapHelpers.getWallNeighbours(mapIndex, cellX, cellY) >= 4);

    Vector2 position = new Vector2(cellX, cellY);
    Entity entity = new Entity();
    entity.add(new ExitComponent());
    entity.add(new PositionComponent(position));

    entity.add(new VisualComponent(
      Main.asciiAtlas.createSprite("1403"), position
    ));

    return entity;
  }

  /**
   * Create rain drop.
   *
   * @return The rain drop
   */
  public Entity createRainDrop() {
    Entity entity = new Entity();
    Vector2 position = new Vector2(0, 0);

    entity.add(new DecorationComponent(false));
    entity.add(new RainDropComponent());
    entity.add(new PositionComponent(position));
    entity.add(new VisualComponent(
      Main.asciiAtlas.createSprite("1502"), position, Colors.get("CYAN")
    ));

    return entity;
  }
}
