/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.ejb;

import java.util.Calendar;
import java.util.HashMap;

/**
 *
 * @author sluzba
 */
public class SimpleObjectCache {

  public static final long S_UPDATE_TIMEOUT = 10 * 60 * 1000; // 10 minutes
  private final HashMap<Object, Object> mlstCacheObject = new HashMap<>();
  private final HashMap<Object, Long> mlstCachedObjectTime = new HashMap<>();

  synchronized public <T> void cacheObject(T lst, Object c) {
    if (mlstCacheObject.containsKey(c)) {
      mlstCacheObject.replace(c, lst);
    } else {
      mlstCacheObject.put(c, lst);
    }

    if (mlstCachedObjectTime.containsKey(c)) {
      mlstCachedObjectTime.replace(c, Calendar.getInstance().getTimeInMillis());
    } else {
      mlstCachedObjectTime.put(c, Calendar.getInstance().getTimeInMillis());
    }
  }

  synchronized public void clearAllCache() {

    mlstCacheObject.clear();
    mlstCachedObjectTime.clear();

  }

  synchronized public void clearCachedObject(Class c) {
    if (mlstCacheObject.containsKey(c)) {
      mlstCacheObject.remove(c);
      mlstCachedObjectTime.remove(c);
    }

  }

  public <T> T getFromCachedObject(Object c) {
    return mlstCacheObject.containsKey(c) ? (T) mlstCacheObject.get(c) :null;
  }

  public <T> boolean cacheObjectTimeout(Object c) {
    return !mlstCachedObjectTime.containsKey(c)
            || (Calendar.getInstance().getTimeInMillis() - mlstCachedObjectTime.
            get(c)) > S_UPDATE_TIMEOUT;
  }

  public <T> boolean cacheObjectTimeout(Object c, long lAfter) {
    return SimpleObjectCache.this.cacheObjectTimeout(c)
            || (mlstCachedObjectTime.containsKey(c) && lAfter > mlstCachedObjectTime.
            get(c));
  }
}
