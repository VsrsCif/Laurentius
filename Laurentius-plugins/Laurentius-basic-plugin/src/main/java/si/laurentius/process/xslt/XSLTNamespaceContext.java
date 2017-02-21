/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.process.xslt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.namespace.NamespaceContext;
import si.laurentius.plugin.imp.Namespace;


/**
 *
 * @author sluzba
 */
public class XSLTNamespaceContext implements NamespaceContext{
  
  Map<String, String> mpNS= new HashMap<>();
  public XSLTNamespaceContext(List<Namespace> lst) {
    for (Namespace n: lst){
      mpNS.put(n.getPrefix(),n.getNamespace());
    }
  }

  
  
  @Override
  public String getNamespaceURI(String prefix) {
    if (mpNS.containsKey(prefix)){
      return mpNS.get(prefix);
    }
    return null;
  }

  @Override
  public String getPrefix(String namespaceURI) {
    if (mpNS.containsValue(namespaceURI)){
      for (String key: mpNS.keySet()){
        if (Objects.equals(mpNS.get(key), namespaceURI)){
          return key;
        }
      }
    }
    return null;
  }

  @Override
  public Iterator getPrefixes(String namespaceURI) {
    List<String> lst = new ArrayList<>();
    if (mpNS.containsValue(namespaceURI)){      
      mpNS.keySet().stream().filter((key) ->
          (Objects.equals(mpNS.get(key), namespaceURI))).forEachOrdered((key) -> {
            lst.add(key);
      });
    }
    return lst.iterator();
  }
  
}
