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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.SortedSet;

import org.apache.jena.rdf.model.Literal;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.zbw.stwfsa.kb.StwThesaurus;

/**
 * Test particular functions of the knowledge base, that is, StwThesaurus.
 * 
 * @author Toepfer Martin
 *
 */
public class TestKb {

  private static StwThesaurus stw;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    String sPth = System.getenv("STW_PTH");
    if (sPth == null) {
      Assert.fail("STW_PTH environment variable not specified");
    }
    Path stwPth = Paths.get(sPth);
    stw = new StwThesaurus(stwPth);
    Literal version = stw.getVersion();
    System.out.println("run tests with STW: " + version);
    // following assertions may refer to a specific version of the STW.
    if (!"9.04".equals(version.toString())) {
      System.err.printf(
              "warning: this version (STW %s) of the STW may not comply with the tests, see test code for details",
              version.toString());
    }
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    if (stw != null) {
      stw.close();
    }
  }

  @Test
  public void testEntryVocabulary() {
    Map<String, String> voc = stw.getEntryVocabulary("en");
    String key = "EU foreign policy";
    Assert.assertTrue(voc.containsKey(key));
  }

  @Test
  public void testCategories() {
    {
      // http://zbw.eu/stw/version/latest/descriptor/16489-4/about.de.html
      SortedSet<String> codes = stw.getCodes("16489-4"); // foreign policy
      Assert.assertTrue(codes.size() == 1);
      Assert.assertTrue(codes.contains("N.04.06"));
    }
    {
      // http://zbw.eu/stw/version/latest/descriptor/29726-2/about.en.html
      SortedSet<String> codes = stw.getCodes("29726-2"); //
      Assert.assertTrue(codes.size() == 1);
      Assert.assertTrue(codes.contains("N.04.06.02"));
    }
  }
}
