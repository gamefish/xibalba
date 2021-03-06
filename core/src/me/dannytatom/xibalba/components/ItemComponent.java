package me.dannytatom.xibalba.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import me.dannytatom.xibalba.utils.yaml.ItemData;

import java.util.HashMap;

public class ItemComponent implements Component {
  public final String type;
  public final String description;
  public final float weight;
  public final boolean twoHanded;
  public final String skill;
  public final HashMap<String, Integer> attributes;
  public final Array<String> verbs;
  public final Array<String> actions;
  public String location;
  public String name;
  public Quality quality;
  public boolean throwing = false;

  /**
   * Initialize item component from yaml data.
   *
   * @param data ItemData instance containing data from relevant yaml file
   */
  public ItemComponent(String name, String description, Quality quality, ItemData data) {
    this.name = name;
    this.description = description;
    this.quality = quality;
    this.type = data.type;
    this.weight = data.weight;
    this.location = data.location;
    this.twoHanded = data.twoHanded;
    this.skill = data.skill;

    this.attributes = data.attributes;
    this.actions = data.actions == null ? null : new Array<>(data.actions.toArray(new String[0]));
    this.verbs = data.verbs == null ? null : new Array<>(data.verbs.toArray(new String[0]));
  }

  public enum Quality {
    BROKEN(-6), POOR(-2), USABLE(0), GOOD(2), GREAT(6);

    private int modifier;

    Quality(int modifier) {
      this.modifier = modifier;
    }

    public int getModifier() {
      return modifier;
    }
  }
}
