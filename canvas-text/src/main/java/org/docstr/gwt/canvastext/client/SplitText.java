/*
 * Copyright (c) 2023 Document Node Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docstr.gwt.canvastext.client;

import static elemental2.dom.DomGlobal.console;
import static org.docstr.gwt.canvastext.client.Justify.justifyLine;

import elemental2.dom.CanvasRenderingContext2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author delight.wjk@gmail.com
 */
public class SplitText {

  // Hair space character for precise justification
  private static final String SPACE = "\u200A";

  private SplitText() {
  }

  public static List<String> splitText(
      CanvasRenderingContext2D ctx, String text, boolean justify,
      double width) {
    final Map<String, Double> textMap = new HashMap<>();

    List<String> textArray = new ArrayList<>();
    if (text == null) {
      return textArray;
    }
    String[] initialTextArray = text.split("\n");

    double spaceWidth = justify ? measureText(ctx, textMap, SPACE) : 0;

    int index = 0;
    int averageSplitPoint = 0;
    for (String singleLine : initialTextArray) {
      double textWidth = measureText(ctx, textMap, singleLine);

      if (textWidth <= width) {
        textArray.add(singleLine);
        continue;
      }

      // Keep the remaining part of the line
      String tempLine = singleLine;

      int splitPoint = 0;
      double splitPointWidth = 0;
      String textToPrint = "";

      while (textWidth > width) {
        index++;
        splitPoint = Math.min(averageSplitPoint, tempLine.length());
        splitPointWidth =
            splitPoint == 0 ? 0
                : measureText(ctx, textMap, tempLine.substring(0, splitPoint));

        // if (splitPointWidth === width) Nailed
        if (splitPointWidth < width) {
          while (splitPointWidth < width && splitPoint < tempLine.length()) {
            splitPoint++;
            splitPointWidth = measureText(ctx, textMap,
                tempLine.substring(0, splitPoint));
            if (splitPoint == tempLine.length()) {
              break;
            }
          }
        } else if (splitPointWidth > width) {
          while (splitPointWidth > width) {
            splitPoint = Math.max(1, splitPoint - 1);
            splitPointWidth = measureText(ctx, textMap,
                tempLine.substring(0, splitPoint));
            if (splitPoint == 1) {
              break;
            }
          }
        }

        averageSplitPoint = (int) Math.round(averageSplitPoint * 1.0
            + (splitPoint * 1.0 - averageSplitPoint) / index);

        // Remove last character that was out of the box
        splitPoint--;

        // Ensures a new line only happens at a space, and not amidst a word
        if (splitPoint > 0) {
          int tempSplitPoint = splitPoint;
          if (!" ".equals(
              tempLine.substring(tempSplitPoint, tempSplitPoint + 1))) {
            while (tempSplitPoint >= 0 && !" ".equals(
                tempLine.substring(tempSplitPoint, tempSplitPoint + 1))) {
              tempSplitPoint--;
            }
            if (tempSplitPoint > 0) {
              splitPoint = tempSplitPoint;
            }
          }
        }

        if (splitPoint == 0) {
          splitPoint = 1;
        }

        // Finally, sets text to print
        textToPrint = tempLine.substring(0, splitPoint);

        textToPrint = justify
            ? justifyLine(ctx, textToPrint, spaceWidth, SPACE, width)
            : textToPrint;
        textArray.add(textToPrint);
        tempLine = tempLine.substring(splitPoint);
        textWidth = measureText(ctx, textMap, tempLine);
      }

      if (textWidth > 0) {
        textToPrint = justify
            ? justifyLine(ctx, tempLine, spaceWidth, SPACE, width)
            : tempLine;
        textArray.add(textToPrint);
      }
    }
    return textArray;
  }

  private static double measureText(
      CanvasRenderingContext2D ctx, Map<String, Double> textMap, String text) {
    Double width = textMap.get(text);
    if (width != null) {
      return width;
    }

    width = ctx.measureText(text).width;
    textMap.put(text, width);
    return width;
  }
}
