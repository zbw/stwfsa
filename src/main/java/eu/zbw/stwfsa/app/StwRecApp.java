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
package eu.zbw.stwfsa.app;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.zbw.stwfsa.annotator.ClientDataFactory;
import eu.zbw.stwfsa.annotator.StwAnnotation;
import eu.zbw.stwfsa.annotator.StwAutomataFactory;
import eu.zbw.stwfsa.kb.StwThesaurus;
import eu.zbw.stwfsa.util.IoUtil;
import eu.zbw.stwfsa.util.IoUtil.OutputRow;
import eu.zbw.stwfsa.util.IoUtil.TitleKwsRowRaw;
import monq.clifj.Commandline;
import monq.clifj.CommandlineException;
import monq.clifj.Option;
import monq.jfa.CharSequenceCharSource;
import monq.jfa.CharSource;
import monq.jfa.Dfa;
import monq.jfa.DfaRun;
import monq.jfa.Nfa;

/**
 * see README.md for usage example.
 * 
 * @author Toepfer Martin
 *
 */
public class StwRecApp {

  public static final String VERSION = "0.3";

  /**
   * see README.md for usage example.
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] argz) throws Exception {
    String prog = System.getProperty("argv0", "StwRecApp");
    Commandline cmd = new Commandline(prog, "Match documents against STW thesaurus", "",
            "on windows, use batch script instead of cygwin", 0, Integer.MAX_VALUE);

    cmd.addOption(new Option("-version", "", "print version info and exit immediately", 0, 0));

    cmd.addOption(new Option("-voc_strategy", StwAutomataFactory.STRATEGY_DEFAULT,
            "strategy for dictionary building, see class StwAutomataFactory for available options",
            0, 1));
    cmd.addOption(new Option("-in", "path_of_input", "some *.csv file", 1, 1));
    cmd.addOption(new Option("-out", "output_file", "path_of_output", 1, 1));
    cmd.addOption(new Option("-info", "", "print offsets and matching text", 0, 0));
    cmd.addOption(new Option("-compressed", "", "use", 0, 0));

    // pre-check, handle version arg:
    for (int i = 0; i < argz.length; i++) {
      String string = argz[i];
      if (string.equals("-version")) {
        System.out.println("version: " + VERSION);
        System.exit(0);
      }
    }

    try {
      cmd.parse(argz);
    } catch (CommandlineException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }

    String[] args = cmd.getStringValues("--");
    if (args.length % 2 != 0) {
      System.err.println("illegal: uneven number of arguments");
      System.exit(1);
    }

    if (!cmd.available("-in")) {
      System.err.println("unavailable parameter -in");
      System.exit(1);
    }
    if (!cmd.available("-out")) {
      System.err.println("unavailable parameter -out");
      System.exit(1);
    }
    String vocStrategy = StwAutomataFactory.STRATEGY_DEFAULT;
    if (cmd.available("-voc_strategy")) {
      vocStrategy = cmd.getStringValue("-voc_strategy");
    }

    Path stwPth = StwThesaurus.getStwLocByEnv();
    if (!(Files.exists(stwPth) && Files.isRegularFile(stwPth))) {
      System.err.println("illegal STW pth! please check correctness of env '"
              + StwThesaurus.SYSKEY_STW_DIR + "'");
      System.exit(1);
    }
    Nfa nfa = null;
    try (StwThesaurus stw = new StwThesaurus(stwPth);) {
      System.out.printf("STW-VERSION = %s%n", stw.getVersion());
      nfa = StwAutomataFactory.createNfa(stw, vocStrategy);
    }
    Dfa dfa = nfa.compile(DfaRun.UNMATCHED_COPY);

    String sFin = cmd.getStringValue("-in");
    Path pthIn = Paths.get(sFin);

    String sFout = cmd.getStringValue("-out");
    Path outPath = Paths.get(sFout);

    System.out.printf("<%s>%n  < %s%n  > %s%n", StwRecApp.class.getSimpleName(), pthIn.toString(),
            outPath.toString());
    run(dfa, outPath, pthIn, cmd.available("-info"), vocStrategy, cmd.available("-compressed"));

    System.out.println("done.");
  }

  private static void run(Dfa dfa, Path outPath, Path datasetPath, boolean extendedInfo,
          String vocStrategy, boolean compressed) throws IOException {
    List<TitleKwsRowRaw> rows = IoUtil.readTitleKwsCsvJoint(datasetPath);
    List<OutputRow> datao = new ArrayList<OutputRow>();
    List<Object[]> dataoX = new ArrayList<>();
    for (TitleKwsRowRaw row : rows) {
      // prepare run:
      DfaRun r = StwAutomataFactory.createDfaRun(dfa);
      // FIXME expressions require char at beginning to match...
      String rInput = " " + row.content + " ";
      rInput = StwAutomataFactory.preprocessContent(rInput, vocStrategy);
      CharSource in = new CharSequenceCharSource(rInput); // require WS at end
      r.setIn(in);
      // run:
      r.filter();
      // handle results:
      // - plain style:
      Set<String> cids = (Set<String>) ((Map) r.clientData).get(ClientDataFactory.CONCEPT_IDS);
      OutputRow rowOut = new OutputRow();
      rowOut.documentId = row.documentId;
      rowOut.cids = cids.toArray(new String[0]);
      datao.add(rowOut);
      // - extended info style:
      Map<String, StwAnnotation> matchesMap = (Map) ((Map) r.clientData)
              .get(ClientDataFactory.CONCEPT_MATCHES);
      for (StwAnnotation x : matchesMap.values()) {
        dataoX.add(new Object[] { row.documentId, x });
      }
    }
    if (extendedInfo) {
      try (OutputStream fos = Files.newOutputStream(outPath);) {
        try (BufferedWriter wr = new BufferedWriter(
                new OutputStreamWriter(fos, Charset.forName("UTF-8")));) {
          for (Iterator iterator = dataoX.iterator(); iterator.hasNext();) {
            Object[] object = (Object[]) iterator.next();
            String docid = (String) object[0];
            StwAnnotation anno = (StwAnnotation) object[1];
            wr.write(String.format("%s\t%s\t%d\t%d\t%s%n", docid, anno.cid, anno.begin, anno.end,
                    anno.matchingText));
          }
        }
      }
    } else {
      try (OutputStream fos = Files.newOutputStream(outPath);) {
        IoUtil.writePredictions(fos, datao.iterator(), compressed);
      }
    }
  }

}
