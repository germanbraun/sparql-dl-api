// Copyright (c) 2018. This source code is available under the terms of the GNU Lesser General Public License (LGPL)
// Author: GILIA <german.braun@fi.uncoma.edu.ar>
// Universidad Nacional del Comahue. Argentina.

// Run from linux console 
// java -cp de-derivo-sparqldlapi-3.0.0.jar:'lib/*':. de.derivo.sparqldlapi.examples.Sparql_dl_crowd

package de.derivo.sparqldlapi.examples;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import java.io.BufferedWriter;

import org.semanticweb.owlapi.apibinding.OWLManager;
//import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
//import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

//import java.io.InputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.jdom.output.XMLOutputter;

//import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.ReasonerFactory;


import org.semanticweb.owlapi.model.OWLClassExpression;
import uk.ac.manchester.cs.owl.owlapi.OWLClassExpressionImpl;



/**
 * This basic model load an ontology, initialise the SPARQL-DL query engine
 * as well as to execute simple queries in order to extract an ontology from a OWL 2 document.
 * This module takes an ontology document (owl) and uses the built-in Hermit Reasoner as reasoning system.
 * In case you use any other reasoning engine make sure you have the respective jars within your
 * classpath (note that you have to provide the resp. ReasonerFactory in this case).
 *
 * @author Germán Braun
 * @author Christian Gimenez
 */
public class Sparql_dl_crowd
{
	private static QueryEngine engine;

