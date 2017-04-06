package me.dannytatom.xibalba.utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import me.dannytatom.xibalba.Main;
import me.dannytatom.xibalba.components.AbilitiesComponent;
import me.dannytatom.xibalba.components.AttributesComponent;
import me.dannytatom.xibalba.components.BodyComponent;
import me.dannytatom.xibalba.components.EquipmentComponent;
import me.dannytatom.xibalba.components.GodComponent;
import me.dannytatom.xibalba.components.InventoryComponent;
import me.dannytatom.xibalba.components.PlayerComponent;
import me.dannytatom.xibalba.components.PositionComponent;
import me.dannytatom.xibalba.components.SkillsComponent;
import me.dannytatom.xibalba.components.VisualComponent;
import me.dannytatom.xibalba.components.defects.MyopiaComponent;
import me.dannytatom.xibalba.components.defects.OneArmComponent;
import me.dannytatom.xibalba.components.traits.CarnivoreComponent;
import me.dannytatom.xibalba.components.traits.PerceptiveComponent;
import me.dannytatom.xibalba.components.traits.QuickComponent;
import me.dannytatom.xibalba.components.traits.ScoutComponent;
import me.dannytatom.xibalba.world.WorldManager;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.util.Calendar;
import java.util.TreeMap;

public class PlayerSetup {
  private static final TreeMap<Integer, String> rnMap = new TreeMap<>();

  public final AttributesComponent attributes;
  public final SkillsComponent skills;
  public final Array<String> traits;
  public final Array<String> defects;
  public YamlToGod god;

  public String color;
  public String name;

  /**
   * Setup player defaults.
   * This is used during character creation.
   */
  public PlayerSetup() {
    rnMap.put(1000, "M");
    rnMap.put(900, "CM");
    rnMap.put(500, "D");
    rnMap.put(400, "CD");
    rnMap.put(100, "C");
    rnMap.put(90, "XC");
    rnMap.put(50, "L");
    rnMap.put(40, "XL");
    rnMap.put(10, "X");
    rnMap.put(9, "IX");
    rnMap.put(5, "V");
    rnMap.put(4, "IV");
    rnMap.put(1, "I");

    color = "FFFFFF";
    generateName();

    attributes = new AttributesComponent(name, "It's you", 100, 10, 5, 4, 4, 4);
    skills = new SkillsComponent();
    traits = new Array<>();
    defects = new Array<>();
  }

  private static String intToRoman(int number) {
    int floored = rnMap.floorKey(number);

    if (number == floored) {
      return rnMap.get(number);
    }

    return rnMap.get(floored) + intToRoman(number - floored);
  }

  /**
   * Create the player.
   * </p>
   * This is called when character creation is done and
   * we want to create the actual entity.
   *
   * @return The player entity
   */
  public Entity create() {
    attributes.maxHealth = attributes.toughness * 10;
    attributes.health = attributes.maxHealth;

    attributes.maxOxygen = attributes.toughness * 4;
    attributes.oxygen = attributes.maxOxygen;

    Entity player = new Entity();

    player.add(skills);
    player.add(attributes);

    Vector2 position = WorldManager.world.getCurrentMap().entrance;
    player.add(new PositionComponent(position));
    player.add(
        new VisualComponent(Main.asciiAtlas.createSprite("0004"), position, Main.parseColor(color))
    );

    player.add(new PlayerComponent());
    player.add(new InventoryComponent());
    player.add(new EquipmentComponent());

    TreeMap<String, Integer> bodyParts = new TreeMap<>();
    bodyParts.put("head", 10);
    bodyParts.put("body", 8);
    bodyParts.put("left arm", 10);
    bodyParts.put("right arm", 10);
    bodyParts.put("left leg", 10);
    bodyParts.put("right leg", 10);
    player.add(new BodyComponent(bodyParts));

    if (defects.contains(OneArmComponent.name, false)) {
      player.add(new OneArmComponent());

      BodyComponent body = ComponentMappers.body.get(player);
      body.parts.remove("left arm");

      AttributesComponent attributes = ComponentMappers.attributes.get(player);
      attributes.maxHealth = MathUtils.ceil(attributes.maxHealth - (attributes.maxHealth * .20f));
      attributes.health = attributes.maxHealth;
    }

    if (defects.contains(MyopiaComponent.name, false)) {
      player.add(new MyopiaComponent());

      AttributesComponent attributes = ComponentMappers.attributes.get(player);
      attributes.maxVision = 5;
      attributes.vision = 5;
    }

    if (traits.contains(ScoutComponent.name, false)) {
      player.add(new ScoutComponent());

      AttributesComponent attributes = ComponentMappers.attributes.get(player);
      attributes.maxVision = 20;
      attributes.vision = 20;
    }

    if (traits.contains(PerceptiveComponent.name, false)) {
      player.add(new PerceptiveComponent());

      AttributesComponent attributes = ComponentMappers.attributes.get(player);
      attributes.hearing = attributes.vision * 2;
    }

    if (traits.contains(CarnivoreComponent.name, false)) {
      player.add(new CarnivoreComponent());
    }

    if (traits.contains(QuickComponent.name, false)) {
      player.add(new QuickComponent());

      AttributesComponent attributes = ComponentMappers.attributes.get(player);
      attributes.speed = attributes.speed + MathUtils.round(attributes.speed * .5f);
    }

    WorldManager.god = new Entity();
    WorldManager.god.add(new GodComponent(god.name, god.description));

    AbilitiesComponent abilitiesComponent = new AbilitiesComponent();

    Yaml yaml = new Yaml(new Constructor(YamlToAbility.class));
    god.abilities.forEach((String ability) -> {
      YamlToAbility details = (YamlToAbility) yaml.load(
          Gdx.files.internal("data/abilities/" + ability + ".yaml").read()
      );

      details.counter = details.recharge;
      abilitiesComponent.abilities.add(details);
    });

    player.add(abilitiesComponent);

    return player;
  }

  private void generateName() {
    String preceding;
    String[] names;

    if (MathUtils.randomBoolean()) {
      preceding = intToRoman(Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
      names = Gdx.files.internal("data/names/male").readString().split("\\r?\\n");
    } else {
      preceding = "IX";
      names = Gdx.files.internal("data/names/female").readString().split("\\r?\\n");
    }

    name = preceding + " " + names[MathUtils.random(0, names.length - 1)];
  }
}
