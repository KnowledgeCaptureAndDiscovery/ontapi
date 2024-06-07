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

package edu.isi.wings.ontapi.tests;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.tdb.TDBFactory;
import openllet.jena.PelletReasonerFactory;
import org.junit.jupiter.api.Test;

public class KBTests {
	static String onturl = "https://wings-workflows.org/ontology/workflow.owl";
	static String url = "http://localhost:8080/wings/export/users/1/DMDomain/workflows/ModelAndClassify.owl";
	@Test	
	void write() {
		Dataset tdbstore = TDBFactory.createDataset("/tmp/test");
		try {
			tdbstore.begin(ReadWrite.WRITE);
			Model impmodel = tdbstore.getNamedModel(onturl);
			if(impmodel.isEmpty()) {
				impmodel.read(onturl);
			}
			OntModel ontmodel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, impmodel);
			tdbstore.commit();
			
			tdbstore.begin(ReadWrite.WRITE);
			OntModel model =  ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, tdbstore.getNamedModel(url));
			model.addSubModel(ontmodel);
			Property prop = model.getProperty(onturl + "#hasVersion");
			Individual tpl = model.createIndividual(url + "#ModelAndClassify", model.getResource(onturl + "#WorkflowTemplate"));
			tpl.setPropertyValue(prop, model.createTypedLiteral(2));
			tdbstore.commit();
			tdbstore.end();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			//tdbstore.end();
		}
	}

}
