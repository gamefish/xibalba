package me.dannytatom.xibalba.utils;

import com.badlogic.ashley.core.Entity;
import me.dannytatom.xibalba.components.EquipmentComponent;
import me.dannytatom.xibalba.components.ItemComponent;

public class EquipmentHelpers {
  public EquipmentHelpers() {

  }

  /**
   * Check if an item is equipped.
   *
   * @param entity The entity whose equipment we're checking
   * @param item   The item we want to check
   *
   * @return Whether or not it's equipped
   */
  public boolean isEquipped(Entity entity, Entity item) {
    EquipmentComponent equipment = ComponentMappers.equipment.get(entity);

    return equipment.slots.containsValue(item);
  }

  /**
   * Get the slot location of an item.
   *
   * @param item The item we want to check
   *
   * @return Location of item
   */
  public String getLocation(Entity item) {
    return ComponentMappers.item.get(item).location;
  }

  /**
   * Equip item to right hand (primary weapon slot).
   *
   * @param entity The entity we want to hold the item
   * @param item   The item itself
   */
  public void holdItem(Entity entity, Entity item) {
    ItemComponent itemDetails = ComponentMappers.item.get(item);
    EquipmentComponent equipment = ComponentMappers.equipment.get(entity);

    if (itemDetails.actions.get("canHold")) {
      equipment.slots.put("right hand", item);
    }
  }

  /**
   * Equip an item to that item's location.
   *
   * @param entity The entity we want to wear the item
   * @param item   The item itself
   */
  public void wearItem(Entity entity, Entity item) {
    EquipmentComponent equipment = ComponentMappers.equipment.get(entity);
    ItemComponent itemDetails = ComponentMappers.item.get(item);

    equipment.slots.put(itemDetails.location, item);
  }

  /**
   * Remove an item from either their right hand or wherever it's slotted.
   *
   * @param entity The entity we want to remove the item from
   * @param item   The item itself
   */
  public void removeItem(Entity entity, Entity item) {
    EquipmentComponent equipment = ComponentMappers.equipment.get(entity);
    ItemComponent itemDetails = ComponentMappers.item.get(item);

    if (equipment.slots.get("right hand") == item) {
      equipment.slots.put("right hand", null);
    } else {
      equipment.slots.put(itemDetails.location, null);
    }
  }

  public Entity getPrimaryWeapon(Entity entity) {
    return ComponentMappers.equipment.get(entity).slots.get("right hand");
  }
}
