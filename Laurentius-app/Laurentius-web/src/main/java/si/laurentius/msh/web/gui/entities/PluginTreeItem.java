/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.msh.web.gui.entities;

import java.io.Serializable;

/**
 *
 * @author sluzba
 */
public class PluginTreeItem implements Serializable, Comparable<PluginTreeItem> {

  private String name;
  private Object pluginItem;

  private String type;

  public PluginTreeItem(String name, Object pluginItem, String type) {
    this.name = name;
    this.pluginItem = pluginItem;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Object getPluginItem() {
    return pluginItem;
  }
  
  

  @Override
  public int hashCode() {
    final int prime = 65537;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());

    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PluginTreeItem other = (PluginTreeItem) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }

    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int compareTo(PluginTreeItem document) {
    return this.getName().compareTo(document.getName());
  }
}
