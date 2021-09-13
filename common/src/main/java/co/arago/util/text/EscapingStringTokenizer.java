package co.arago.util.text;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A string tokenizer that handles escaping while tokenizing.
 */
public class EscapingStringTokenizer {

    /**
     * Include empty fields in list. Default is false.
     */
    public boolean includeEmpty = false;

    /**
     * Default delimiter = '/'
     */
    public char delimiter = '/';

    /**
     * Default escape char = '\'
     */
    public char escape = '\\';

    protected EscapingStringTokenizer() {
    }

    public static EscapingStringTokenizer newInstance() {
        return new EscapingStringTokenizer();
    }

    /**
     * Include empty fields in list. Default is false.
     *
     * @param includeEmpty the flag.
     * @return this
     */
    public EscapingStringTokenizer setIncludeEmpty(boolean includeEmpty) {
        this.includeEmpty = includeEmpty;
        return this;
    }

    /**
     * Default delimiter = '/'
     *
     * @param delimiter The delimiter char to set.
     * @return this
     */
    public EscapingStringTokenizer setDelimiter(char delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * Default escape char = '\'
     *
     * @param escape The escape char to use.
     * @return this
     */
    public EscapingStringTokenizer setEscape(char escape) {
        this.escape = escape;
        return this;
    }

    private void setToken(List<String> tokens, String token) {
        if (StringUtils.isNotEmpty(token) || includeEmpty)
            tokens.add(token);
    }

    /**
     * Build the list of tokens from the input string.
     *
     * @param input The string to tokenize.
     * @return List of tokens.
     * @throws IllegalArgumentException When the input has invalid escape.
     */
    public List<String> build(String input) {
        List<String> tokens = new ArrayList<>();
        if (StringUtils.isNotEmpty(input)) {
            StringBuilder sb = new StringBuilder();

            boolean inEscape = false;
            for (char current : input.toCharArray()) {
                if (inEscape) {
                    inEscape = false;
                } else if (current == escape) {
                    inEscape = true;
                    continue;
                } else if (current == delimiter) {
                    setToken(tokens, sb.toString());
                    sb.setLength(0);
                    continue;
                }
                sb.append(current);
            }
            if (inEscape)
                throw new IllegalArgumentException("Invalid terminal escape in input");

            setToken(tokens, sb.toString());
        }

        return tokens;
    }
}
