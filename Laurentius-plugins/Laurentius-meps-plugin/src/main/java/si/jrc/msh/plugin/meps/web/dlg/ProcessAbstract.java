/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.meps.web.dlg;

/**
 *
 * @author sluzba
 */
public abstract class ProcessAbstract {
  protected String processMessage = "";
  protected String processTitle = "";
  protected int progress = 0;
  private boolean stop = true;
  
  public String getProcessMessage() {
    return processMessage;
  }

  public void setProcessMessage(String processMessage) {
    this.processMessage = processMessage;
  }

  public int getProgress() {
    
    return progress;
  }

  public void setProgress(int progress) {
    this.progress = progress;
  }

  public boolean getStop() {
    return stop;
  }

  public void setStop(boolean stop) {
    this.stop = stop;
  }

  public String getProcessTitle() {
    return processTitle;
  }

  public void setProcessTitle(String processTitle) {
    this.processTitle = processTitle;
  }
  
  
}
