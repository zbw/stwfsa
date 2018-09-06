/**
* zaptain-stwfsa | Dictionary matching tool for the STW Thesaurus for Economics
* Copyright (C) 2017-2018  Martin Toepfer | ZBW -- Leibniz Information Centre for Economics
* 
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package eu.zbw.stwfsa.kb;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.OWL;

public class StwThesaurus implements Closeable {

  public static final String SYSKEY_STW_DIR = "STW_DIR";

  public static final String FN_STW_NT = "stw.nt";

  public static Path getStwLocByEnv() {
    String stwdir = System.getenv(SYSKEY_STW_DIR);
    return Paths.get(stwdir, FN_STW_NT);
  }

  /**
   * Just for debugging:
   *
   * - load STW, show some term -> concept extractions
   *
   * @param args
   */
  public static void main(String[] args) {
    Path stwLoc = getStwLocByEnv();
    System.out.printf("stw @ %s", stwLoc.toString());
    try (StwThesaurus stw = new StwThesaurus(stwLoc)) {
      Map<String, String> voc = stw.getEntryVocabulary("en");
      for (Entry<String, String> x : voc.entrySet()) {
        System.out.printf("> %s -> %s%n", x.getKey(), x.getValue());
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private Model stw;

  public StwThesaurus(Path stwPth) {
    this.stw = createDatasetObj(stwPth);
  }

  public Literal getVersion() {
    Resource stwres = stw.getResource("http://zbw.eu/stw");
    Property vp = stw.getProperty(OWL.versionInfo.toString());
    return stw.getProperty(stwres, vp).getLiteral();
  }

  private Model createDatasetObj(Path stwPth) {
    return RDFDataMgr.loadModel(stwPth.toString());
  }

  public Model getDataset() {
    return stw;
  }

  /**
   * be careful with terms in braces, for instance:
   * 
   * 19077-5: CEEC (Central and Eastern European Countries)
   * 
   * which you have to handle separately.
   * 
   * @param language
   *          either "*" (don't care), or code like "en".
   * @return all literal prefLabels and altLabels of descriptors
   */
  public Map<String, String> getEntryVocabulary(String language) {
    Map<String, String> voc = new HashMap<>();
    String queryString = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
            + "PREFIX zbwext: <http://zbw.eu/namespaces/zbw-extensions/>\n"
            + "SELECT ?cidres ?label WHERE " + "{ " + "?cidres a zbwext:Descriptor ."
            + "{?cidres skos:prefLabel ?label .}" //
            + " UNION " //
            + "{?cidres skos:altLabel ?label .}}";
    // + "FILTER not EXISTS { ?cidres OWL.deprecated true . }"; ??
    // note on deprecated terms: regarding the stw this is not an issue
    // deprecated concepts use rdfs:label instead of skos:(pref|alt)Label
    Query q = QueryFactory.create(queryString);
    ResultSet results = QueryExecutionFactory.create(q, stw).execSelect();
    while (results.hasNext()) {
      QuerySolution soln = results.nextSolution();
      RDFNode rConcept = soln.get("cidres");
      String rStr = rConcept.toString();
      Literal litLabel = soln.getLiteral("label");
      String lexicalForm = litLabel.getLexicalForm();
      lexicalForm = StringEscapeUtils.unescapeXml(lexicalForm);
      if (language == null || "*".equals(language) || language.equals(litLabel.getLanguage())) {
        voc.put(lexicalForm, rStr);
      }
    }
    return voc;
  }

  public SortedSet<String> getCodes(String cid) {
    SortedSet<String> codes = new TreeSet<>();
    // TODO see /zbw-sandbox/src/main/resources/query/map_cid_to_thsys.sparql
    ParameterizedSparqlString sparql = new ParameterizedSparqlString();
    try {
      sparql.setCommandText(loadQuery("map_cid_to_thsys.sparql"));
      sparql.setIri("qid", "http://zbw.eu/stw/descriptor/" + cid);
      Query query = sparql.asQuery();
      ResultSet results = QueryExecutionFactory.create(query, stw).execSelect();
      while (results.hasNext()) {
        QuerySolution soln = results.nextSolution();
        System.out.println(soln);
        codes.add(soln.get("code").toString());
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return codes;
  }

  /**
   * 
   * @param fn
   *          filename of the sparql query, which is located in the classpath under queries/*,
   *          typically used for parameterized queries, see:
   *          {@link https://jena.apache.org/documentation/query/parameterized-sparql-strings.html}
   * @return content of query fn
   * @throws IOException
   */
  public String loadQuery(String fn) throws IOException {
    URL resource = getClass().getClassLoader().getResource("queries/" + fn);
    return IOUtils.toString(resource, Charset.forName("UTF-8"));
  }

  @Override
  public void close() throws IOException {
    stw.close();
  }
}
