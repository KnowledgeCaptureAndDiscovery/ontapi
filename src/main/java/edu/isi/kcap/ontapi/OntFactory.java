/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.isi.kcap.ontapi;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.TxnType;
import org.apache.jena.reasoner.rulesys.BuiltinRegistry;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.LocationMapper;

import java.io.InputStream;

import edu.isi.kcap.ontapi.jena.KBAPIJena;
import edu.isi.kcap.ontapi.jena.KBObjectJena;
import edu.isi.kcap.ontapi.jena.KBTripleJena;
import edu.isi.kcap.ontapi.jena.extrules.date.*;
import edu.isi.kcap.ontapi.jena.extrules.hash.Md5Hash;
import edu.isi.kcap.ontapi.jena.extrules.math.*;
import edu.isi.kcap.ontapi.jena.rules.KBRuleJena;
import edu.isi.kcap.ontapi.jena.rules.KBRuleListJena;
import edu.isi.kcap.ontapi.rules.KBRule;
import edu.isi.kcap.ontapi.rules.KBRuleList;

public class OntFactory {
	public static int JENA = 0;
	public static int SESAME = 1;

	int type;
	
  Dataset tdbstore;

  boolean usetdb = false;
  
	// Type of Factory (default JENA used for now, can add more here)
	public OntFactory(int type) {
		this.type = type;
		if (this.type == JENA) {
			OntDocumentManager.getInstance().clearCache();
			OntDocumentManager.getInstance().setCacheModels(false);
			FileManager.get().setModelCaching(false);

			// Extra Rules
			BuiltinRegistry.theRegistry.register(new Power());
			BuiltinRegistry.theRegistry.register(new Log());
			BuiltinRegistry.theRegistry.register(new AddDays());
			BuiltinRegistry.theRegistry.register(new SubtractDays());
			BuiltinRegistry.theRegistry.register(new Md5Hash());
		}
	}

	public OntFactory(int type, String tripleStoreDirectory) {
		this(type);
		this.usetdb = true;
    if(tdbstore == null)
      tdbstore = TDBFactory.createDataset(tripleStoreDirectory);   
	}
	
	public boolean usesTripleStore(KBAPI kb) {
	  if(this.type == JENA) {
	    KBAPIJena jkb = (KBAPIJena) kb;
	    if(jkb.tdbstore != null)
	      return true;
	  }
	  return false;
	}
	 
	public void useTripleStore(KBAPI kb) {
	  if(this.type == JENA && tdbstore != null) {
	    KBAPIJena jkb = (KBAPIJena) kb;
      jkb.tdbstore = tdbstore;
	  }
	}

  public void noTripleStore(KBAPI kb) {
    if(this.type == JENA) {
      KBAPIJena jkb = (KBAPIJena) kb;    
      jkb.tdbstore = null;
    }
  }
  
	public KBAPI getKB(String url, OntSpec spec) throws Exception {
		if (this.type == JENA) {
			if (this.usetdb)
				return new KBAPIJena(url, tdbstore, spec);
			else
				return new KBAPIJena(url, spec);
		}
		return null;
	}

	public KBAPI getKB(String url, OntSpec spec, boolean create_if_empty) throws Exception {
		return this.getKB(url, spec, create_if_empty, false);
	}
	
	public KBAPI getKB(String url, OntSpec spec, boolean create_if_empty, boolean cache_url) 
			throws Exception {
		if (this.type == JENA) {
			if (this.usetdb)
				return new KBAPIJena(url, tdbstore, spec, cache_url);
			else
				return new KBAPIJena(url, spec, create_if_empty, cache_url);
		}
		return null;
	}

	public KBAPI getKB(InputStream data, String base, OntSpec spec) {
		if (this.type == JENA) {
		  this.usetdb = false;
			return new KBAPIJena(data, base, spec);
		}
		return null;
	}

	public KBAPI getKB(OntSpec spec) {
		if (this.type == JENA) {
		  return new KBAPIJena(spec);
		}
		return null;
	}

	public KBObject getObject(String id) {
		if (this.type == JENA) {
			return new KBObjectJena(id);
		}
		return null;
	}

	public KBObject getDataObject(Object value) {
		if (this.type == JENA) {
			return new KBObjectJena(value, true);
		}
		return null;
	}
	
	public KBRule parseRule(String text) {
		if (this.type == JENA) {
			return new KBRuleJena(text);
		}
		return null;
	}

	public KBRuleList parseRules(String text) {
		if (this.type == JENA) {
			return new KBRuleListJena(text);
		}
		return null;
	}
	
	public KBRuleList createEmptyRuleList() {
		if (this.type == JENA) {
			return new KBRuleListJena();
		}
		return null;
	}
	
	public void addAltEntry(String ns, String file) {
		// System.out.println("Using local ontologies");
		if (this.type == JENA) {
			// Setup alternate URLS for a localized system
			OntDocumentManager.getInstance().getFileManager().getLocationMapper()
					.addAltEntry(ns, file);
			LocationMapper.get().addAltEntry(ns, file);
		} else if (this.type == SESAME) {

		}
	}

	public void addAltPrefix(String nsPrefix, String dir) {
		// System.out.println("Using local ontologies");
		if (this.type == JENA) {
			// Setup alternate URLS for a localized system
			OntDocumentManager.getInstance().getFileManager().getLocationMapper()
					.addAltPrefix(nsPrefix, dir);
			LocationMapper.get().addAltPrefix(nsPrefix, dir);
		} else if (this.type == SESAME) {

		}
	}

	public KBTriple getTriple(KBObject subj, KBObject pred, KBObject obj) {
		if (this.type == JENA) {
			return new KBTripleJena(subj, pred, obj);
		}
		return null;
	}
	
	// Transactions
  public boolean start_read_transaction() {
    if(this.usetdb && !tdbstore.isInTransaction()) {
      tdbstore.begin(TxnType.READ);
    }
    return true;
  }
  
  public boolean start_write_transaction() {
    if(this.usetdb && !tdbstore.isInTransaction()) {
      tdbstore.begin(TxnType.WRITE);
    }
    return true;
  }
  
  public boolean commit_transaction() {
    if(this.usetdb && !tdbstore.isInTransaction()) {
      tdbstore.commit();
    }
    return true;    
  }
  
  public boolean end_transaction() {
     if(this.usetdb && tdbstore.isInTransaction()) {
       if(tdbstore.supportsTransactionAbort()) {
         tdbstore.abort();
       }
       tdbstore.end();
     }
     return true;
  }	
  
	public static void shutdown() {
	  /*if(tdbstore.isInTransaction())
	    tdbstore.abort();
	  tdbstore.end();
	  tdbstore.close();
	  TDBFactory.release(tdbstore);*/
	}
}
