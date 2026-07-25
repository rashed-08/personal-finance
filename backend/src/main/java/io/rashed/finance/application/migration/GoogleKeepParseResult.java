package io.rashed.finance.application.migration;

import java.util.List;

/** Result of parsing a raw Google Keep export blob. Warnings never abort the parse — unparseable lines are skipped and recorded here instead. */
public record GoogleKeepParseResult(

        List<GoogleKeepMonth> months,

        List<String> warnings

) {
}
