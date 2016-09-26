package me.dannytatom.xibalba;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import me.dannytatom.xibalba.components.AttributesComponent;
import me.dannytatom.xibalba.components.ItemComponent;
import me.dannytatom.xibalba.components.MouseMovementComponent;
import me.dannytatom.xibalba.components.PlayerComponent;
import me.dannytatom.xibalba.components.PositionComponent;
import me.dannytatom.xibalba.components.actions.MovementComponent;
import me.dannytatom.xibalba.components.actions.RangeComponent;
import me.dannytatom.xibalba.components.items.WeaponComponent;
import me.dannytatom.xibalba.utils.ComponentMappers;
import me.dannytatom.xibalba.world.WorldManager;

import java.util.Objects;

public class PlayerInput implements InputProcessor {
  private final OrthographicCamera worldCamera;
  public int keyHeld = -1;
  private boolean holdingShift = false;

  public PlayerInput(OrthographicCamera worldCamera) {
    this.worldCamera = worldCamera;
  }

  @Override
  public boolean keyDown(int keycode) {
    AttributesComponent attributes = ComponentMappers.attributes.get(WorldManager.player);
    PositionComponent position = ComponentMappers.position.get(WorldManager.player);
    PlayerComponent playerDetails = ComponentMappers.player.get(WorldManager.player);

    switch (keycode) {
      // Move North
      case Keys.NUMPAD_8:
        keyHeld = Keys.NUMPAD_8;

        if (WorldManager.state == WorldManager.State.PLAYING) {
          handleMovement(attributes.energy, new Vector2(position.pos.x, position.pos.y + 1));
        } else if (WorldManager.state == WorldManager.State.TARGETING) {
          handleTargeting(new Vector2(0, 1));
        } else if (WorldManager.state == WorldManager.State.LOOKING) {
          handleLooking(new Vector2(0, 1), false);
        }
        break;
      // Move North East
      case Keys.NUMPAD_9:
        keyHeld = Keys.NUMPAD_9;

        if (WorldManager.state == WorldManager.State.PLAYING) {
          handleMovement(attributes.energy, new Vector2(position.pos.x + 1, position.pos.y + 1));
        } else if (WorldManager.state == WorldManager.State.TARGETING) {
          handleTargeting(new Vector2(1, 1));
        } else if (WorldManager.state == WorldManager.State.LOOKING) {
          handleLooking(new Vector2(1, 1), false);
        }
        break;
      // Move East
      case Keys.NUMPAD_6:
        keyHeld = Keys.NUMPAD_6;

        if (WorldManager.state == WorldManager.State.PLAYING) {
          handleMovement(attributes.energy, new Vector2(position.pos.x + 1, position.pos.y));
        } else if (WorldManager.state == WorldManager.State.TARGETING) {
          handleTargeting(new Vector2(1, 0));
        } else if (WorldManager.state == WorldManager.State.LOOKING) {
          handleLooking(new Vector2(1, 0), false);
        }
        break;
      // Move South East
      case Keys.NUMPAD_3:
        keyHeld = Keys.NUMPAD_3;

        if (WorldManager.state == WorldManager.State.PLAYING) {
          handleMovement(attributes.energy, new Vector2(position.pos.x + 1, position.pos.y - 1));
        } else if (WorldManager.state == WorldManager.State.TARGETING) {
          handleTargeting(new Vector2(1, -1));
        } else if (WorldManager.state == WorldManager.State.LOOKING) {
          handleLooking(new Vector2(1, -1), false);
        }
        break;
      // Move South
      case Keys.NUMPAD_2:
        keyHeld = Keys.NUMPAD_2;

        if (WorldManager.state == WorldManager.State.PLAYING) {
          handleMovement(attributes.energy, new Vector2(position.pos.x, position.pos.y - 1));
        } else if (WorldManager.state == WorldManager.State.TARGETING) {
          handleTargeting(new Vector2(0, -1));
        } else if (WorldManager.state == WorldManager.State.LOOKING) {
          handleLooking(new Vector2(0, -1), false);
        }
        break;
      // Move South West
      case Keys.NUMPAD_1:
        keyHeld = Keys.NUMPAD_1;

        if (WorldManager.state == WorldManager.State.PLAYING) {
          handleMovement(attributes.energy, new Vector2(position.pos.x - 1, position.pos.y - 1));
        } else if (WorldManager.state == WorldManager.State.TARGETING) {
          handleTargeting(new Vector2(-1, -1));
        } else if (WorldManager.state == WorldManager.State.LOOKING) {
          handleLooking(new Vector2(-1, -1), false);
        }
        break;
      // Move West
      case Keys.NUMPAD_4:
        keyHeld = Keys.NUMPAD_4;

        if (WorldManager.state == WorldManager.State.PLAYING) {
          handleMovement(attributes.energy, new Vector2(position.pos.x - 1, position.pos.y));
        } else if (WorldManager.state == WorldManager.State.TARGETING) {
          handleTargeting(new Vector2(-1, 0));
        } else if (WorldManager.state == WorldManager.State.LOOKING) {
          handleLooking(new Vector2(-1, 0), false);
        }
        break;
      // Move North West
      case Keys.NUMPAD_7:
        keyHeld = Keys.NUMPAD_7;

        if (WorldManager.state == WorldManager.State.PLAYING) {
          handleMovement(attributes.energy, new Vector2(position.pos.x - 1, position.pos.y + 1));
        } else if (WorldManager.state == WorldManager.State.TARGETING) {
          handleTargeting(new Vector2(-1, 1));
        } else if (WorldManager.state == WorldManager.State.LOOKING) {
          handleLooking(new Vector2(-1, 1), false);
        }
        break;
      // Look
      case Keys.S:
        if (WorldManager.state == WorldManager.State.PLAYING) {
          WorldManager.state = WorldManager.State.LOOKING;
        }
        break;
      // Release
      case Keys.R:
        if (WorldManager.state == WorldManager.State.PLAYING) {
          Entity primaryWeapon = WorldManager.itemHelpers.getRightHand(WorldManager.player);

          if (primaryWeapon != null) {
            WeaponComponent weaponDetails = ComponentMappers.weapon.get(primaryWeapon);

            if (Objects.equals(weaponDetails.type, "range")) {
              if (WorldManager.itemHelpers.hasAmmunitionOfType(WorldManager.player,
                  weaponDetails.ammunitionType)) {
                playerDetails.target = null;
                playerDetails.path = null;

                WorldManager.state = WorldManager.State.TARGETING;
              } else {
                WorldManager.log.add("You aren't carrying any ammunition for this");
              }
            } else {
              WorldManager.log.add("This weapon doesn't take ammunition");
            }
          } else {
            WorldManager.log.add("You aren't holding a weapon");
          }
        }
        break;
      // Target
      case Keys.T: {
        if (WorldManager.state == WorldManager.State.PLAYING) {
          Entity primaryWeapon
              = WorldManager.itemHelpers.getRightHand(WorldManager.player);

          if (primaryWeapon != null) {
            ItemComponent itemDetails = ComponentMappers.item.get(primaryWeapon);

            if (itemDetails.actions.contains("throw", false)) {
              itemDetails.throwing = true;

              playerDetails.target = null;
              playerDetails.path = null;

              WorldManager.state = WorldManager.State.TARGETING;
            } else {
              WorldManager.log.add("You can't throw that");
            }
          }
        }
        break;
      }
      // Drop
      case Keys.D: {
        if (WorldManager.state == WorldManager.State.PLAYING) {
          Entity primaryWeapon
              = WorldManager.itemHelpers.getRightHand(WorldManager.player);

          if (primaryWeapon != null) {
            WorldManager.itemHelpers.drop(WorldManager.player, primaryWeapon);
          }
        }
        break;
      }
      // Close dialogs or cancel actions
      case Keys.Q:
        playerDetails.target = null;
        playerDetails.path = null;

        if (WorldManager.state == WorldManager.State.MOVING) {
          WorldManager.player.remove(MouseMovementComponent.class);
          WorldManager.player.remove(MovementComponent.class);
        }

        WorldManager.state = WorldManager.State.PLAYING;
        break;
      // Confirm action
      case Keys.SPACE:
        if (WorldManager.state == WorldManager.State.TARGETING) {
          if (WorldManager.itemHelpers.getThrowing(WorldManager.player) == null) {
            handleRange();
          } else {
            handleThrow();
          }
        }
        break;
      case Keys.SHIFT_LEFT:
      case Keys.SHIFT_RIGHT:
        holdingShift = true;
        break;
      default:
    }

    return true;
  }

