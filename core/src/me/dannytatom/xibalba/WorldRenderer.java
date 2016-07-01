package me.dannytatom.xibalba;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import me.dannytatom.xibalba.components.AttributesComponent;
import me.dannytatom.xibalba.components.DecorationComponent;
import me.dannytatom.xibalba.components.EnemyComponent;
import me.dannytatom.xibalba.components.EntranceComponent;
import me.dannytatom.xibalba.components.ExitComponent;
import me.dannytatom.xibalba.components.ItemComponent;
import me.dannytatom.xibalba.components.PlayerComponent;
import me.dannytatom.xibalba.components.PositionComponent;
import me.dannytatom.xibalba.components.VisualComponent;
import me.dannytatom.xibalba.map.Cell;
import me.dannytatom.xibalba.map.Map;
import me.dannytatom.xibalba.map.ShadowCaster;
import me.dannytatom.xibalba.utils.ComponentMappers;
import org.apache.commons.lang3.ArrayUtils;
import org.xguzm.pathfinding.grid.GridCell;

public class WorldRenderer {
  private final Main main;
  private final SpriteBatch batch;
  private final ShadowCaster caster;
  private final Viewport viewport;
  private final OrthographicCamera worldCamera;

  /**
   * Renders the game world.
   *
   * @param main  Instance of Main class
   * @param batch The sprite batch to use (set in PlayScreen)
   */
  public WorldRenderer(Main main, OrthographicCamera worldCamera, SpriteBatch batch) {
    this.main = main;
    this.worldCamera = worldCamera;
    this.batch = batch;

    viewport = new FitViewport(960, 540, worldCamera);

    caster = new ShadowCaster();
  }

  /**
   * Render shit.
   */
  public void render() {
    // Get playerDetails position
    PositionComponent playerPosition = ComponentMappers.position.get(main.player);

    // Set worldCamera to follow playerDetails
    worldCamera.position.set(
        playerPosition.pos.x * Main.SPRITE_WIDTH,
        playerPosition.pos.y * Main.SPRITE_HEIGHT, 0
    );

    worldCamera.update();

    batch.setProjectionMatrix(worldCamera.combined);
    batch.begin();

    AttributesComponent playerAttributes = ComponentMappers.attributes.get(main.player);

    Map map = main.world.getCurrentMap();

    float[][] lightMap = caster.calculateFov(
        main.mapHelpers.createFovMap(),
        (int) playerPosition.pos.x, (int) playerPosition.pos.y,
        playerAttributes.vision
    );

    for (int x = 0; x < map.width; x++) {
      for (int y = 0; y < map.height; y++) {
        Cell cell = main.mapHelpers.getCell(x, y);

        if (lightMap[x][y] > 0) {
          cell.hidden = false;
        }

        if (!cell.hidden) {
          cell.forgotten = lightMap[x][y] <= 0;

          batch.setColor(1f, 1f, 1f, lightMap[x][y] <= 0.15f ? 0.15f : lightMap[x][y]);
          batch.draw(cell.sprite, x * Main.SPRITE_WIDTH, y * Main.SPRITE_HEIGHT);
          batch.setColor(1f, 1f, 1f, 1f);
        }
      }
    }

    TextureAtlas atlas = main.assets.get("sprites/main.atlas");
    PlayerComponent playerDetails = ComponentMappers.player.get(main.player);

    if (playerDetails.targetingPath != null && playerDetails.target != null) {
      for (int i = 0; i < playerDetails.targetingPath.size(); i++) {
        GridCell cell = playerDetails.targetingPath.get(i);
        boolean isLast = i == (playerDetails.targetingPath.size() - 1);
        String spritePath = isLast ? "Level/Cave/UI/Target-Throw-1" : "Level/Cave/UI/Target-Path-1";

        batch.setColor(1f, 1f, 1f, isLast ? 1f : 0.5f);
        batch.draw(
            atlas.createSprite(spritePath),
            cell.x * Main.SPRITE_WIDTH, cell.y * Main.SPRITE_HEIGHT
        );
        batch.setColor(1f, 1f, 1f, 1f);
      }
    } else if (playerDetails.lookingPath != null && playerDetails.target != null) {
      for (int i = 0; i < playerDetails.lookingPath.size(); i++) {
        GridCell cell = playerDetails.lookingPath.get(i);
        boolean isLast = i == (playerDetails.lookingPath.size() - 1);
        String spritePath = isLast ? "Level/Cave/UI/Target-1" : "Level/Cave/UI/Target-Path-1";

        batch.setColor(1f, 1f, 1f, isLast ? 1f : 0.5f);
        batch.draw(
            atlas.createSprite(spritePath),
            cell.x * Main.SPRITE_WIDTH, cell.y * Main.SPRITE_HEIGHT
        );
        batch.setColor(1f, 1f, 1f, 1f);
      }
    }

    renderStairs(lightMap);
    renderDecorations(lightMap);
    renderItems(lightMap);
    renderEnemies(lightMap);
    renderPlayer(lightMap);

    batch.end();
  }

