package me.dannytatom.xibalba.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import me.dannytatom.xibalba.components.AttributesComponent;
import me.dannytatom.xibalba.utils.ComponentMappers;

import java.util.Comparator;

public abstract class UsesEnergySystem extends SortedIteratingSystem {
  protected UsesEnergySystem(Family family) {
    super(family, new EnergyComparator());
  }

  private static class EnergyComparator implements Comparator<Entity> {
    @Override
    public int compare(Entity e1, Entity e2) {
      AttributesComponent a1 = ComponentMappers.attributes.get(e1);
      AttributesComponent a2 = ComponentMappers.attributes.get(e2);

      if (a2.energy > a1.energy) {
        return 1;
      } else if (a1.energy > a2.energy) {
        return -1;
      } else {
        return 0;
      }
    }
  }
}
