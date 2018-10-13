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
  public void invoke(StringBuilder yytext, final int start, DfaRun runner)
          throws CallbackException {
    Map mapClientData = (Map) runner.clientData;
    final int offsetYY = (int) mapClientData.get(ClientDataFactory.LAST_MATCH_OFFSET);
    // note on yytext: is cleared after a match!
    String cid = this.code;
    final int startInYY = start;
    final int endInYY = yytext.length();
    final int startTotal = offsetYY + startInYY;
    final int endTotal = offsetYY + endInYY;
    String textBefore = yytext.substring(0, startInYY);
    if (textBefore.isEmpty() || textBefore.matches(".*[^A-Z0-9a-z]$")) {
      String substring = yytext.substring(start, endInYY); // extract text
      // trim text match
      Matcher wsPat = PAT_FIX.matcher(substring);
      wsPat.find();
      final int trimmedStartYY = startInYY + wsPat.start(1);
      final int trimmedEndYY = trimmedStartYY + wsPat.group(1).length();
      String trimmedMatch = yytext.substring(trimmedStartYY, trimmedEndYY);
      if (verbose) {
        System.out.printf("** %s%n** %s%n", substring.replace(" ", "#"),
                trimmedMatch.replace(" ", "#"));
      }
      final int trimmedStartTotal = offsetYY + trimmedStartYY;
      final int trimmedEndTotal = offsetYY + trimmedEndYY;
      // legacy code (simply record concept id):
      Set<String> cids = (Set<String>) mapClientData.get(ClientDataFactory.CONCEPT_IDS);
      cids.add(cid);
      // new code:
      Map<String, StwAnnotation> conceptMatches = (TreeMap) mapClientData
              .get(ClientDataFactory.CONCEPT_MATCHES);
      StwAnnotation annotation = new StwAnnotation(cid, trimmedMatch, trimmedStartTotal,
              trimmedEndTotal);
      conceptMatches.put(cid, annotation);
      // debug:
      if (verbose) {
        System.out.println();
        System.out.printf(">> code = %s%n", cid);
        System.out.printf(">> %5d%n", trimmedStartYY);
        System.out.printf(">>         %80s%n", yytext);
        System.out.printf(">> match = %80s%n", trimmedMatch);
      }
    }
    mapClientData.put(ClientDataFactory.LAST_MATCH_OFFSET, endTotal);
  }

  @Override
  public String toString() {
    return super.toString() + " " + String.format("[%s]", this.code);
  }
}
