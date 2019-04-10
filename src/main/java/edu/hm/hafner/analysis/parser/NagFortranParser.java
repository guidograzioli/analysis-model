package edu.hm.hafner.analysis.parser;

import java.util.Optional;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.LookaheadParser;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.analysis.RegexpDocumentParser;
import edu.hm.hafner.util.LookaheadStream;

/**
 * A parser for messages from the NAG Fortran Compiler.
 *
 * @author Mat Cross.
 */
public class NagFortranParser extends LookaheadParser {
    private static final long serialVersionUID = 2072414911276743946L;

    private static final String NAGFOR_MSG_PATTERN = "^(Info|Warning|Questionable|Extension|Obsolescent|Deleted "
            + "feature used|Error|Runtime Error|Fatal Error|Panic): (.+\\.[^,:\\n]+)(, line (\\d+))?: (.+($\\s+detected"
            + " at .+)?)";

    /**
     * Creates a new instance of {@link NagFortranParser}.
     */
    public NagFortranParser() {
        super(NAGFOR_MSG_PATTERN);
    }

    @Deprecated
    protected Optional<Issue> createIssue(final Matcher matcher, final IssueBuilder builder) {
        String category = matcher.group(1);

        return builder.setFileName(matcher.group(2))
                .setLineStart(getLineNumber(matcher))
                .setCategory(category)
                .setMessage(matcher.group(5))
                .setSeverity(mapPriority(category))
                .buildOptional();
    }

    @Override
    protected Optional<Issue> createIssue(final Matcher matcher, final LookaheadStream lookahead,
            final IssueBuilder builder)
            throws ParsingException {

        if (lookahead.hasNext()) {
            String category = matcher.group(1);

            return builder.setFileName(matcher.group(2))
                    .setLineStart(getLineNumber(matcher))
                    .setCategory(category)
                    .setMessage(matcher.group(5))
                    .setSeverity(mapPriority(category))
                    .buildOptional();
        }
        else {
            throw new ParsingException("Bla bla");
        }
    }

    private Severity mapPriority(final String category) {
        switch (category) {
            case "Error":
            case "Runtime Error":
            case "Fatal Error":
            case "Panic":
                return Severity.WARNING_HIGH;
            case "Info":
                return Severity.WARNING_LOW;
            default:
                return Severity.WARNING_NORMAL;
        }
    }

    private int getLineNumber(final Matcher matcher) {
        int lineNumber;
        if (StringUtils.isEmpty(matcher.group(4))) {
            lineNumber = 0;
        }
        else {
            lineNumber = Integer.parseInt(matcher.group(4));
        }
        return lineNumber;
    }
}
