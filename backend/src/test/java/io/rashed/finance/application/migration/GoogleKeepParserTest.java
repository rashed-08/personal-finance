package io.rashed.finance.application.migration;

import io.rashed.finance.common.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleKeepParserTest {

    /** The user's actual Google Keep export sample, verbatim (Bengali digits, real formatting quirks). */
    private static final String REAL_SAMPLE = """
            ০৫-২৬
            =========

            গ্যাস ১৮৫০

            জীম ১৫০০

            বাসা ভাড়া ১০০০০

            ইন্টারনেট বিল ৯০০

            ইলেকট্রিসিটি বিল ১০০০

            ঔষধ ৩০০০  (১৭৫০+৫০০+১৮৫+৪০০+১৮০

            বাজার ১৩৯০০ (৬৯০+১৩৫০+২১৩০+৮০০+৬২০+১৯৩০+৫৯০+৩৪০+২৪৫০+৮৫০+৪৭০+৬০০+৭০০+৩৮০)

            আফিয়া (৪৩০+২২৫০+২৩০+১১৩০+৪৫০

            দুধ ৩০০+৩০০+৩০০+৩০০

            রিচার্জ ১৯০+১২০+১০০

            খরচ ৩৩০+৩৬০+২০০+৩০০

            টেকনাফ ১০০০

            আপা ১০০০

            বেল্ট ১৫০০

            &#x20;

            =৪২০০০

            ০৪-২৬
            =========

            জীম ১৫০০

            বাসা ভাড়া ১০০০০

            ইন্টারনেট বিল ৯০০

            ইলেকট্রিসিটি বিল ১৭০০

            ঔষধ ২৭৭০ (৯৮০+১৭৯০

            বাজার ১০৭৩৫ ( ৯০০+৭২৫+১২০০+৪৮০+১৩৮০+৩৮০+৪৭৫+৩০০+১১৭৫+৪৫০+৩০০+১৭০০+৬০০+৬৭০'+৭০০+৬৫০

            দুধ ১২০০ (৪০০+২০০+৩০০+৩০০

            আফিয়া ১৭০০ (১২০০+৫০০)

            ব্লেন্ডার ৬৫০০

            রিচার্জ ৪৯০ (১৬০+১০০+২৩০)

            মরিচ ৭০০

            টেকনাফ ১০০০+৩৮০+৮০০+৮৫০

            খালাম্মা কুকার ১৭০০

            ধান ১০০০০

            মামলা ৩৫০০ (১০০০+২৫০০)

            দরগাহ ৫০০

            কোরবানি ৫০০+৩০০+১৫৫০০+১৩০+১১০+২৫০

            খরচ ৩৫০+৪৩০+৪০০+৩৫০+

            &#x20;

            =৭৫৮০০
            """;

    @Test
    void parse_realSample_findsBothMonthsWithCorrectTotals() {

        GoogleKeepParseResult result = GoogleKeepParser.parse(REAL_SAMPLE);

        assertEquals(2, result.months().size());

        GoogleKeepMonth may = result.months().get(0);
        assertEquals(YearMonth.of(2026, 5), may.yearMonth());
        assertEquals(Money.of(42000), may.statedTotal());
        assertEquals(14, may.lines().size());

        GoogleKeepMonth april = result.months().get(1);
        assertEquals(YearMonth.of(2026, 4), april.yearMonth());
        assertEquals(Money.of(75800), april.statedTotal());
        assertEquals(18, april.lines().size());
    }

    @Test
    void parse_realSample_simpleAmountLine() {

        GoogleKeepMonth may = GoogleKeepParser.parse(REAL_SAMPLE).months().get(0);

        GoogleKeepLine gas = findLine(may, "গ্যাস");
        assertEquals(Money.of(1850), gas.amount());
        assertNull(gas.breakdownNotes());
    }

    @Test
    void parse_realSample_statedAmountWinsOverUnclosedBreakdownSum() {

        GoogleKeepMonth may = GoogleKeepParser.parse(REAL_SAMPLE).months().get(0);

        // ঔষধ ৩০০০ (১৭৫০+৫০০+১৮৫+৪০০+১৮০ — breakdown sums to 3015, stated 3000 must win, unclosed paren.
        GoogleKeepLine medicine = findLine(may, "ঔষধ");
        assertEquals(Money.of(3000), medicine.amount());
        assertEquals("1750+500+185+400+180", medicine.breakdownNotes());
    }

    @Test
    void parse_realSample_closedBreakdownParens() {

        GoogleKeepMonth may = GoogleKeepParser.parse(REAL_SAMPLE).months().get(0);

        GoogleKeepLine market = findLine(may, "বাজার");
        assertEquals(Money.of(13900), market.amount());
        assertEquals("690+1350+2130+800+620+1930+590+340+2450+850+470+600+700+380", market.breakdownNotes());
    }

    @Test
    void parse_realSample_breakdownOnlyNoStatedAmount_computesSum() {

        GoogleKeepMonth may = GoogleKeepParser.parse(REAL_SAMPLE).months().get(0);

        // আফিয়া (৪৩০+২২৫০+২৩০+১১৩০+৪৫০ — no leading number at all, amount must be the breakdown sum.
        GoogleKeepLine afiya = findLine(may, "আফিয়া");
        assertEquals(Money.of(4490), afiya.amount());
    }

    @Test
    void parse_realSample_bareSumExpressionNoParens() {

        GoogleKeepMonth may = GoogleKeepParser.parse(REAL_SAMPLE).months().get(0);

        GoogleKeepLine milk = findLine(may, "দুধ");
        assertEquals(Money.of(1200), milk.amount());
        assertEquals("300+300+300+300", milk.breakdownNotes());
    }

    @Test
    void parse_realSample_danglingTrailingPlusIsIgnored() {

        GoogleKeepMonth april = GoogleKeepParser.parse(REAL_SAMPLE).months().get(1);

        // খরচ ৩৫০+৪৩০+৪০০+৩৫০+ — trailing '+' with nothing after it.
        GoogleKeepLine kharoch = findLine(april, "খরচ");
        assertEquals(Money.of(1530), kharoch.amount());
    }

    @Test
    void parse_realSample_multiWordCategoryLabel() {

        GoogleKeepMonth april = GoogleKeepParser.parse(REAL_SAMPLE).months().get(1);

        GoogleKeepLine cooker = findLine(april, "খালাম্মা কুকার");
        assertEquals(Money.of(1700), cooker.amount());
    }

    @Test
    void parse_realSample_bareSumExpressionMultipleTerms() {

        GoogleKeepMonth april = GoogleKeepParser.parse(REAL_SAMPLE).months().get(1);

        GoogleKeepLine teknaf = findLine(april, "টেকনাফ");
        assertEquals(Money.of(3030), teknaf.amount());

        GoogleKeepLine korbani = findLine(april, "কোরবানি");
        assertEquals(Money.of(16790), korbani.amount());
    }

    @Test
    void parse_ignoresBlankAndDividerLines() {

        String text = """
                01-26
                =========

                Gym 1500

                =1500
                """;

        GoogleKeepParseResult result = GoogleKeepParser.parse(text);

        assertEquals(1, result.months().size());
        assertEquals(1, result.months().get(0).lines().size());
        assertEquals(Money.of(1500), result.months().get(0).statedTotal());
    }

    @Test
    void parse_supportsTwoLineTotalWordFormat() {

        String text = """
                01-26
                =========

                Gym 1500

                Total
                1500
                """;

        GoogleKeepParseResult result = GoogleKeepParser.parse(text);

        assertEquals(1, result.months().size());
        assertEquals(Money.of(1500), result.months().get(0).statedTotal());
    }

    @Test
    void parse_convertsHtmlEntityArtifactsToBlankLines() {

        String text = """
                01-26
                =========

                Gym 1500

                &#x20;

                =1500
                """;

        GoogleKeepParseResult result = GoogleKeepParser.parse(text);

        assertEquals(1, result.months().size());
        assertEquals(1, result.months().get(0).lines().size());
    }

    @Test
    void parse_monthWithNoTotalLineIsStillClosedOut() {

        String text = """
                01-26
                =========

                Gym 1500

                02-26
                =========

                Rent 10000

                =10000
                """;

        GoogleKeepParseResult result = GoogleKeepParser.parse(text);

        assertEquals(2, result.months().size());
        assertEquals(YearMonth.of(2026, 1), result.months().get(0).yearMonth());
        assertNull(result.months().get(0).statedTotal());
        assertEquals(YearMonth.of(2026, 2), result.months().get(1).yearMonth());
        assertEquals(Money.of(10000), result.months().get(1).statedTotal());
    }

    @Test
    void parse_lineBeforeAnyMonthHeaderIsWarnedAndSkipped() {

        String text = """
                Gym 1500

                01-26
                =========

                Rent 10000

                =10000
                """;

        GoogleKeepParseResult result = GoogleKeepParser.parse(text);

        assertEquals(1, result.months().size());
        assertEquals(1, result.months().get(0).lines().size());
        assertTrue(result.warnings().stream().anyMatch(w -> w.contains("Gym 1500")));
    }

    private static GoogleKeepLine findLine(GoogleKeepMonth month, String categoryLabel) {

        return month.lines().stream()
                .filter(line -> line.categoryLabel().equals(categoryLabel))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No line found for category: " + categoryLabel));
    }
}
