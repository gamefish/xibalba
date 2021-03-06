package me.dannytatom.xibalba.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import me.dannytatom.xibalba.utils.ComponentMappers;

public class MapDijkstra {
  public Array<Vector2> exploreGoals;
  public Dijkstra[] wanderLand = new Dijkstra[5];
  public Dijkstra[] wanderWater = new Dijkstra[3];
  public Dijkstra targetPlayerLand;
  public Dijkstra targetPlayerWater;
  public Dijkstra playerExplore;
  private Map map;

  public MapDijkstra(Map map) {
    this.map = map;
  }

  public void updateAll() {
    updateWanderLand();
    updateWanderWater();
    updateTargetPlayerLand();
    updateTargetPlayerWater();
    updatePlayerExplore();
  }

  public void updateWanderLand() {
    for (int i = 0; i < wanderLand.length; i++) {
      Array<Vector2> goal = new Array<>();
      goal.add(WorldManager.mapHelpers.getRandomOpenPositionOnLand(map.depth));

      MapCell.Type[] walkableTypes = new MapCell.Type[2];
      walkableTypes[0] = MapCell.Type.FLOOR;
      walkableTypes[1] = MapCell.Type.SHALLOW_WATER;

      wanderLand[i] = new Dijkstra(map, walkableTypes, goal);
    }
  }

  public Array<Vector2> findWanderLandPath(Vector2 start) {
    return wanderLand[MathUtils.random(0, wanderLand.length - 1)].findPath(start);
  }

  public void updateWanderWater() {
    if (map.hasWater) {
      for (int i = 0; i < wanderWater.length; i++) {
        Array<Vector2> goal = new Array<>();
        goal.add(WorldManager.mapHelpers.getRandomOpenPositionInWater(map.depth));

        MapCell.Type[] walkableTypes = new MapCell.Type[2];
        walkableTypes[1] = MapCell.Type.DEEP_WATER;

        wanderWater[i] = new Dijkstra(map, walkableTypes, goal);
      }
    }
  }

  public Array<Vector2> findWanderWaterPath(Vector2 start) {
    if (map.hasWater) {
      return wanderWater[MathUtils.random(0, wanderWater.length - 1)].findPath(start);
    } else {
      return null;
    }
  }

  public Array<Vector2> findExplorePath(Vector2 start) {
    return playerExplore.findPath(start);
  }

  public void updateTargetPlayerLand() {
    Vector2 position = ComponentMappers.position.get(WorldManager.player).pos;
    Array<Vector2> goals = new Array<>();
    goals.add(position);

    MapCell.Type[] walkableTypes = new MapCell.Type[2];
    walkableTypes[0] = MapCell.Type.FLOOR;
    walkableTypes[1] = MapCell.Type.SHALLOW_WATER;

    targetPlayerLand = new Dijkstra(map, walkableTypes, goals);
  }

  public Array<Vector2> findTargetPlayerLandPath(Vector2 start) {
    return targetPlayerLand.findPath(start);
  }

  public void updateTargetPlayerWater() {
    Vector2 position = ComponentMappers.position.get(WorldManager.player).pos;
    Array<Vector2> goals = new Array<>();
    goals.add(position);

    MapCell.Type[] walkableTypes = new MapCell.Type[2];
    walkableTypes[0] = MapCell.Type.DEEP_WATER;
    walkableTypes[1] = MapCell.Type.SHALLOW_WATER;

    targetPlayerWater = new Dijkstra(map, walkableTypes, goals);
  }

  public Array<Vector2> findTargetPlayerWaterPath(Vector2 start) {
    return targetPlayerWater.findPath(start);
  }

  public void updatePlayerExplore() {
    exploreGoals = new Array<>();

    for (int x = 0; x < map.width; x++) {
      for (int y = 0; y < map.height; y++) {
        if (map.getCellMap()[x][y].hidden && map.getCellMap()[x][y].isFloor()) {
          exploreGoals.add(new Vector2(x, y));
        }
      }
    }

    MapCell.Type[] walkableTypes = new MapCell.Type[2];
    walkableTypes[0] = MapCell.Type.FLOOR;
    walkableTypes[1] = MapCell.Type.SHALLOW_WATER;

    playerExplore = new Dijkstra(map, walkableTypes, exploreGoals);
  }
}
