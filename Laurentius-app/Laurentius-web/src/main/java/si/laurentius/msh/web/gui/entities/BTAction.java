/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.web.gui.entities;

/**
 *
 * @author Jože Rihtaršič
 */
public class BTAction {

  String leftEnd;
  String name;
  String rightEnd;

  /**
   *
   * @param name
   * @param leftEnd
   * @param rightEnd
   */
  public BTAction(String name, String leftEnd, String rightEnd) {
    this.name = name;
    this.leftEnd = leftEnd;
    this.rightEnd = rightEnd;
  }

  /**
   *
   * @return
   */
  public String getLeftEnd() {
    return leftEnd;
  }

  /**
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @return
   */
  public String getRightEnd() {
    return rightEnd;
  }

  /**
   *
   * @param leftEnd
   */
  public void setLeftEnd(String leftEnd) {
    this.leftEnd = leftEnd;
  }

  /**
   *
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   *
   * @param rightEnd
   */
  public void setRightEnd(String rightEnd) {
    this.rightEnd = rightEnd;
  }

}
