/**
 * Evaluates simple arithmetic typed by the user, e.g. "3/4", "2*(1+0.5)" or "12 x 2".
 * Supports + - * / x ( ) and decimals. Throws IllegalArgumentException when the text isn't valid.
 */
public final class MathExpression {

    private final String text;
    private int pos;

    private MathExpression(String text) {
        this.text = text;
    }

    public static double evaluate(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("empty");
        }
        MathExpression parser = new MathExpression(text);
        double value = parser.parseExpression();
        parser.skipSpaces();
        if (parser.pos != parser.text.length()) {
            throw new IllegalArgumentException("unexpected '" + parser.text.charAt(parser.pos) + "'");
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("not a number");
        }
        return value;
    }

    // expression := term (('+' | '-') term)*
    private double parseExpression() {
        double value = parseTerm();
        while (true) {
            skipSpaces();
            if (accept('+')) {
                value += parseTerm();
            } else if (accept('-')) {
                value -= parseTerm();
            } else {
                return value;
            }
        }
    }

    // term := factor (('*' | '/' | 'x') factor)*
    private double parseTerm() {
        double value = parseFactor();
        while (true) {
            skipSpaces();
            if (accept('*') || accept('x') || accept('X') || accept('\u00D7')) {
                value *= parseFactor();
            } else if (accept('/') || accept('\u00F7')) {
                double divisor = parseFactor();
                if (divisor == 0) {
                    throw new IllegalArgumentException("division by zero");
                }
                value /= divisor;
            } else {
                return value;
            }
        }
    }

    // factor := ('+' | '-') factor | '(' expression ')' | number
    private double parseFactor() {
        skipSpaces();
        if (accept('-')) {
            return -parseFactor();
        }
        if (accept('+')) {
            return parseFactor();
        }
        if (accept('(')) {
            double value = parseExpression();
            skipSpaces();
            if (!accept(')')) {
                throw new IllegalArgumentException("missing )");
            }
            return value;
        }
        return parseNumber();
    }

    private double parseNumber() {
        int start = pos;
        while (pos < text.length() && (Character.isDigit(text.charAt(pos)) || text.charAt(pos) == '.')) {
            pos++;
        }
        if (start == pos) {
            throw new IllegalArgumentException("number expected");
        }
        try {
            return Double.parseDouble(text.substring(start, pos));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bad number");
        }
    }

    private void skipSpaces() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
    }

    private boolean accept(char c) {
        if (pos < text.length() && text.charAt(pos) == c) {
            pos++;
            return true;
        }
        return false;
    }
}
