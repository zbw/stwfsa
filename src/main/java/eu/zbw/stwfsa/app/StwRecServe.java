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

//import static spark.Spark.*;
import static spark.Spark.get;
import static spark.Spark.post;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.zbw.stwfsa.annotator.StwAnnotation;
import eu.zbw.stwfsa.annotator.StwAnnotator;
import eu.zbw.stwfsa.annotator.StwAutomataFactory;
import eu.zbw.stwfsa.kb.StwThesaurus;
import monq.clifj.Commandline;

/**
 * <p>
 * Please use the cmd line app rather than the server, this is just a showcase.
 * </p>
 * 
 * The server processes json arrays of {"id": ..., "content": ...} objects and returns jsons arrays
 * that contain the recognized annotations.
 * 
 * <p>
 * Example curl:<br/>
 * 
 * <code>
curl "http://localhost:4567/process-json" -H "Host: localhost:4567"-H "Accept: application/json" --compressed -H "Connection: keep-alive" -H "Upgrade-Insecure-Requests: 1" -H "Pragma: no-cache" -H "Cache-Control: no-cache" --data "
[
{"id": "1234", "content": "Air polution in Northern Germany"},
{"id": "5678", "content": "The automobile industry and tax regulation effects"}
]"
 * </code>
 * </p>
 * 
 * @author Toepfer Martin
 *
 */
public class StwRecServe {

  /**
   * by default, listening on port: 4567.
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] argz) throws Exception {
    String prog = System.getProperty("argv0", "StwRecApp");
    Commandline cmd = new Commandline(prog, "Server to match documents against STW thesaurus", "",
            "on windows, use batch script instead of cygwin", 0, Integer.MAX_VALUE);
    // port(4567);

    String stwDirPth = System.getenv("STW_DIR");
    if (stwDirPth == null) {
      throw new IllegalArgumentException("STW_PTH environment variable not specified");
    }
    Path stwPth = Paths.get(stwDirPth, "stw.nt");
    try (StwThesaurus stw = new StwThesaurus(stwPth);) {
      System.out.println("run with STW: " + stw.getVersion());

      get("/version", (req, res) -> String.format("StwRecServe Version: %s", StwRecApp.VERSION));
      post("/process-json", "application/json", (req, res) -> {
        JsonParser parser = new JsonParser();

        JsonArray records = parser.parse(req.body()).getAsJsonArray();
        for (JsonElement record : records) {
          StwAnnotator annotator = new StwAnnotator(stw, StwAutomataFactory.STRATEGY_DEFAULT);
          JsonObject reco = record.getAsJsonObject();
          String id = reco.get("id").getAsString();
          String content = reco.get("content").getAsString();
          List<StwAnnotation> annotations = annotator.process(content);

          JsonArray jsonAnnotations = new JsonArray(annotations.size());
          StreamSupport.stream(annotations.spliterator(), false).map(StwRecServe::toJson)
                  .forEach(x -> {
                    jsonAnnotations.add(x);
                  });
          reco.add("annotations", jsonAnnotations);
        }
        return records; // res;
      });
    }
  }

  public static JsonObject toJson(StwAnnotation annotation) {
    JsonObject jo = new JsonObject();
    jo.addProperty("begin", new Integer(annotation.begin));
    jo.addProperty("end", new Integer(annotation.end));
    jo.addProperty("cid", annotation.cid);
    jo.addProperty("matchingText", annotation.matchingText);
    return jo;
  }

}
