/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.plugin.tc.web;

import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author Jože Rihtaršič
 */
@ManagedBean
@SessionScoped
public class LocaleManager {

  private Locale locale;

  /**
   *
   * @return
   */
  public String getLanguage() {
    return locale.getLanguage();
  }

  /**
   *
   * @return
   */
  public Locale getLocale() {
    return locale;
  }

  /**
     *
     */
  @PostConstruct
  public void init() {
    locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
  }

  /**
   *
   * @param language
   */
  public void setLanguage(String language) {
    locale = new Locale(language);
    FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
  }

}
