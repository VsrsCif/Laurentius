/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.msh.web.pmode;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
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
import si.laurentius.msh.pmode.Service;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;

@ManagedBean(name = "pModeServiceGraphView")
@SessionScoped
public class PModeServiceGraphView extends AbstractAdminJSFView<Service.Action> {

  private DefaultDiagramModel model;

  private Service editableService;

  @Override
  public void createEditable() {

    Service.Action act = new Service.Action();
     act.setName("TestAction");
    act.setInvokeRole(ActionRole.Initiator.getValue());
    act.setPayloadProfiles(new Service.Action.PayloadProfiles());

    Service.Action.PayloadProfiles.PayloadProfile pf = new Service.Action.PayloadProfiles.PayloadProfile();
    pf.setName("payload");
    pf.setMIME(MimeValue.MIME_BIN.getMimeType());
    pf.setMinOccurs(0);
    pf.setMaxOccurs(100);
    pf.setMaxSize(BigInteger.valueOf(10 * 1024 * 1024));
    
    act.getPayloadProfiles().setMaxSize(BigInteger.valueOf(10 * 1024 * 1024));
    act.getPayloadProfiles().getPayloadProfiles().add(pf);
    setNew(act);
  }

  @Override
  public List<Service.Action> getList() {
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
    connector.setPaintStyle("{strokeStyle:'#787F57', lineWidth:3}");
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
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean removeSelected() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public void setService(Service srv) {
    editableService = srv;
    updateGraph();
  }

  public void updateGraph() {
    model.clearElements();
    if (editableService != null) {
      for (int i = 0; i < editableService.getActions().size(); i++) {
        Service.Action a = editableService.getActions().get(i);
        addAction(a, i);

      }
      initHeader(editableService);
    }
  }

  private void addAction(Service.Action act, int index) {

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
          Service.Action act) {
    Connection conn = null;
    if (Objects.equals(act.getInvokeRole(), ActionRole.Executor.getValue())) {
      conn = new Connection(to, from);
    } else {
      conn = new Connection(from, to);
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
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean validateData() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public void selectedInstanceToTop() {
    Service.Action spi = getSelected();

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
    Service.Action spi = getSelected();

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
    Service.Action spi = getSelected();

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
    Service.Action spi = getSelected();

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
