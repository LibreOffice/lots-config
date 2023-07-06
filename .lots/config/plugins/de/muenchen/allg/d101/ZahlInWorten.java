/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the European Union Public Licence (EUPL), 
 * version 1.0 (or any later version).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 *
 * You should have received a copy of the European Union Public Licence
 * along with this program. If not, see 
 * http://ec.europa.eu/idabc/en/document/7330
 */
package de.muenchen.allg.d101;

import java.util.regex.Pattern;

/**
 * Konvertiert eine Zahl in natürliche Sprache.
 * 
 * @author Matthias Benkmann (D-III-ITD-D101)
 */
public class ZahlInWorten
{
  /**
   * einemillionUNDeins oder einemillioneins.
   */
  private static boolean WILL_UND_NACH_GROSSER_ZAHL_VOR_2STELLIG = true;

  /**
   * hundertUNDeins oder hunderteins.
   */
  private static boolean WILL_UND_NACH_HUNDERT = true;

  private static final String[] ZIFFER =
    {
      "null", "eins", "zwei", "drei", "vier", "fünf", "sechs", "sieben", "acht",
      "neun" };

  private static final String[] SCALE =
    {
      "", "tausend", "million", "milliard", "billion", "billiard", "trillion",
      "trilliard", "quadrillion", "quadrilliard", "quintillion", "quintilliard",
      "sextillion", "sextilliard", "septillion", "septilliard", "oktillion",
      "oktilliard", "nonillion", "nonilliard", "dezillion", "dezilliard" };

  private static final String[] HUNDERT_BLOCK = {
    "ein", "zwei", "drei", "vier", "fünf", "sechs", "sieben", "acht", "neun" };

  private static final String[] EINER_BLOCK =
    {
      "eine", "zwei", "drei", "vier", "fünf", "sechs", "sieben", "acht", "neun",
      "zehn", "elf", "zwölf", "dreizehn", "vierzehn", "fünfzehn", "sechzehn",
      "siebzehn", "achtzehn", "neunzehn" };

  private static final String[] ZIG_BLOCK =
    {
      "einzig", "zwanzig", "dreißig", "vierzig", "fünfzig", "sechzig", "siebzig",
      "achtzig", "neunzig" };

  private static final Pattern LEGALE_ZAHL = Pattern.compile("\\A-?\\d+,?\\d*\\z");

  /**
   * Nimmt einen String, der eine Zahl beschreibt (mit Komma als Dezimaltrennzeichen
   * und ohne Gruppierungspunkte; optional mit führendem Minus; ohne Whitespace; ohne
   * Exponent; führende Nullen erlaubt) und konvertiert diesen in natürliche Sprache.
   * 
   * Hat der übergebene String kein erlaubtes Format wird ein leere String
   * zurückgeliefert.
   * 
   * @author Matthias Benkmann (D-III-ITD-D101)
   */
  public static String zahlInWorten(String str)
  {
    if (!LEGALE_ZAHL.matcher(str).matches()) return "";

    boolean minus = str.charAt(0) == '-';
    if (minus) str = str.substring(1);

    String vorkomma;
    String nachkomma;
    int commaPos = str.indexOf(',');
    if (commaPos == 0)
    {
      str = 0 + str;
      ++commaPos;
    }

    if (commaPos >= 0)
    {
      vorkomma = str.substring(0, commaPos);
      nachkomma = str.substring(commaPos + 1);
    }
    else
    {
      vorkomma = str;
      nachkomma = "";
    }

    StringBuilder buffy = new StringBuilder(convertVorkomma(vorkomma));
    String nach = convertNachkomma(nachkomma);
    if (nach.length() > 0) buffy.append(" komma ");
    buffy.append(nach);
    if (minus) buffy.insert(0, "minus ");
    return buffy.toString();
  }

  /**
   * Nimmt einen (potentiell leeren) String aus Ziffern und konvertiert ihn in die
   * Worte dieser Ziffern, einfach aneinandergereiht.
   * 
   * @author Matthias Benkmann (D-III-ITD-D101)
   */
  private static String convertNachkomma(String str)
  {
    StringBuilder buffy = new StringBuilder();
    for (int i = 0; i < str.length(); ++i)
    {
      buffy.append(ZIFFER[str.charAt(i) - '0']);
      if (i < str.length()) buffy.append(' ');
    }
    return buffy.toString();
  }

