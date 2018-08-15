
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
package edu.isi.wings.ontapi.util;


import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/**
 * Servlet implementation class SparqlEndpoint
 */
public class SparqlAPI {
 
  String tdbdir;
  
  public SparqlAPI(String tripleStoreDir) {
    this.tdbdir = tripleStoreDir;
  }

  public void showQueryResults(String queryString, String format, PrintStream out) 
      throws IOException {
    Query query = QueryFactory.create(queryString);
    if(query.isSelectType()) {
      ResultsFormat fmt = ResultsFormat.lookup(format);
      
      Dataset tdbstore = TDBFactory.createDataset(this.tdbdir);
      QueryExecution qexec = QueryExecutionFactory.create(query, tdbstore);
      qexec.getContext().set(TDB.symUnionDefaultGraph, true);
      ResultSet results = qexec.execSelect();
      if(fmt == null) {
        out.print(queryString+"\n");
        ResultSetFormatter.out(out, results, query);
      }
      else
        ResultSetFormatter.output(out, results, fmt);
    }
    else {
      out.print("Only select queries allowed");
    }
  }
  
  public void executeUpdateQuery(String updateString, PrintStream out) 
      throws IOException {
    Dataset tdbstore = TDBFactory.createDataset(this.tdbdir);
    UpdateRequest update = UpdateFactory.create(updateString);
    UpdateAction.execute(update, tdbstore);
    out.print("Updated");
    TDB.sync(tdbstore);
  }
  
  public void updateGraphURLs(String cururl, String newurl, PrintStream out) {
    Dataset tdbstore = TDBFactory.createDataset(this.tdbdir);
    try {out.println(cururl +" ==>> "+newurl);}
    catch (Exception e) {System.out.println(cururl +" ==>> "+newurl);}
    
    // Update all graphs in the Dataset
    ArrayList<String> graphnames = new ArrayList<String>();
    try {
      Query query = QueryFactory.create("SELECT DISTINCT ?g { GRAPH ?g { ?s ?p ?o }}");
      QueryExecution qexec = QueryExecutionFactory.create(query, tdbstore);
      qexec.getContext().set(TDB.symUnionDefaultGraph, true);
      ResultSet results = qexec.execSelect();
      while(results.hasNext()) {
        QuerySolution soln = results.next();
        RDFNode graph = soln.get("g");
        if(graph.isURIResource())
          graphnames.add(graph.asResource().getURI());
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    
    for(String graphname : graphnames) {
      if(graphname.startsWith(cururl)) {
        String newname = graphname.replace(cururl, newurl);
        try {out.println(graphname + " -> "+newname);}
        catch (Exception e) {System.out.println(graphname + " -> "+newname);}
        
        try {
          Model m = tdbstore.getNamedModel(graphname);
          Model nm = ModelFactory.createDefaultModel();
          
          for(StmtIterator iter = m.listStatements(); iter.hasNext(); ) {
            Statement st = iter.next();
            Resource subj = st.getSubject();
            Property pred = st.getPredicate();
            RDFNode obj = st.getObject();
            if(subj.isURIResource() && subj.getURI().startsWith(cururl)) {
              String nurl = subj.getURI().replace(cururl, newurl);
              subj = new ResourceImpl(nurl);
            }
            if(pred.getURI().startsWith(cururl)) {
              String nurl = pred.getURI().replace(cururl, newurl);
              pred = new PropertyImpl(nurl);
            }
            if(obj.isURIResource() && obj.asResource().getURI().startsWith(cururl)) {
              String nurl = obj.asResource().getURI().replace(cururl, newurl);
              obj = new ResourceImpl(nurl);
            }
            nm.add(new StatementImpl(subj, pred, obj));
          }
          tdbstore.removeNamedModel(graphname);
          tdbstore.addNamedModel(newname, nm);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    TDB.sync(tdbstore);
  }

}
