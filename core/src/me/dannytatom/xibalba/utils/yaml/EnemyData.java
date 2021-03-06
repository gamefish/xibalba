package me.dannytatom.xibalba.utils.yaml;

import me.dannytatom.xibalba.components.AttributesComponent;

import java.util.HashMap;
import java.util.TreeMap;

public class EnemyData {
  public AttributesComponent.Type type;
  public HashMap<String, String> visual;
  public HashMap<String, Integer> attributes;
  public BrainData brain;
  public TreeMap<String, Integer> bodyParts;
  public TreeMap<String, String> wearableBodyParts;
  public HashMap<String, String> effects;
}
