package me.dannytatom.xibalba.systems.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
import me.dannytatom.xibalba.components.PositionComponent;
import me.dannytatom.xibalba.components.actions.MovementComponent;
import me.dannytatom.xibalba.components.ai.BrainComponent;
import me.dannytatom.xibalba.components.ai.WanderComponent;
import me.dannytatom.xibalba.systems.UsesEnergySystem;
import me.dannytatom.xibalba.utils.ComponentMappers;
import me.dannytatom.xibalba.world.WorldManager;
import org.xguzm.pathfinding.grid.GridCell;
import org.xguzm.pathfinding.grid.NavigationGrid;
import org.xguzm.pathfinding.grid.finders.AStarGridFinder;

import java.util.ArrayList;
import java.util.List;

public class WanderSystem extends UsesEnergySystem {
  public WanderSystem() {
    super(Family.all(WanderComponent.class, PositionComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    BrainComponent brain = ComponentMappers.brain.get(entity);

    // Create path to a random open cell on the world if one
    // doesn't already exist.
    if (brain.path == null || brain.path.isEmpty()) {
      PositionComponent position = ComponentMappers.position.get(entity);

      NavigationGrid<GridCell> grid =
          new NavigationGrid<>(WorldManager.mapHelpers.createPathfindingMap(), false);
      AStarGridFinder<GridCell> finder = new AStarGridFinder<>(GridCell.class);

      do {
        Vector2 randomPosition = WorldManager.mapHelpers.getRandomOpenPosition();
        brain.path = finder.findPath((int) position.pos.x, (int) position.pos.y,
            (int) randomPosition.x, (int) randomPosition.y, grid);
      }
      while (brain.path == null);
    }

    // Start walking.
    // If the path becomes blocked, reset the path.
    //
    // TODO: Instead of checking next cell, check any cell in the path that's in their vision
    GridCell cell = brain.path.get(0);

    if (cell.isWalkable()) {
      entity.add(new MovementComponent(new Vector2(cell.getX(), cell.getY())));

      List<GridCell> newPath = new ArrayList<>(brain.path);
      newPath.remove(cell);

      brain.path = newPath;
    } else {
      brain.path = null;
    }
  }
}
