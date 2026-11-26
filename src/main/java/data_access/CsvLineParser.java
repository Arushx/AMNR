package data_access;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits one line of CSV text into its fields, following the usual CSV quoting rules.
 *
 * <p>A plain {@code line.split(",")} breaks on text such as {@code "Oil, Gas & Fuels"}. Here a
 * field wrapped in double quotes may contain commas, and a doubled quote ({@code ""}) inside it
 * stands for one quote character.
 *
 * <p>Fields can't span several lines; our data files never need that.
 */
public final class CsvLineParser {

  private CsvLineParser() {
  }

  /**
   * Splits a line into fields.
   *
   * @param line one line of CSV, without its line ending
   * @return the fields, in order; an empty line gives one empty field
   * @throws IllegalArgumentException if a quoted field is never closed
   */
  public static List<String> parse(String line) {
    final List<String> fields = new ArrayList<>();
    final StringBuilder field = new StringBuilder();
    boolean inQuotes = false;
    int i = 0;
    while (i < line.length()) {
      final char c = line.charAt(i);
      if (inQuotes) {
        if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
          // A doubled quote inside quotes is one literal quote.
          field.append('"');
          i++;
        } else if (c == '"') {
          inQuotes = false;
        } else {
          field.append(c);
        }
      } else if (c == '"') {
        inQuotes = true;
      } else if (c == ',') {
        fields.add(field.toString());
        field.setLength(0);
      } else {
        field.append(c);
      }
      i++;
    }
    if (inQuotes) {
      throw new IllegalArgumentException("unclosed quote in CSV line: " + line);
    }
    fields.add(field.toString());
    return fields;
  }
}
