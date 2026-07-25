package io.rashed.finance.application.migration;

import io.rashed.finance.common.valueobject.Money;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts a raw Google Keep export blob (per docs/business/GoogleKeepMigration.md)
 * into structured {@link GoogleKeepMonth} records. Pure text processing — no
 * I/O, no persistence, no Spring dependency.
 *
 * <p>Handles real-world formatting the doc's illustrative example doesn't
 * show: Bengali numerals, HTML entity artifacts (e.g. {@code &#x20;}),
 * unclosed breakdown parentheses, bare "+"-joined sum expressions with no
 * parentheses at all, category lines with no stated amount (amount is then
 * the breakdown sum), a dangling trailing "+", and a "=NNNN" total line
 * glued together rather than the doc's two-line "Total" / NNNN form (both
 * are supported).
 */
public final class GoogleKeepParser {

    private static final Pattern MONTH_HEADER = Pattern.compile("^(\\d{1,2})-(\\d{2,4})$");

    private static final Pattern GLUED_TOTAL = Pattern.compile("^=\\s*(\\d+(?:\\.\\d+)?)$");

    private static final Pattern HEX_ENTITY = Pattern.compile("&#x([0-9A-Fa-f]+);");

    private static final Pattern DEC_ENTITY = Pattern.compile("&#([0-9]+);");

    private static final int BENGALI_ZERO = 0x09E6;

    private GoogleKeepParser() {
    }

    public static GoogleKeepParseResult parse(String content) {

        List<GoogleKeepMonth> months = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        YearMonth currentYearMonth = null;
        List<GoogleKeepLine> currentLines = new ArrayList<>();
        boolean pendingTotalWord = false;

        String[] rawLines = content == null ? new String[0] : content.split("\\r?\\n");

        for (String rawLine : rawLines) {

            String line = normalize(rawLine);

            if (line.isEmpty() || isDivider(line)) {
                continue;
            }

            if (pendingTotalWord) {

                Money total = parsePlainNumber(line);
                pendingTotalWord = false;

                if (currentYearMonth != null && total != null) {
                    months.add(new GoogleKeepMonth(currentYearMonth, List.copyOf(currentLines), total));
                    currentYearMonth = null;
                    currentLines = new ArrayList<>();
                    continue;
                }
                // Fall through — the line after "Total" wasn't a bare number, treat it normally below.
            }

            YearMonth month = parseMonthHeader(line);

            if (month != null) {

                if (currentYearMonth != null) {
                    // Previous month had no explicit total line — close it out anyway.
                    months.add(new GoogleKeepMonth(currentYearMonth, List.copyOf(currentLines), null));
                }

                currentYearMonth = month;
                currentLines = new ArrayList<>();
                continue;
            }

            Money gluedTotal = parseGluedTotal(line);

            if (gluedTotal != null) {

                if (currentYearMonth == null) {
                    warnings.add("Total line found before any month header, ignored: \"" + rawLine.trim() + "\"");
                    continue;
                }

                months.add(new GoogleKeepMonth(currentYearMonth, List.copyOf(currentLines), gluedTotal));
                currentYearMonth = null;
                currentLines = new ArrayList<>();
                continue;
            }

            if (line.equalsIgnoreCase("total")) {
                pendingTotalWord = true;
                continue;
            }

            if (currentYearMonth == null) {
                warnings.add("Line found before any month header, skipped: \"" + rawLine.trim() + "\"");
                continue;
            }

            GoogleKeepLine parsed = parseCategoryLine(line);

            if (parsed == null) {
                warnings.add("Could not detect an amount on line, skipped: \"" + rawLine.trim() + "\"");
                continue;
            }

            currentLines.add(parsed);
        }

        if (currentYearMonth != null) {
            months.add(new GoogleKeepMonth(currentYearMonth, List.copyOf(currentLines), null));
        }

        return new GoogleKeepParseResult(List.copyOf(months), List.copyOf(warnings));
    }

    // -------------------------------------------------------------------------
    // Line classification
    // -------------------------------------------------------------------------

    private static boolean isDivider(String line) {

        return line.chars().allMatch(c -> c == '=') && line.length() >= 3;
    }

    private static YearMonth parseMonthHeader(String line) {

        Matcher matcher = MONTH_HEADER.matcher(line);

        if (!matcher.matches()) {
            return null;
        }

        int month = Integer.parseInt(matcher.group(1));
        int year = Integer.parseInt(matcher.group(2));

        if (month < 1 || month > 12) {
            return null;
        }

        int fullYear = year < 100 ? 2000 + year : year;

        return YearMonth.of(fullYear, month);
    }

    private static Money parseGluedTotal(String line) {

        Matcher matcher = GLUED_TOTAL.matcher(line);

        if (!matcher.matches()) {
            return null;
        }

        return Money.of(new BigDecimal(matcher.group(1)));
    }

    private static Money parsePlainNumber(String line) {

        if (!line.matches("\\d+(\\.\\d+)?")) {
            return null;
        }

        return Money.of(new BigDecimal(line));
    }

    // -------------------------------------------------------------------------
    // Category line parsing
    // -------------------------------------------------------------------------

    private static GoogleKeepLine parseCategoryLine(String normalizedLine) {

        int parenIndex = normalizedLine.indexOf('(');

        String beforeParen = parenIndex >= 0 ? normalizedLine.substring(0, parenIndex).trim() : normalizedLine.trim();

        String parenContent = null;

        if (parenIndex >= 0) {

            String afterParen = normalizedLine.substring(parenIndex + 1);

            parenContent = afterParen.endsWith(")")
                    ? afterParen.substring(0, afterParen.length() - 1).trim()
                    : afterParen.trim();
        }

        int firstDigitIndex = -1;

        for (int i = 0; i < beforeParen.length(); i++) {
            if (Character.isDigit(beforeParen.charAt(i))) {
                firstDigitIndex = i;
                break;
            }
        }

        String label;
        String numericPart;

        if (firstDigitIndex < 0) {
            label = beforeParen.trim();
            numericPart = "";
        } else {
            label = beforeParen.substring(0, firstDigitIndex).trim();
            numericPart = beforeParen.substring(firstDigitIndex).trim();
        }

        if (label.isEmpty()) {
            return null;
        }

        Money amount;
        String notes;

        if (!numericPart.isEmpty() && numericPart.contains("+")) {

            amount = sumPlusExpression(numericPart);
            notes = parenContent != null ? numericPart + " (" + parenContent + ")" : numericPart;

        } else if (!numericPart.isEmpty()) {

            amount = parsePlainNumber(numericPart);
            notes = parenContent;

        } else if (parenContent != null) {

            amount = sumPlusExpression(parenContent);
            notes = parenContent;

        } else {
            return null;
        }

        if (amount == null || !amount.isPositive()) {
            return null;
        }

        return new GoogleKeepLine(normalizedLine, label, amount, notes);
    }

    private static Money sumPlusExpression(String expression) {

        BigDecimal sum = BigDecimal.ZERO;
        boolean foundAny = false;

        for (String term : expression.split("\\+")) {

            String trimmed = term.trim();

            if (trimmed.isEmpty() || !trimmed.matches("\\d+(\\.\\d+)?")) {
                continue;
            }

            sum = sum.add(new BigDecimal(trimmed));
            foundAny = true;
        }

        return foundAny ? Money.of(sum) : null;
    }

    // -------------------------------------------------------------------------
    // Normalization
    // -------------------------------------------------------------------------

    private static String normalize(String rawLine) {

        String decoded = decodeHtmlEntities(rawLine);
        String digitsConverted = convertBengaliDigits(decoded);

        return digitsConverted.trim();
    }

    private static String decodeHtmlEntities(String text) {

        String result = text
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");

        result = replaceEntities(result, HEX_ENTITY, 16);
        result = replaceEntities(result, DEC_ENTITY, 10);

        return result;
    }

    private static String replaceEntities(String text, Pattern pattern, int radix) {

        Matcher matcher = pattern.matcher(text);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {

            int codePoint = Integer.parseInt(matcher.group(1), radix);
            matcher.appendReplacement(builder, Matcher.quoteReplacement(new String(Character.toChars(codePoint))));
        }

        matcher.appendTail(builder);

        return builder.toString();
    }

    private static String convertBengaliDigits(String text) {

        StringBuilder builder = new StringBuilder(text.length());

        for (int i = 0; i < text.length(); i++) {

            char c = text.charAt(i);

            if (c >= BENGALI_ZERO && c <= BENGALI_ZERO + 9) {
                builder.append((char) ('0' + (c - BENGALI_ZERO)));
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }
}
