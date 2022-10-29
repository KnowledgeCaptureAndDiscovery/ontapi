package edu.isi.kcap.ontapi.transactions;

import edu.isi.kcap.ontapi.KBAPI;

public interface TransactionsAPI {
  public boolean start_read();
  
  public boolean start_write();
  
  public boolean read(Runnable r);
  
  public boolean write(Runnable r);  
  
  public boolean save(KBAPI kb);
  
  public boolean saveAll();
  
  public boolean end();

  public boolean is_in_transaction();
  
  public boolean start_batch_operation();
  
  public void stop_batch_operation();
}
