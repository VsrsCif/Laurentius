/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.test.db;

import javax.persistence.EntityTransaction;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 *
 * @author Jože Rihtaršič
 */
public class MockUserTransaction implements UserTransaction {

  EntityTransaction met;

  /**
   *
   * @param et
   */
  public MockUserTransaction(EntityTransaction et) {
    met = et;
  }

  /**
   *
   * @throws SystemException
   */
  @Override
  public void begin() throws NotSupportedException, SystemException {
    met.begin();
  }

  /**
   *
   * @throws HeuristicMixedException
   * @throws SecurityException
   * @throws IllegalStateException
   */
  @Override
  public void commit() throws RollbackException, HeuristicMixedException,
      HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
    met.commit();
  }

  /**
   *
   * @return @throws SystemException
   */
  @Override
  public int getStatus() throws SystemException {

    return met.isActive() ? Status.STATUS_ACTIVE : Status.STATUS_NO_TRANSACTION;
  }

  /**
   *
   * @throws IllegalStateException
   * @throws SecurityException
   * @throws SystemException
   */
  @Override
  public void rollback() throws IllegalStateException, SecurityException, SystemException {
    met.rollback();
  }

  /**
   *
   * @throws IllegalStateException
   * @throws SystemException
   */
  @Override
  public void setRollbackOnly() throws IllegalStateException, SystemException {
    met.setRollbackOnly();
  }

  /**
   *
   * @param seconds
   * @throws SystemException
   */
  @Override
  public void setTransactionTimeout(int seconds) throws SystemException {
    // ingore
  }

}