  @Override
  public boolean keyUp(int keycode) {
    keyHeld = -1;

    switch (keycode) {
      case Keys.SHIFT_LEFT:
      case Keys.SHIFT_RIGHT:
        holdingShift = false;
        break;
      default:
    }

    return true;
  }

  @Override
  public boolean keyTyped(char character) {
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (button == 1) {
      return false;
    }

    if (WorldManager.state == WorldManager.State.PLAYING) {
      Vector2 mousePosition = Main.mousePositionToWorld(worldCamera);

      if (holdingShift) {
        Entity enemy = WorldManager.mapHelpers.getEnemyAt(mousePosition);
        PlayerComponent player = ComponentMappers.player.get(WorldManager.player);

        if (enemy != null && WorldManager.entityHelpers.isNearPlayer(enemy)) {
          player.focusedAction = PlayerComponent.FocusedAction.MELEE;
          player.focusedEntity = enemy;

          WorldManager.state = WorldManager.State.FOCUSED;

          return true;
        }
      } else {
        if (ComponentMappers.mouseMovement.get(WorldManager.player) == null) {
          if (WorldManager.mapHelpers.cellExists(mousePosition)
              && !WorldManager.mapHelpers.getCell(mousePosition.x, mousePosition.y).hidden) {
            WorldManager.player.add(new MouseMovementComponent());

            WorldManager.state = WorldManager.State.MOVING;
            WorldManager.executeTurn = true;

            return true;
          }
        }
      }
    } else if (WorldManager.state == WorldManager.State.TARGETING) {
      if (WorldManager.itemHelpers.getThrowing(WorldManager.player) == null) {
        handleRange();
      } else {
        handleThrow();
      }
    }

    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return false;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    PlayerComponent playerDetails = ComponentMappers.player.get(WorldManager.player);
    PositionComponent playerPosition = ComponentMappers.position.get(WorldManager.player);
    Vector2 mousePosition = Main.mousePositionToWorld(worldCamera);
    Vector2 relativeToPlayer = mousePosition.cpy().sub(playerPosition.pos);

    if (playerDetails.target != null && playerDetails.target.epsilonEquals(mousePosition, 0.00001f)) {
      return false;
    }

    if (WorldManager.state == WorldManager.State.PLAYING) {
      playerDetails.target = null;
      playerDetails.path = null;

      if (WorldManager.mapHelpers.cellExists(mousePosition)) {
        handleLooking(relativeToPlayer, true);

        return true;
      }
    } else if (WorldManager.state == WorldManager.State.LOOKING) {
      playerDetails.target = null;
      playerDetails.path = null;

      if (WorldManager.mapHelpers.cellExists(mousePosition)) {
        handleLooking(relativeToPlayer, false);

        return true;
      }
    } else if (WorldManager.state == WorldManager.State.TARGETING) {
      playerDetails.target = null;
      playerDetails.path = null;

      if (WorldManager.mapHelpers.cellExists(mousePosition)) {
        handleTargeting(relativeToPlayer);

        return true;
      }
    }

    return false;
  }

