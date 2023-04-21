/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.msh.web.pmode;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.DiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.StraightConnector;
import org.primefaces.model.diagram.endpoint.BlankEndPoint;
import org.primefaces.model.diagram.endpoint.EndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;
import org.primefaces.model.diagram.overlay.ArrowOverlay;
import org.primefaces.model.diagram.overlay.LabelOverlay;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.pmode.enums.ActionRole;
import si.laurentius.commons.pmode.enums.MessageType;
import si.laurentius.msh.pmode.Action;
import si.laurentius.msh.pmode.PayloadProfile;
import si.laurentius.msh.pmode.Service;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;

@Named("pModeServiceGraphView")
@SessionScoped
public class PModeServiceGraphView extends AbstractAdminJSFView<Action> {

  private DefaultDiagramModel model;

  private Service editableService;

  @Override
  public void createEditable() {

    String sbname = "ation_%03d";
    int i = 1;
    while (actionExists(String.format(sbname, i))) {
      i++;
    }

    Action act = new Action();
    act.setName(String.format(sbname, i));
    act.setInvokeRole(ActionRole.Initiator.getValue());
    act.setMessageType(MessageType.UserMessage.getValue());
    act.setPayloadProfiles(new Action.PayloadProfiles());

    PayloadProfile pf = new PayloadProfile();
    pf.setName("payload");
    pf.setMIME(MimeValue.MIME_BIN.getMimeType());
    pf.setMinOccurs(0);
    pf.setMaxOccurs(100);
    pf.setMaxSize(BigInteger.valueOf(15 * 1024 * 1024));

    act.getPayloadProfiles().setMaxSize(BigInteger.valueOf(15 * 1024 * 1024));
    act.getPayloadProfiles().getPayloadProfiles().add(pf);
    setNew(act);
  }

