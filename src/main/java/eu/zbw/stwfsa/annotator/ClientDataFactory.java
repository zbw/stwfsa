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
package eu.zbw.stwfsa.annotator;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Provide list of constants for access to client data of automaton.
 * 
 * Initialize a client data map.
 * 
 * @author Toepfer Martin
 *
 */
public class ClientDataFactory {

  public static final String LAST_MATCH_OFFSET = "_offset_";

  public static final String CONCEPT_IDS = "cids";

  public static final String CONCEPT_MATCHES = "ConceptMatches";

  public static Map produceNewClientData() {
    Map mapClientData = new HashMap();
    mapClientData.put(CONCEPT_IDS, new TreeSet<String>());
    mapClientData.put(CONCEPT_MATCHES, ClientDataFactory.createFieldConceptMatches());
    mapClientData.put(LAST_MATCH_OFFSET, 0);
    return mapClientData;
  }

  public static TreeMap<String, StwAnnotation> createFieldConceptMatches() {
    return new TreeMap<>();
  }
}
