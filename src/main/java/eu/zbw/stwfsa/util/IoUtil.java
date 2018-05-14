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
package eu.zbw.stwfsa.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class IoUtil {

  public static class TitleKwsRowRaw {
    public String documentId;

    public String content;

    /**
     * Concept IDs
     */
    public String[] cids;
  }

  public static class OutputRow {
    public String documentId;

    /**
     * Concept IDs
     */
    public String[] cids;
  }

  /**
   * Parse TSV-files (tab separated cells) that contain per row:
   * <ul>
   * <li>the document id,</li>
   * <li>the short-text document content (title, maybe joined with keywords),</li>
   * </ul>
   * and optionally
   * <ul>
   * <li>concept ids, separated by semi-colon</li>
   * </ul>
   * 
   * @param datasetPath
   * @return
   */
  public static List<TitleKwsRowRaw> readTitleKwsCsvJoint(Path datasetPath) {
    File csvFile = new File(datasetPath.toString());
    if (!csvFile.exists()) {
      // log.error("File " + csvFile.getAbsolutePath() + " not found!");
      throw new RuntimeException(new FileNotFoundException(datasetPath.toString()));
    }
    if (!csvFile.isFile()) {
      // log.error("File object '" + csvFile.getAbsolutePath() + "' is not a file !");
      throw new RuntimeException();
    }
    if (!csvFile.getName().endsWith(".csv")) {
      // log.error("File '" + csvFile.getAbsolutePath() + "' has illegal format extension !" + "\n"
      // + "Expected '.csv' !");
      throw new RuntimeException();
    }
    // --
    List<TitleKwsRowRaw> docs = new ArrayList<TitleKwsRowRaw>();
    //
    Iterator<String> lineIt;
    try {
      lineIt = Files.lines(datasetPath).iterator();
    } catch (IOException e) {
      // log.error("Error while loading documents: " + e.getMessage());
      throw new RuntimeException();
    }
    while (lineIt.hasNext()) {
      String line = (String) lineIt.next();
      String[] parts = line.split("\t");
      TitleKwsRowRaw row = new TitleKwsRowRaw();
      if (parts.length >= 2) {
        row.documentId = parts[0];
        row.content = parts[1];
        if (parts.length > 2) {
          row.cids = parts[2].split(";");
        } else {
          row.cids = new String[0];
        }
        docs.add(row);
      }
    }
    return docs;
  }

  public static List<OutputRow> readPredictionsFlat(Path pthIn, boolean compressed)
          throws IOException {
    List<String> lns = Files.readAllLines(pthIn, Charset.forName("UTF-8"));
    Map<String, List<String>> mapDocCids = new TreeMap<>();
    List<String> dicOrder = new ArrayList<>();
    for (String ln : lns) {
      String[] split = ln.split("\t");
      String docid = split[0];
      String cid = split[1];
      if (split.length == 2) {
        if (!mapDocCids.containsKey(docid)) {
          dicOrder.add(docid);
          mapDocCids.put(docid, new LinkedList<>());
        }
        mapDocCids.get(docid).add(cid);
      }
    }
    List<OutputRow> data = dicOrder.stream().map(x -> {
      OutputRow or = new OutputRow();
      or.documentId = x;
      or.cids = mapDocCids.get(x).toArray(new String[0]);
      return or;
    }).collect(Collectors.toList());
    return data;
  }

  public static Map<String, List<String>> readPredictionsFlatMap(Path pthIn) throws IOException {
    List<String> lns = Files.readAllLines(pthIn, Charset.forName("UTF-8"));
    Map<String, List<String>> mapDocCids = new TreeMap<>();
    List<String> dicOrder = new ArrayList<>();
    for (String ln : lns) {
      String[] split = ln.split("\t");
      String docid = split[0];
      String cid = split[1];
      if (split.length == 2) {
        if (!mapDocCids.containsKey(docid)) {
          dicOrder.add(docid);
          mapDocCids.put(docid, new LinkedList<>());
        }
        mapDocCids.get(docid).add(cid);
      }
    }
    return mapDocCids;
  }

  public static void writePredictions(OutputStream out, Iterator<OutputRow> rows,
          boolean compressed) throws IOException {
    try (BufferedWriter wr = new BufferedWriter(
            new OutputStreamWriter(out, Charset.forName("UTF-8")));) {
      while (rows.hasNext()) {
        IoUtil.OutputRow row = (IoUtil.OutputRow) rows.next();
        if (compressed) {
          wr.write(row.documentId);
          wr.write("\t");
          wr.write(String.join("\t", row.cids));
          wr.write("\n");
        } else {
          for (String cid : row.cids) {
            wr.write(row.documentId);
            wr.write("\t");
            wr.write(cid);
            wr.write("\n");
          }
        }
      }
    }
  }

}
