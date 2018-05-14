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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;

/**
 * Essential properties of the SKOS reference.
 * 
 * see: http://www.w3.org/2004/02/skos/core
 */
public class SKOS {

  private static Model m_model = ModelFactory.createDefaultModel();

  public static final String NS_SKOS = "http://www.w3.org/2004/02/skos/core#";

  public static final Property prefLabel = m_model.createProperty(NS_SKOS + "prefLabel");

  public static final Property altLabel = m_model.createProperty(NS_SKOS + "altLabel");

}
