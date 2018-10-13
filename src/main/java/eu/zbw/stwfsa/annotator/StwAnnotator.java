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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.zbw.stwfsa.kb.StwThesaurus;
import monq.jfa.CharSequenceCharSource;
import monq.jfa.CharSource;
import monq.jfa.CompileDfaException;
import monq.jfa.Dfa;
import monq.jfa.DfaRun;
import monq.jfa.Nfa;
import monq.jfa.ReSyntaxException;

/**
 * Extract {@link StwAnnotation}s from input strings.
 * 
 * @author Toepfer Martin
 *
 */
public class StwAnnotator {
  private String vocStrategy;

  private DfaRun run;

  public StwAnnotator(StwThesaurus stw, String vocStrategy)
          throws ReSyntaxException, CompileDfaException {
    this.vocStrategy = vocStrategy;
    Nfa nfa = StwAutomataFactory.createNfa(stw, vocStrategy);
    Dfa dfa = nfa.compile(DfaRun.UNMATCHED_COPY);
    run = StwAutomataFactory.createDfaRun(dfa);
  }

  public List<StwAnnotation> process(String content) throws IOException {
    String rInput = content + " ";
    rInput = StwAutomataFactory.preprocessContent(rInput, vocStrategy);
    // process input:
    CharSource in = new CharSequenceCharSource(rInput);
    run.clientData = ClientDataFactory.produceNewClientData(); // clear client data
    run.setIn(in);
    run.filter();
    // create return list:
    LinkedList<StwAnnotation> annotations = new LinkedList<>();

    Map<String, StwAnnotation> matchesMap = (Map) ((Map) run.clientData)
            .get(ClientDataFactory.CONCEPT_MATCHES);
    annotations.addAll(matchesMap.values());
    return annotations;
  }

}
