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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.zbw.stwfsa.kb.StwThesaurus;
import monq.ie.Term2Re;
import monq.jfa.AbstractFaAction;
import monq.jfa.Dfa;
import monq.jfa.DfaRun;
import monq.jfa.Nfa;
import monq.jfa.ReSyntaxException;

/**
 * 
 * @author Toepfer Martin
 *
 */
public class StwAutomataFactory {

  private static final String NOT_WORDCHAR = "[^A-Z0-9a-z]";

  public static final String STRATEGY_0 = "";

  public static final String STRATEGY_OPTION_1 = "+extractByBracesPrefixUpper";

  public static final String STRATEGY_OPTION_2 = "+restrictMatchStart";

  public static final String STRATEGY_OPTION_3 = "+ignoreSpecial";

  public static final String STRATEGY_DEFAULT = STRATEGY_OPTION_1 + STRATEGY_OPTION_2
          + STRATEGY_OPTION_3;

  public static DfaRun createDfaRun(Dfa dfa) {
    DfaRun r = new DfaRun(dfa);
    r.clientData = ClientDataFactory.produceNewClientData();
    return r;
  }

  public static Nfa createNfa(Path stwPth) throws ReSyntaxException, IOException {
    try (StwThesaurus stw = new StwThesaurus(stwPth);) {
      return createNfa(stw, STRATEGY_OPTION_1);
    }
  }

  /**
   * 
   * @param stw
   * @return
   * @throws ReSyntaxException
   */
  public static Nfa createNfa(StwThesaurus stw, String vocStrategy) throws ReSyntaxException {
    Nfa nfa = new Nfa();

    Map<String, String> voc = stw.getEntryVocabulary("en");
    // TODO control acronym/braces expansion by parameter
    System.out.println("expand vocabulary heuristically...");
    if (vocStrategy.contains(STRATEGY_OPTION_1)) {
      voc.putAll(EntryVocabularyFilter.extractByBracesPrefixUpper(voc));
    }

    Map<String, AbstractFaAction> actions = new TreeMap<>();

    for (Entry<String, String> e : voc.entrySet()) {
      String cidRef = e.getValue();
      String cid = cidRef.substring(cidRef.lastIndexOf("/") + 1);
      if (!actions.containsKey(cid)) {
        StwCodeAction a = new StwCodeAction(cid, false);
        a.setPriority(actions.size()); // TODO fix hack
        // TODO for example, we may rerun the app with inverse priority order ?! quickfix
        actions.put(cid, a);
      }
      AbstractFaAction a = actions.get(cid);
      if (a == null) {
        throw new NullPointerException("no action for cid:" + cid);
      }
      String phrase = e.getKey(); // <- a label of the concept
      String regEx = null;
      if (vocStrategy.contains(STRATEGY_OPTION_2)) {
        if (phrase.matches(".*&.*")) {
          // like R&D
          regEx = phrase.replaceAll("[A-Z&](?=[A-Z&])", "$0 *") + NOT_WORDCHAR;
          // System.out.printf("sp&cial phrase: %s (%s)%n", regEx, phrase);
        } else if (phrase.matches("[A-Z]+")) {
          regEx = phrase.replaceAll("\\w", "$0\\\\.?") + NOT_WORDCHAR;
          // System.out.printf("UPPER phrase: %s (%s)%n", regEx, phrase);
        } else {
          regEx = Term2Re.convert(phrase);
          // System.out.printf("Term2Re phrase: %s%n", regEx);
        }
      } else {
        regEx = Term2Re.convert(phrase);
      }
      nfa.or(regEx, a);
    }
    return nfa;
  }

  public static String preprocessContent(String content, String vocStrategy) {
    if (vocStrategy.contains(StwAutomataFactory.STRATEGY_OPTION_3)) {
      StringBuffer sbu = new StringBuffer();
      Pattern pat = Pattern.compile("(\\w)-(\\w)");
      Matcher mat = pat.matcher(content);
      while (mat.find()) {
        mat.appendReplacement(sbu, "$1$2");
      }
      mat.appendTail(sbu);
      content = sbu.toString(); // .replaceAll("\\w()", "");
    }
    return content;
  }
}
