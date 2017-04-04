/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.ejb.cache;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author sluzba
 */
public class SimpleListCache {

  public static final long S_UPDATE_TIMEOUT = 10 * 60 * 1000; // 10 minutes
  private final HashMap<Object, List<?>> mlstCacheList = new HashMap<>();
  private final HashMap<Object, Long> mlstCachedListTime = new HashMap<>();

  synchronized public <T> void cacheList(List<T> lst, Object c) {
    if (mlstCacheList.containsKey(c)) {
      mlstCacheList.get(c).clear();
      mlstCacheList.replace(c, lst);
    } else {
      mlstCacheList.put(c, lst);
    }

    if (mlstCachedListTime.containsKey(c)) {
      mlstCachedListTime.replace(c, Calendar.getInstance().getTimeInMillis());
    } else {
      mlstCachedListTime.put(c, Calendar.getInstance().getTimeInMillis());
    }
  }

  synchronized public void clearAllCache() {
    for (Object c : mlstCacheList.keySet()) {
      mlstCacheList.get(c).clear();
    }
    mlstCacheList.clear();
    mlstCachedListTime.clear();

  }

  synchronized public void clearCachedList(Object c) {
    if (mlstCacheList.containsKey(c)) {
      mlstCacheList.get(c).clear();
      mlstCacheList.remove(c);
      mlstCachedListTime.remove(c);
    }

  }

  public <T> List<T> getFromCachedList(Object c) {
    return mlstCacheList.containsKey(c) ? Collections.unmodifiableList((List<T>) mlstCacheList.get(c)) : Collections.emptyList();
  }

  public <T> boolean cacheListTimeout(Object c) {
    return !mlstCachedListTime.containsKey(c)
            || (Calendar.getInstance().getTimeInMillis() - mlstCachedListTime.
            get(c)) > S_UPDATE_TIMEOUT;
  }
  public <T> boolean cacheListTimeout(Object c, long lAfter) {
    return SimpleListCache.this.cacheListTimeout(c)  
            || (mlstCachedListTime.containsKey(c) && lAfter > mlstCachedListTime.get(c));
  }
}
