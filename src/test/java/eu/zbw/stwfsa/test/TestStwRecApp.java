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
package eu.zbw.stwfsa.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.zbw.stwfsa.annotator.ClientDataFactory;
import eu.zbw.stwfsa.annotator.StwAnnotation;
import eu.zbw.stwfsa.annotator.StwAutomataFactory;
import eu.zbw.stwfsa.kb.StwThesaurus;
import monq.jfa.CharSequenceCharSource;
import monq.jfa.CharSource;
import monq.jfa.CompileDfaException;
import monq.jfa.Dfa;
import monq.jfa.DfaRun;
import monq.jfa.Nfa;

/**
 * 
 * @author Toepfer Martin
 *
 */
public class TestStwRecApp {

  private static StwThesaurus stw;

  private static Nfa stwRec;

  private static Dfa dfa;

  private DfaRun run;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    String sPth = System.getenv("STW_PTH");
    if (sPth == null) {
      Assert.fail("STW_PTH environment variable not specified");
    }
    Path stwPth = Paths.get(sPth);
    stw = new StwThesaurus(stwPth);
    System.out.println("run tests with STW: " + stw.getVersion());
    stwRec = StwAutomataFactory.createNfa(stw, StwAutomataFactory.STRATEGY_DEFAULT);
    dfa = stwRec.compile(DfaRun.UNMATCHED_COPY);
    // Map<String, String> voc = stw.getEntryVocabulary("en");
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    if (stw != null) {
      stw.close();
    }
  }

  @Before
  public void setUp() throws Exception {
    // fail("Not yet implemented");
    System.out.print("reset stwRec run... ");
    run = StwAutomataFactory.createDfaRun(dfa);
    System.out.println("done.");
  }

  @After
  public void tearDown() throws Exception {
  }

  public void processInput(String string) throws IOException {
    CharSource in = new CharSequenceCharSource(string);
    run.setIn(in);
    run.filter();
  }

  public void assertRecognitionEquals(String input, String[] conceptIds) throws IOException {
    assertRecognition(input, conceptIds, true);
  }

  public void assertRecognition(String input, String[] conceptIds, boolean assertEquals)
          throws IOException {
    processInput(" " + input);
    // FIXME FIX HACK processInput(input);
    Set<String> cidsPredicted = (Set<String>) ((Map) run.clientData)
            .get(ClientDataFactory.CONCEPT_IDS);
    for (int i = 0; i < conceptIds.length; i++) {
      String cidExpected = conceptIds[i];
      Assert.assertTrue(
              String.format("failed to recognize concept: '%s' in '%s'", cidExpected, input),
              cidsPredicted.contains(cidExpected));
    }
    System.out.printf("input: %s%n", input);
    System.out.println("recognized: " + cidsPredicted);
    Map<String, StwAnnotation> matchesMap = (Map) ((Map) run.clientData)
            .get(ClientDataFactory.CONCEPT_MATCHES);
    for (StwAnnotation x : matchesMap.values()) {
      System.out.println("matching data: " + x.toString());
    }
    if (assertEquals) {
      Assert.assertTrue(cidsPredicted.size() == conceptIds.length);
    }
  }

  // @Test
  // public void testVocabularyBraceEntries() {
  // // SALT | SALT (Strategic Arms Limitation Talks) | http://zbw.eu/stw/descriptor/16445-3
  // // salt | x | x
  // // Set<Entry<String, String>> voc = stw.getEntryVocabulary("en").entrySet();
  // // for (Entry<String, String> entry : voc) {
  // // System.out.println(entry);
  // // }
  // System.out.println("BRACES - Inside");
  // EntryVocabularyFilter.explain(stw.getEntryVocabulary("en"),
  // EntryVocabularyFilter::extendByBracesInside);
  // System.out.println("BRACES - PrefixUpper");
  // EntryVocabularyFilter.explain(stw.getEntryVocabulary("en"),
  // EntryVocabularyFilter::extractByBracesPrefixUpper);
  //
  // }

  @Test
  public void testBasic() throws CompileDfaException, IOException {
    // European_foreign_policy | http://zbw.eu/stw/descriptor/29726-2
    System.out.println("> testBasic");
    String input = "Germany and Italy ";
    String[] conceptIds = { "17039-2", "18012-3" };
    assertRecognitionEquals(input, conceptIds);
  }

  @Test
  public void testCoveringMatches() throws IOException {
    // European_foreign_policy | http://zbw.eu/stw/descriptor/29726-2
    // _________foreign_policy | http://zbw.eu/stw/descriptor/16489-4
    System.out.println("> testCoveringMatches");
    String input = "European foreign policy ";
    String[] conceptIds = { "29726-2" };
    assertRecognitionEquals(input, conceptIds);
  }

  @Test
  public void testCoveringMatches2() throws IOException {
    // . _________foreign__________policy | http://zbw.eu/stw/descriptor/16489-4
    // . European_foreign__________policy | http://zbw.eu/stw/descriptor/29726-2
    // . European_foreign_economic_policy | http://zbw.eu/stw/descriptor/29617-0
    // . _________foreign_economic_policy | http://zbw.eu/stw/descriptor/10682-5
    System.out.println("> testCoveringMatches2");
    String input = " European foreign economic policy ";
    String[] conceptIds = { "29617-0" };
    assertRecognitionEquals(input, conceptIds);
  }

  @Test
  @Ignore
  public void testGapMatches() throws IOException {
    // TODO test something like X z Y for pattern X Y
  }

  @Test
  public void testOverlappingMatches() throws IOException {
    // . __________ intelligence agency | http://zbw.eu/stw/descriptor/16332-2
    // . collective intelligence ______ | http://zbw.eu/stw/descriptor/26376-4
    System.out.println("> testOverlappingMatches");
    String input = " collective intelligence agency ";
    String[] conceptIds = { "26376-4" };
    assertRecognitionEquals(input, conceptIds);
  }

  @Test
  public void testAbbrevationFdiAkronym() throws IOException {
    System.out.println("> testAbbrevationMatches [FDI]");
    String input = "Exports, international investment, and plant performance : evidence from a non-parametric test ## exporting # FDI # multinationals # productivity # profitability # plant heterogeneity ";
    String[] conceptIds = { "10823-6" };
    assertRecognition(input, conceptIds, false);
    // TODO test, e.g., "USA", "u.s.a.", " US ", vs. "us" ...
  }

  @Test
  public void testAbbrevationSaltAkronym() throws IOException {
    // SALT | SALT (Strategic Arms Limitation Talks) | http://zbw.eu/stw/descriptor/16445-3
    // salt | salt (chemistry, cooking) | http://zbw.eu/stw/descriptor/16445-3
    System.out.println("> testAbbrevationMatches [SALT]");
    String input = " SALT ";
    String[] conceptIds = { "16445-3" }; // alt (chemistry, cooking)
    assertRecognitionEquals(input, conceptIds);
  }

  @Test
  public void testAbbrevationSaltCooking() throws IOException {
    // SALT | SALT (Strategic Arms Limitation Talks) | http://zbw.eu/stw/descriptor/16445-3
    // salt | salt (chemistry, cooking) | http://zbw.eu/stw/descriptor/16445-3
    System.out.println("> testAbbrevationMatches [salt]");
    String input = " salt ";
    String[] conceptIds = new String[] { "14205-5" }; // salt (chemistry, cooking)
    assertRecognitionEquals(input, conceptIds);
  }

  @Test
  // @Ignore
  public void testAbbrevationPeriodAkronym() throws IOException {
    // test, e.g., "USA", "u.s.a.", " US ", vs. "us" ...

    // UK | 17374-2
    String[] conceptIds = { "17374-2" };
    String input = " U.K. ";
    System.out.printf("> testAbbrevationPeriodAkronym [%s]%n", input);
    assertRecognitionEquals(input, conceptIds);

    // TODO test also:
    // USA | 17829-1
    // String input = " U.S.A. "; // USA is only a German label in STW!
    // String input = " u.s.a. ";
    // String input = "U.S.";
  }

  @Test
  public void testAbbrevationMatchesAids() throws IOException {
    // aids | ... ? %% development aid ?
    // AIDS | http://zbw.eu/stw/descriptor/18898-5
    // AIDS model | http://zbw.eu/stw/descriptor/19552-1
    // Development aid | http://zbw.eu/stw/descriptor/10555-5
    System.out.println("> test acronym disambiguation [AIDS]");
    String input = "AIDS isn't equal to AIDS model and aid [development], i.e., development aid, is also different. ";
    String[] conceptIds = new String[] { "18898-5", "19552-1", "10555-5" };
    assertRecognition(input, conceptIds, true);
    // System.err.print("?? aid [development] not found, should be found ??");
  }

  @Test
  public void testAkronymPart() throws IOException {
    // possible mismatch: SOE (State-owned enterprise) | 11628-6
    System.out.println("> testAkronym");
    String input = " Socio-Economic Panel Study (SOEP) ";
    String[] conceptIds = { "15125-5" };
    assertRecognitionEquals(input, conceptIds);
  }

  @Test
  public void testSpecialCharacter() throws IOException {
    // TODO F&amp;E transfer=http://zbw.eu/stw/descriptor/18869-5
    // R&D | 10436-6
    String input = " R&D ";
    // String input = " R & D ";
    String[] conceptIds = new String[] { "10436-6" };
    System.out.printf("> test [%s] !> %s%n", input, Arrays.deepToString(conceptIds));
    assertRecognitionEquals(input, conceptIds);
    //
    try {
      setUp();
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    input = " R & D ";
    conceptIds = new String[] { "10436-6" };
    System.out.printf("> test [%s] !> %s%n", input, Arrays.deepToString(conceptIds));
    assertRecognitionEquals(input, conceptIds);
  }

  @Test
  public void testSep() throws IOException {
    // nonparametric test | http://zbw.eu/stw/descriptor/29890-3
    String input = " non-parametric test ";
    String[] conceptIds = new String[] { "29890-3" };
    System.out.printf("> test [%s] !> %s%n", input, Arrays.deepToString(conceptIds));
    String vocStrategy = StwAutomataFactory.STRATEGY_OPTION_3;
    assertRecognitionEquals(StwAutomataFactory.preprocessContent(input, vocStrategy), conceptIds);
    // //
    // try {
    // setUp();
    // } catch (Exception e) {
    // Assert.fail(e.getMessage());
    // }
    // input = " non-parametric test ";
    // conceptIds = new String[] { "29890-3" };
    // System.out.printf("> test [%s] !> %s%n", input, Arrays.deepToString(conceptIds));
    // assertRecognitionEquals(input, conceptIds);
  }

  @Test
  @Ignore
  public void testSep2() throws IOException {
    // TODO ! development aid | XXXX
    String input = " children development - aid for disabled";
    // String[] conceptIds = new String[] { "xxx" };
    // System.out.printf("> test [%s] !> %s%n", input, Arrays.deepToString(conceptIds));
    // assertRecognitionEquals(input, conceptIds);
    Assert.fail();
  }

  @Test
  public void testSubwordMismatch() throws IOException {
    // cement | http://zbw.eu/stw/descriptor/14258-5
    String input = "  cement ";
    String[] conceptIds = new String[] { "14258-5" };
    System.out.printf("> test [%s] !> %s%n", input, Arrays.deepToString(conceptIds));
    assertRecognitionEquals(input, conceptIds);
    //
    try {
      setUp();
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    input = " enforcement ";
    conceptIds = new String[] {};
    System.out.printf("> test [%s] !> %s%n", input, Arrays.deepToString(conceptIds));
    assertRecognitionEquals(input, conceptIds);
  }

}
