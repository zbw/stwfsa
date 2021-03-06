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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.zbw.stwfsa.annotator.StwAnnotation;
import eu.zbw.stwfsa.annotator.StwAnnotator;
import eu.zbw.stwfsa.annotator.StwAutomataFactory;
import eu.zbw.stwfsa.kb.StwThesaurus;

public class TestStwAnnotator {

  private static StwThesaurus stw;

  private static StwAnnotator annotator;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    String sPth = System.getenv("STW_PTH");
    if (sPth == null) {
      Assert.fail("STW_PTH environment variable not specified");
    }
    Path stwPth = Paths.get(sPth);
    stw = new StwThesaurus(stwPth);
    System.out.println("run tests with STW: " + stw.getVersion());
    annotator = new StwAnnotator(stw, StwAutomataFactory.STRATEGY_DEFAULT);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    if (stw != null) {
      stw.close();
    }
  }

  /**
   * A basic test, just check that a few concept matches are found.
   * 
   * @throws IOException
   */
  @Test
  public void testStwAnnotator() throws IOException {
    String content = "Fishing in the North Sea : Trade Agreements between Finland, Sweden, and Russia";
    List<StwAnnotation> cids = annotator.process(content);
    Set<String> stringsToMatch = new TreeSet<>(Arrays.asList("North Sea", "Finland", "Russia"));
    for (Iterator iterator = cids.iterator(); iterator.hasNext();) {
      StwAnnotation stwAnnotation = (StwAnnotation) iterator.next();
      // System.out.println(stwAnnotation);
      stringsToMatch.remove(stwAnnotation.matchingText);
    }
    Assert.assertEquals("Some expected matches were not found.", Collections.EMPTY_SET,
            stringsToMatch);
  }

  /**
   * A basic test, just check more concept matches...
   * 
   * @throws IOException
   */
  @Test
  public void testStwAnnotations2() throws IOException {
    String content = "German beer price increases caused by Football World Cup";
    List<StwAnnotation> cids = annotator.process(content);
    Set<String> expectedMatches = new TreeSet<>(
            Arrays.asList("German", "beer", "price", "Football", "World Cup"));
    for (Iterator iterator = cids.iterator(); iterator.hasNext();) {
      StwAnnotation stwAnnotation = (StwAnnotation) iterator.next();
      // System.out.println(stwAnnotation);
      expectedMatches.remove(stwAnnotation.matchingText);
    }
    Assert.assertEquals("Some expected matches were not found.", Collections.EMPTY_SET,
            expectedMatches);
  }

  /**
   * A basic test, just to check some offsets...
   * 
   * @throws IOException
   */
  @Test
  public void testStwAnnotatorOffsets() throws IOException {
    String content = "German beer price increases caused by Football World Cup";
    List<StwAnnotation> cids = annotator.process(content);
    Assert.assertTrue("assert that some annotations are found...", cids.size() > 2);
    for (Iterator iterator = cids.iterator(); iterator.hasNext();) {
      StwAnnotation anno = (StwAnnotation) iterator.next();
      String phraseExtracted = content.substring(anno.begin, anno.end);
      String phraseAnno = anno.matchingText;
      String message = String.format("%s (from offsets %d-%d) must equal %s (from action)",
              phraseExtracted, anno.begin, anno.end, phraseAnno);
      Assert.assertTrue(message, phraseExtracted.equals(phraseAnno));
    }
  }

}