  /**
   * Nimmt einen String, der eine nicht-negative ganze Zahl beschreibt (ohne
   * Gruppierungspunkte; kein Whitespace; führende 0er erlaubt) und konvertiert
   * diesen in natürlich Sprache. Ein leerer String wird in "null" übersetzt.
   * 
   * @author Matthias Benkmann (D-III-ITD-D101)
   */
  private static String convertVorkomma(String str)
  {
    StringBuilder buffy = new StringBuilder();

    // führende Nuller entfernen
    int i = 0;
    while (i < str.length() && str.charAt(i) == '0')
      ++i;
    str = str.substring(i);

    if (str.length() == 0) return "null";

    int scaleIndex = 0;

    boolean und = false;

    i = str.length();
    while (i > 0)
    {
      int len = 3;
      i -= 3;
      if (i < 0)
      {
        len += i;
        i = 0;
      }

      String dreiStr = str.substring(i, i + len);
      if (scaleIndex == 0 && dreiStr.charAt(0) == '0') und = true;
      String dreierGruppe = convertDreierGruppe(dreiStr);
      if (scaleIndex == 0 && dreierGruppe.length() == 0) und = false;
      if (dreierGruppe.length() > 0)
      {
        und = und && WILL_UND_NACH_GROSSER_ZAHL_VOR_2STELLIG;
        if (und && scaleIndex > 0)
        {
          buffy.insert(0, "und");
          und = false;
        }

        if (dreierGruppe.endsWith("eine"))
        {
          if (scaleIndex == 1) // "eine" -> "ein" vor "tausend"
            dreierGruppe = dreierGruppe.substring(0, dreierGruppe.length() - 1);
          else if (scaleIndex > 1 && ((scaleIndex & 1) == 1))
            buffy.insert(0, "e"); // "iard" -> "iarde"
          else if (scaleIndex == 0)
          {
            // "eine" -> "eins" falls kein Suffix mehr dahinter
            dreierGruppe =
              dreierGruppe.substring(0, dreierGruppe.length() - 1) + "s";
          }
        }
        else
        {
          // "ion" -> "ionen" , "iard" -> "iarden"
          if (scaleIndex > 1) buffy.insert(0, "en");
        }

        buffy.insert(0, SCALE[scaleIndex]);

        buffy.insert(0, dreierGruppe);
      }
      if (++scaleIndex >= SCALE.length) return "sehr viel";
    }

    return buffy.toString();
  }

  /**
   * Konvertiert eine maximal dreistellige nicht-negative ganze Zahl in ein
   * Wort-Präfix, das man vor "million" bzw. "millionen" setzen kann. "1" wird zu
   * "eine". "101" wird zu "hundert[und]eine" ([und] hängt von der Konstante
   * {@link #WILL_UND_NACH_HUNDERT} ab}, d.h. es wird immer auf "eine" geendet, nie
   * auf "ein".
   * 
   * Ein leerer String bzw. ein String nur aus 0ern wird zum leeren String
   * konvertiert.
   * 
   * @author Matthias Benkmann (D-III-ITD-D101)
   */
  private static String convertDreierGruppe(String str)
  {
    // führende Nuller entfernen
    int i = 0;
    while (i < str.length() && str.charAt(i) == '0')
      ++i;
    str = str.substring(i);

    if (str.length() == 0) return "";

    StringBuilder buffy = new StringBuilder();
    boolean und = false;
    if (str.length() == 3)
    {
      buffy.append(HUNDERT_BLOCK[str.charAt(0) - '1']);
      buffy.append("hundert");
      str = str.substring(1);
      if (str.charAt(0) == '0') str = str.substring(1);
      if (str.charAt(0) == '0') str = str.substring(1);
      und = WILL_UND_NACH_HUNDERT;
    }

    if (str.length() == 1)
    {
      if (und) buffy.append("und");
      buffy.append(EINER_BLOCK[str.charAt(0) - '1']);
    }
    else if (str.length() == 2)
    {
      // if (und) buffy.append("und");
      char ch = str.charAt(0);
      if (ch == '1')
      {
        int num = Integer.parseInt(str) - 1;
        buffy.append(EINER_BLOCK[num]);
      }
      else
      {
        if (str.charAt(1) != '0')
        {
          buffy.append(HUNDERT_BLOCK[str.charAt(1) - '1']);
          buffy.append("und");
        }

        buffy.append(ZIG_BLOCK[str.charAt(0) - '1']);
      }
    }

    return buffy.toString();
  }
}