  @Override
  public boolean scrolled(int amount) {
    if (amount == 1 && worldCamera.zoom < 1) {
      worldCamera.zoom += 0.2f;
    } else if (amount == -1 && worldCamera.zoom > 0.4f) {
      worldCamera.zoom -= 0.2f;
    }

    Gdx.app.log("PlayerInput", "Zoom level: " + worldCamera.zoom + "");

    return true;
  }

  /**
   * Handles player movement.
   *
   * @param energy How much energy the player has
   * @param pos    The position we're attempting to move to
   */
  private void handleMovement(int energy, Vector2 pos) {
    if (holdingShift) {
      Entity enemy = WorldManager.mapHelpers.getEnemyAt(pos);
      PlayerComponent player = ComponentMappers.player.get(WorldManager.player);

      if (enemy != null) {
        WorldManager.state = WorldManager.State.FOCUSED;
        player.focusedAction = PlayerComponent.FocusedAction.MELEE;
        player.focusedEntity = enemy;
      }
    } else {
      if (energy >= MovementComponent.COST) {
        WorldManager.player.add(new MovementComponent(pos));

        WorldManager.executeTurn = true;
      }
    }

    PlayerComponent playerDetails = ComponentMappers.player.get(WorldManager.player);

    playerDetails.path = null;
  }

  private void handleTargeting(Vector2 pos) {
    WorldManager.mapHelpers.createTargetingPath(
        ComponentMappers.position.get(WorldManager.player).pos, pos
    );
  }

  private void handleLooking(Vector2 pos, boolean careAboutWalls) {
    WorldManager.mapHelpers.createLookingPath(
        ComponentMappers.position.get(WorldManager.player).pos, pos, careAboutWalls
    );
  }

  private void handleThrow() {
    AttributesComponent attributes = ComponentMappers.attributes.get(WorldManager.player);

    if (attributes.energy < RangeComponent.COST) {
      return;
    }

    PlayerComponent playerDetails = ComponentMappers.player.get(WorldManager.player);

    if (holdingShift) {
      Entity enemy = WorldManager.mapHelpers.getEnemyAt(playerDetails.target);
      PlayerComponent player = ComponentMappers.player.get(WorldManager.player);

      if (enemy != null) {
        playerDetails.focusedAction = PlayerComponent.FocusedAction.THROWING;
        WorldManager.state = WorldManager.State.FOCUSED;
        player.focusedEntity = enemy;
      }
    } else {
      WorldManager.combatHelpers.preparePlayerForThrowing(playerDetails.target, "body");
      WorldManager.executeTurn = true;

      WorldManager.state = WorldManager.State.PLAYING;
    }
  }

  private void handleRange() {
    AttributesComponent attributes = ComponentMappers.attributes.get(WorldManager.player);

    if (attributes.energy < RangeComponent.COST) {
      return;
    }

    PlayerComponent playerDetails = ComponentMappers.player.get(WorldManager.player);

    if (holdingShift) {
      Entity enemy = WorldManager.mapHelpers.getEnemyAt(playerDetails.target);
      PlayerComponent player = ComponentMappers.player.get(WorldManager.player);

      if (enemy != null) {
        playerDetails.focusedAction = PlayerComponent.FocusedAction.RANGED;
        WorldManager.state = WorldManager.State.FOCUSED;
        player.focusedEntity = enemy;
      }
    } else {
      WorldManager.combatHelpers.preparePlayerForRanged(playerDetails.target, "body");
      WorldManager.executeTurn = true;

      WorldManager.state = WorldManager.State.PLAYING;
    }
  }
}
