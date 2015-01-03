package me.dannytatom.xibalba.components;

import com.badlogic.ashley.core.Component;

public class SkillsComponent extends Component {
  public int unarmed = 4;
  public int unarmedCounter = 0;
  public int throwing = 0;
  public int throwingCounter = 0;
  public int slashing = 0;
  public int slashingCounter = 0;
  public int stabbing = 0;
  public int stabbingCounter = 0;

  public SkillsComponent() {

  }
}
