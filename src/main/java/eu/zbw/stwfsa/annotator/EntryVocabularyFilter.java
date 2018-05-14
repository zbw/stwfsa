/**
* zaptain-stwfsa | Dictionary matching tool for the STW Thesaurus for Economics
* Copyright (C) 2017, 2018  Martin Toepfer | ZBW -- Leibniz Information Centre for Economics
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static methods to extend and restrict prefLabels and altLabels of thesaurus
 * descriptors for dictionary matching.
 * 
 * @author Toepfer Martin
 *
 */
public class EntryVocabularyFilter {

  public static Map<String, String> extendByBracesInside(Map<String, String> voc) {
    Map<String, String> vocCopy = new TreeMap<>(voc);
    Pattern pat = Pattern.compile("\\((.{3,}?)\\)", Pattern.CASE_INSENSITIVE);
    for (Entry<String, String> entry : new TreeMap<>(voc).entrySet()) {
      String key = entry.getKey();
      Matcher matcher = pat.matcher(key);
      if (matcher.find()) {
        String group = matcher.group(1);
        vocCopy.put(group, entry.getValue());
      }
    }
    return vocCopy;
  }

  /**
   * the extracted entries should be recognized later in a CASE-SENSITIVE way !
   * 
   * @param voc
   * @return
   */
  public static Map<String, String> extractByBracesPrefixUpper(Map<String, String> voc) {
    Map<String, String> extracted = new TreeMap<>();
    Pattern pat = Pattern.compile("^([A-Z-]{2,})\\s*\\((.{3,}?)\\)");
    for (Entry<String, String> entry : new TreeMap<>(voc).entrySet()) {
      String key = entry.getKey();
      Matcher matcher = pat.matcher(key);
      if (matcher.find()) {
        String group = matcher.group(1);
        extracted.put(group, entry.getValue());
      }
    }
    return extracted;
  }

  /**
   * Print differences between original vocabulary entries and entries modified by a given function
   * to system out.
   * 
   * @param voc
   * @param x
   *          function that modifies a vocabulary.
   */
  public static void explain(Map<String, String> voc,
          Function<Map<String, String>, Map<String, String>> x) {
    Map<String, String> vocCopy = x.apply(voc);
    Set<Entry<String, String>> entriesCopy = vocCopy.entrySet();
    entriesCopy.removeAll(voc.entrySet());
    for (Entry<String, String> entry : entriesCopy) {
      System.out.println("new: " + entry.toString());
    }
  }

}