  private boolean actionExists(String action) {
    for (Action act : editableService.getActions()) {
      if (Objects.equals(act.getName(), action)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<Action> getList() {
    if (editableService != null) {
      return editableService.getActions();
    }
    return Collections.emptyList();

  }

  @PostConstruct
  public void init() {
    model = new DefaultDiagramModel();
    model.setMaxConnections(-1);

    model.getDefaultConnectionOverlays().add(new ArrowOverlay(20, 20, 1, 1));
    StraightConnector connector = new StraightConnector();
   /// connector.setPaintStyle("{strokeStyle: '#787F57', lineWidth:3, lineDash:5'}");
    connector.setPaintStyle("{strokeStyle: '#585FFF', lineWidth:4}");
    
 
    ;
    model.setDefaultConnector(connector);

  }

  private void initHeader(Service srv) {
    Element partyA = new Element("Initiator", "2em", "2em");
    partyA.setStyleClass("ui-diagram-squence-head");
    Element partyB = new Element("Executor", "27em", "2em");
    partyB.setStyleClass("ui-diagram-squence-head");
    // primefaces bug - if there is no endpoint on element non of connector is
    // visible?
    partyA.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
    partyB.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
    //
    model.addElement(partyA);
    model.addElement(partyB);

  }

  @Override
  public boolean persistEditable() {
    boolean bsuc = false;

    Action ecj = getEditable();
    if (ecj != null) {

      bsuc = editableService.getActions().add(ecj);
      updateGraph();
    } else {
      addError("No editable service!");
    }
    return bsuc;
  }

  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    Action ecj = getSelected();
    if (ecj != null) {
      for (int i = 0; i < editableService.getActions().size(); i++) {
        Action act = editableService.getActions().get(i);
        if (Objects.equals(act.getName(), ecj.getName())) {
          editableService.getActions().remove(i);
          updateGraph();
          bSuc = true;
          break;
        }
      }
      setSelected(null);

    } else {
      addError("Select Action!");
    }
    return bSuc;
  }

  public void setService(Service srv) {
    editableService = srv;
    updateGraph();
  }

  public void updateGraph() {
    model.clearElements();
    if (editableService != null) {
      for (int i = 0; i < editableService.getActions().size(); i++) {
        Action a = editableService.getActions().get(i);
        addAction(a, i);

      }
      initHeader(editableService);
    }
  }

  private void addAction(Action act, int index) {

    Element partyBodyA = new Element(null, "5em", (index * 6 + 5) + "em");
    partyBodyA.setStyleClass("ui-diagram-squence-body");

    Element partyBodyB = new Element(null, "30em", (index * 6 + 5) + "em");
    partyBodyB.setStyleClass("ui-diagram-squence-body");

    partyBodyA.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
    partyBodyB.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
    model.addElement(partyBodyA);
    model.addElement(partyBodyB);

    model.connect(createConnection(partyBodyA.getEndPoints().get(0), partyBodyB.
            getEndPoints().get(0), act));

  }

  private Connection createConnection(EndPoint from, EndPoint to,
          Action act) {
    Connection conn = null;
    if (Objects.equals(act.getInvokeRole(), ActionRole.Executor.getValue())) {
      conn = new Connection(to, from);
    
    } else {
      conn = new Connection(from, to);
    }
    if (act.getMessageType()!=null &&  Objects.equals(MessageType.SignalMessage.getValue(),  act.getMessageType())){
       StraightConnector connector = new StraightConnector();
        connector.setPaintStyle("{strokeStyle: '#989FAF', lineWidth:2, lineDash:5}");
        conn.setConnector(connector);
    }

    if (act.getName() != null) {
      conn.getOverlays().add(new LabelOverlay(act.getName(), "action-connector",
              0.5));
    }

    return conn;
  }

  public DiagramModel getModel() {
    return model;
  }

  @Override
  public boolean updateEditable() {
    Action ecj = getEditable();
    boolean bsuc = false;
    if (ecj != null) {
      for (int i = 0; i < editableService.getActions().size(); i++) {
        Action act = editableService.getActions().get(i);
        if (Objects.equals(act.getName(), ecj.getName())) {
          editableService.getActions().remove(i);
          editableService.getActions().add(i, ecj);
          updateGraph();
          bsuc = true;
          break;
        }
      }
    }
    return bsuc;
  }

  @Override
  public boolean validateData() {
    return true;
  }

  @Override
  public String getSelectedDesc() {
    return getSelected()!= null?getSelected().getName():"";  
  }

  public void selectedInstanceToTop() {
    Action spi = getSelected();

    if (editableService != null && getSelected() != null) {
      int idx = editableService.getActions().indexOf(spi);
      if (idx > 0) {
        editableService.getActions().remove(spi);
        editableService.getActions().add(0, spi);
      }
      updateGraph();
    } else {
      addError("Select action!");
    }
  }

  public void selectedInstanceUp() {
    Action spi = getSelected();

    if (editableService != null && spi != null) {
      int idx = editableService.getActions().indexOf(spi);
      if (idx > 0) {
        editableService.getActions().remove(spi);
        editableService.getActions().add(--idx, spi);
      }
      updateGraph();
    } else {
      addError("Select action!");
    }
  }

  public void selectedInstanceDown() {
    Action spi = getSelected();

    if (editableService != null && spi != null) {
      int idx = editableService.getActions().indexOf(spi);
      if (idx < editableService.getActions().size() - 1) {
        editableService.getActions().remove(spi);
        editableService.getActions().add(++idx, spi);
      }
      updateGraph();
    } else {
      addError("Select action!");
    }
  }

  public void selectedInstanceToBottom() {
    Action spi = getSelected();

    if (editableService != null && spi != null) {
      int idx = editableService.getActions().indexOf(spi);
      if (idx < editableService.getActions().size() - 1) {
        editableService.getActions().remove(spi);
        editableService.getActions().add(spi);
      }
      updateGraph();
    } else {
      addError("Select action!");
    }
  }

}
