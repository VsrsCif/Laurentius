/*
 * Copyright 2016, Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.laurentius.ejb.cache;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jože Rihtaršič
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
