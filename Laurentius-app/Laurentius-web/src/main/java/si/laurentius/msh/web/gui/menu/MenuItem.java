/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.msh.web.gui.menu;

import java.io.Serializable;

public class MenuItem implements Serializable, Comparable<MenuItem> {

  private String name;

  private String icon;

  private String type;

  private String webUrl;

  public MenuItem(String name, String type, String icon) {
    this.name = name;
    this.icon = icon;
    this.type = type;
  }

  public MenuItem(String name, String type, String icon, String wurl) {
    this.name = name;
    this.icon = icon;
    this.type = type;
    this.webUrl = wurl;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getWebUrl() {
    return webUrl;
  }

  public void setWebUrl(String wurl) {
    this.webUrl = wurl;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((icon == null) ? 0 : icon.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((webUrl == null) ? 0 : webUrl.hashCode());
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
    MenuItem other = (MenuItem) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (icon == null) {
      if (other.icon != null) {
        return false;
      }
    } else if (!icon.equals(other.icon)) {
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

  public int compareTo(MenuItem document) {
    return this.getName().compareTo(document.getName());
  }
}