	/**
	 * @param ontologyDocument
     * @throws java.io.IOException
	 */
	public static void main(String[] ontologyDocument) throws IOException 
	{
		try {
                    
                // Create an ontology manager
	        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

                // Load ontology from Ontology File (owl 2 document)
                File in = new File("/var/www/html/wicom-qdod/run/crowd.owl");
                File out = new File("/var/www/html/wicom-qdod/run/crowdsparqldl.json");
                out.createNewFile();

                OWLOntology ont = manager.loadOntologyFromOntologyDocument(in);                   
                       
                // Create an instance of an OWL API reasoner
                // Hermit!
                ReasonerFactory factory = new ReasonerFactory();
                OWLReasoner reasoner = factory.createReasoner(ont);
  
                // Optionally let the reasoner compute the most relevant inferences in advance
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.DISJOINT_CLASSES, 
                         InferenceType.OBJECT_PROPERTY_HIERARCHY, InferenceType.CLASS_ASSERTIONS,InferenceType.OBJECT_PROPERTY_ASSERTIONS);

                
                // Create an instance of the SPARQL-DL query engine
		engine = QueryEngine.create(manager, reasoner, true);

                // Queries to extract an Ontology from an Ontology document OWL 2
                
                        // Get all Classes from crowd ontology
                        processQuery(
				"SELECT ?x WHERE {\n" +
					"Class(?x)" +
				"}"
			);
                        
                        // Get all ObjectProperties from crowd ontology
                        processQuery(
				"SELECT ?x WHERE {\n" +
					"ObjectProperty(?x)" +
				"}"
			);
                        
                        // Get Domain for each ObjectProperty
			processQuery(
                                "PREFIX crowd: <http://localhost/kb1#>\n" + 
				"SELECT DISTINCT ?op ?domain WHERE {\n" +
                                        "ObjectProperty(?op), \n" +
					"Domain(?op,?domain)" +
				"}"
			);
                        
                        // Get Range for each ObjectProperty
			processQuery(
                                "PREFIX crowd: <http://localhost/kb1#>\n" + 
				"SELECT DISTINCT ?op ?range WHERE {\n" +
                                        "ObjectProperty(?op), \n" +
					"Range(?op,?range)" +
				"}"
			);
                        
                        // Get Direct and Strict SubClasses for each Class
			processQuery(
                                "PREFIX crowd: <http://localhost/kb1#>\n" + 
				"SELECT DISTINCT ?strictsub ?class WHERE {\n" +
                                        "DirectSubClassOf(?strictsub,?class), \n" +
                                        "StrictSubClassOf(?strictsub,?class)" +
				"}"
			);
                        
                        // Get Direct SubClasses for each Class
			processQuery(
                                "PREFIX crowd: <http://localhost/kb1#>\n" + 
				"SELECT DISTINCT ?directsub ?class WHERE {\n" +
                                        "DirectSubClassOf(?directsub,?class)" +
				"}"
			);
                        
                        // Get Direct and Strict ObjectProperties for each ObjectProperty
			processQuery(
                                "PREFIX crowd: <http://localhost/kb1#>\n" + 
				"SELECT DISTINCT ?subop ?op WHERE {\n" +
                                        "DirectSubPropertyOf(?subop,?op), \n" +
                                        "StrictSubPropertyOf(?subop,?op)" +
				"}"
			);
                        
                        // Get Direct SubProperties for each SubProperty
			processQuery(
                                "PREFIX crowd: <http://localhost/kb1#>\n" + 
				"SELECT DISTINCT ?subop ?op WHERE {\n" +
                                        "DirectSubPropertyOf(?subop,?op)" +
				"}"
			);
                        
                        // Get Equivalent Classes
			processQuery(
                                "PREFIX crowd: <http://localhost/kb1#>\n" + 
				"SELECT DISTINCT ?class1 ?class2 WHERE {\n" +
                                        "EquivalentClass(?class1,?class2)" +
				"}"
			);
                        
                        // Get Disjoint Classes
			processQuery(
                                "PREFIX crowd: <http://localhost/kb1#>\n" + 
				"SELECT DISTINCT ?class1 ?class2 WHERE {\n" +
                                        "Class(?class1), \n" +
                                        "Class(?class2), \n" +
                                        "DisjointWith(?class1,?class2)" +
				"}"
			);
                        
                        // Get Equivalent ObjectProperties
			processQuery(
                                "PREFIX crowd: <http://localhost/kb1#>\n" + 
				"SELECT DISTINCT ?op1 ?op2 WHERE {\n" +
                                        "EquivalentProperty(?op1,?op2)" +
				"}"
			);
                        
                        // Get Disjoint ObjectProperties
			processQuery(
                                "PREFIX crowd: <http://localhost/kb1#>\n" + 
				"SELECT DISTINCT ?op1 ?op2 WHERE {\n" +
                                        "ObjectProperty(?op1), \n" +
                                        "ObjectProperty(?op2), \n" +
                                        "DisjointWith(?op1,?op2)" +
				"}"
			);
                        
                        // Get all DataProperties
			processQuery(
                                "PREFIX crowd: <http://localhost/kb1#>\n" + 
				"SELECT DISTINCT ?dp WHERE {\n" +
                                        "DataProperty(?dp)" +
				"}"
			);
                        
                        // Get Domain for each DataProperty
			processQuery(
                                "PREFIX crowd: <http://localhost/kb1#>\n" + 
				"SELECT DISTINCT ?dp ?domain WHERE {\n" +
                                        "DataProperty(?dp), \n" +
					"Domain(?dp,?domain)" +
				"}"
			);
                        
                        // Get Range for each DataProperty
			processQuery(
                                "PREFIX crowd: <http://localhost/kb1#>\n" + 
				"SELECT DISTINCT ?dp ?range WHERE {\n" +
                                        "DataProperty(?dp), \n" +
					"Range(?dp,?range)" +
				"}"
			);
                        
                        // These queries apply only for annotations generated in crowd
                        
                        // Annotations for Graphical Ontology
			processQuery(
				"PREFIX crowd: <http://localhost/kb1#>\n" +
                                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
				"SELECT ?class ?id ?x ?y WHERE {\n" +
                                    "Class(?class), \n" +
				    "Annotation(?class, crowd:ot_name_ann, ?id), \n" +
                                    "Annotation(?class, crowd:X, ?x), \n" +
                                    "Annotation(?class, crowd:Y, ?y)" +
				"}"	
			);
                        
                        processQuery(
				"PREFIX crowd: <http://localhost/kb1#>\n" +
                                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
				"SELECT ?op ?id ?x ?y WHERE {\n" +
                                    "ObjectProperty(?op), \n" +
				    "Annotation(?op, crowd:rel_name_ann, ?id), \n" +
                                    "Annotation(?op, crowd:X, ?x), \n" +
                                    "Annotation(?op, crowd:Y, ?y)" +
				"}"	
			);
                        
                           


     

        }
        catch(UnsupportedOperationException exception) {
            System.out.println("Unsupported reasoner operation.");
        }
        catch(OWLOntologyCreationException e) {
            System.out.println("Could not load the ontology: " + e.getMessage());
        }
	}
	
	public static void processQuery(String q) throws IOException
	{
		try {
			long startTime = System.currentTimeMillis();
			
			// Create a query object from it's string representation
			Query query = Query.create(q);
			
			System.out.println("Excecute the query:");
			System.out.println(q);
			System.out.println("-------------------------------------------------");
			
			// Execute the query and generate the result set
			QueryResult result = engine.execute(query);

			// print as XML
			try {
				XMLOutputter outxml = new XMLOutputter();
				outxml.output(result.toXML(), System.out);
			} 
			catch(IOException e) {
				// ok, this should not happen
			}
			
			System.out.println("-------------------------------------------------");
			
			// print as JSON
			System.out.print(result.toJSON());
                        
                        try (
                            BufferedWriter writer = new BufferedWriter(new FileWriter("/var/www/html/wicom-qdod/run/crowdsparqldl.json",true)))
                        {
                                writer.append("\n");
                                writer.write(result.toJSON());
                        }
                        catch(IOException e) {
				// ok, this should not happen
			}
                        
                    
			
			System.out.println("-------------------------------------------------");
			System.out.println("Size of result set: " + result.size());
			System.out.println("Finished in " + (System.currentTimeMillis() - startTime) / 1000.0 + "s\n");
		}
        catch(QueryParserException e) {
        	System.out.println("Query parser error: " + e);
        }
        catch(QueryEngineException e) {
        	System.out.println("Query engine error: " + e);
        }
	}
}
