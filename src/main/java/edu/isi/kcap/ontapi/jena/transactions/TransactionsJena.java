package edu.isi.kcap.ontapi.jena.transactions;

import edu.isi.kcap.ontapi.KBAPI;
import edu.isi.kcap.ontapi.OntFactory;
import edu.isi.kcap.ontapi.transactions.TransactionsAPI;

public class TransactionsJena implements TransactionsAPI {
  protected transient OntFactory ontologyFactory;
  boolean batch;

  public TransactionsJena() {
    this.batch = false;
  }
  
  public TransactionsJena(OntFactory fac) {
    this.ontologyFactory = fac;
    this.batch = false;
  }
  
  public boolean start_read() {
    if(!this.batch)
      return this.ontologyFactory.start_read_transaction();
    return true;
  }
  
  public boolean start_write() {
    if(!this.batch)
      return this.ontologyFactory.start_write_transaction();
    return true;
  }
  
  public boolean saveAll() {
    if(!this.batch)
      return this.ontologyFactory.commit_transaction();
    return true;
  }

  public boolean save(KBAPI kb) {
    if(!this.batch)
      return kb.save();
    return true;
  }
  
  public boolean end() {
    if(!this.batch)
      return this.ontologyFactory.end_transaction();
    return true;
  }

  /* 
   * The following are used to batch multiple function calls together
   * while doing external transaction handling
   */
  
  public boolean start_batch_operation() {
    // If already running in batch mode
    if(this.batch)
      return false;
    
    this.batch = true;
    return true;
  }

  public void stop_batch_operation() {
    this.batch = false;
  }

}