  private void renderStairs(float[][] lightMap) {
    ImmutableArray<Entity> entrances =
        main.engine.getEntitiesFor(Family.all(EntranceComponent.class).get());
    ImmutableArray<Entity> exits =
        main.engine.getEntitiesFor(Family.all(ExitComponent.class).get());

    Object[] entities = ArrayUtils.addAll(entrances.toArray(), exits.toArray());

    for (Object e : entities) {
      Entity entity = (Entity) e;
      PositionComponent position = ComponentMappers.position.get(entity);

      if (main.entityHelpers.isVisible(entity)) {
        VisualComponent visual = ComponentMappers.visual.get(entity);

        batch.setColor(1f, 1f, 1f, lightMap[(int) position.pos.x][(int) position.pos.y]);
        batch.draw(
            visual.sprite, position.pos.x * Main.SPRITE_WIDTH, position.pos.y * Main.SPRITE_HEIGHT
        );
        batch.setColor(1f, 1f, 1f, 1f);
      }
    }
  }

  private void renderDecorations(float[][] lightMap) {
    ImmutableArray<Entity> entities =
        main.engine.getEntitiesFor(Family.all(DecorationComponent.class).get());

    for (Entity entity : entities) {
      PositionComponent position = ComponentMappers.position.get(entity);

      if (main.entityHelpers.isVisible(entity)) {
        VisualComponent visual = ComponentMappers.visual.get(entity);

        batch.setColor(1f, 1f, 1f, lightMap[(int) position.pos.x][(int) position.pos.y]);
        batch.draw(
            visual.sprite, position.pos.x * Main.SPRITE_WIDTH, position.pos.y * Main.SPRITE_HEIGHT
        );
        batch.setColor(1f, 1f, 1f, 1f);
      }
    }
  }

  private void renderItems(float[][] lightMap) {
    ImmutableArray<Entity> entities =
        main.engine.getEntitiesFor(
            Family.all(ItemComponent.class, PositionComponent.class, VisualComponent.class).get()
        );

    for (Entity entity : entities) {
      PositionComponent position = ComponentMappers.position.get(entity);

      if (main.entityHelpers.isVisible(entity)) {
        VisualComponent visual = ComponentMappers.visual.get(entity);

        batch.setColor(1f, 1f, 1f, lightMap[(int) position.pos.x][(int) position.pos.y]);
        batch.draw(
            visual.sprite, position.pos.x * Main.SPRITE_WIDTH, position.pos.y * Main.SPRITE_HEIGHT
        );
        batch.setColor(1f, 1f, 1f, 1f);
      }
    }
  }

  private void renderEnemies(float[][] lightMap) {
    ImmutableArray<Entity> entities =
        main.engine.getEntitiesFor(Family.all(EnemyComponent.class).get());

    for (Entity entity : entities) {
      PositionComponent position = ComponentMappers.position.get(entity);

      if (main.entityHelpers.isVisible(entity)) {
        VisualComponent visual = ComponentMappers.visual.get(entity);

        batch.setColor(1f, 1f, 1f, lightMap[(int) position.pos.x][(int) position.pos.y]);
        batch.draw(
            visual.sprite,
            position.pos.x * Main.SPRITE_WIDTH,
            position.pos.y * Main.SPRITE_HEIGHT + (Main.SPRITE_HEIGHT / 4)
        );
        batch.setColor(1f, 1f, 1f, 1f);
      }
    }
  }

  private void renderPlayer(float[][] lightMap) {
    ImmutableArray<Entity> entities =
        main.engine.getEntitiesFor(Family.all(PlayerComponent.class).get());

    for (Entity entity : entities) {
      PositionComponent position = ComponentMappers.position.get(entity);

      if (main.entityHelpers.isVisible(entity)) {
        VisualComponent visual = ComponentMappers.visual.get(entity);

        batch.setColor(1f, 1f, 1f, lightMap[(int) position.pos.x][(int) position.pos.y]);
        batch.draw(
            visual.sprite,
            position.pos.x * Main.SPRITE_WIDTH,
            position.pos.y * Main.SPRITE_HEIGHT + (Main.SPRITE_HEIGHT / 4)
        );
        batch.setColor(1f, 1f, 1f, 1f);
      }
    }
  }

  public void resize(int width, int height) {
    viewport.update(width, height, true);
  }
}
