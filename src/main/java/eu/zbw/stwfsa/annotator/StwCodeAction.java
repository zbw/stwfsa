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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import monq.jfa.AbstractFaAction;
import monq.jfa.CallbackException;
import monq.jfa.DfaRun;

/**
 * Action performed for matches against a STW concept.
 * 
 * Concept ID and offsets are stored in the **clientData** map.
 * 
 * @author Toepfer Martin
 *
 */
public class StwCodeAction extends AbstractFaAction {

  private static final String TRIM_FIX = "[\\s-,.]";

  private static final Pattern PAT_FIX = Pattern
          .compile("^" + TRIM_FIX + "*(.*?)" + TRIM_FIX + "*$");

  private String code;

  private boolean verbose;

  public StwCodeAction() {
    this("???", true);
  }

  public StwCodeAction(String code) {
    this(code, true);
  }

  public StwCodeAction(String code, boolean verbose) {
    this.code = code;
    this.verbose = verbose;
  }

  @Override
  public void invoke(StringBuilder yytext, int start, DfaRun runner) throws CallbackException {
    Map mapClientData = (Map) runner.clientData;
    String cid = this.code;
    int end = yytext.length();
    int startTotal = ((int) mapClientData.get(ClientDataFactory.LAST_MATCH_OFFSET)) + start;
    int endCurrent = ((int) mapClientData.get(ClientDataFactory.LAST_MATCH_OFFSET)) + end;
    // extract text
    String substring = yytext.substring(start, end);
    // trim text match
    Matcher wsPat = PAT_FIX.matcher(substring);
    wsPat.find();
    int trimmedStart = start + wsPat.start(1);
    int trimmedEnd = trimmedStart + wsPat.group(1).length();
    String trimmedMatch = yytext.substring(trimmedStart, trimmedEnd);
    if (verbose) {
      System.out.printf("** %s%n** %s%n", substring.replace(" ", "#"),
              trimmedMatch.replace(" ", "#"));
    }
    // legacy code (simply record concept id):
    Set<String> cids = (Set<String>) mapClientData.get(ClientDataFactory.CONCEPT_IDS);
    cids.add(cid);
    // new code:
    Map<String, StwAnnotation> conceptMatches = (TreeMap) mapClientData
            .get(ClientDataFactory.CONCEPT_MATCHES);
    // FIXME start and yytext have different meaning than used here !
    // conceptMatches.put(cid, new Object[] { cid, trimmedMatch, startTotal, endCurrent });
    // conceptMatches.put(cid, new Object[] { cid, trimmedMatch, trimmedStart, trimmedEnd });
    conceptMatches.put(cid, new StwAnnotation(cid, trimmedMatch, trimmedStart, trimmedEnd));
    // debug:
    if (verbose) {
      System.out.println();
      System.out.printf(">> code = %s%n", cid);
      System.out.printf(">> %5d%n", trimmedStart);
      System.out.printf(">>         %80s%n", yytext);
      System.out.printf(">> match = %80s%n", trimmedMatch);
    }
    mapClientData.put(ClientDataFactory.LAST_MATCH_OFFSET, endCurrent);
  }

  @Override
  public String toString() {
    return super.toString() + " " + String.format("[%s]", this.code);
  }
}
