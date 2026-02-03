import java.io.*;
import java.net.*;
import java.util.*;

/**
 * HtmlAnalyzer — reads an HTML document from a given URL and returns
 * the text snippet that is at the deepest nesting level.
 *
 * Constraints (per the problem statement):
 *   - Each line is EITHER an opening tag, a closing tag, or a text snippet.
 *   - Tags have no attributes.
 *   - Only paired tags (open + close) are used.
 *   - Leading whitespace (indentation) and blank lines are ignored.
 *
 * Bonus: detects and reports malformed HTML.
 */
public class HtmlAnalyzer {

    // ── Entry point ────────────────────────────────────────────────────────
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java HtmlAnalyzer <URL>");
            return;
        }

        String html;
        try {
            html = fetchHtml(args[0]);
        } catch (Exception e) {
            System.out.println("URL connection error");
            return;
        }

        String result = analyze(html);
        System.out.println(result);
    }

    // ── Fetch HTML from URL ────────────────────────────────────────────────
    private static String fetchHtml(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(10_000);

        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException("HTTP error: " + responseCode);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } finally {
            connection.disconnect();
        }
    }

    // ── Core analysis logic ────────────────────────────────────────────────
    /**
     * Parses the HTML string line-by-line and tracks nesting depth.
     * Returns the first text snippet found at the maximum depth,
     * or "malformed HTML" if the structure is invalid.
     */
    static String analyze(String html) {
        String[] lines = html.split("\n", -1);

        int currentDepth = 0;
        int maxDepth = -1;
        String deepestText = null;

        // Stack to track open tags — used for malformed-HTML detection
        Deque<String> tagStack = new ArrayDeque<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();

            // Skip blank lines
            if (line.isEmpty()) continue;

            // Classify the line
            LineType type = classifyLine(line);

            switch (type) {
                case OPEN_TAG -> {
                    String tagName = extractTagName(line, false);
                    tagStack.push(tagName);
                    currentDepth++;
                }
                case CLOSE_TAG -> {
                    String tagName = extractTagName(line, true);

                    // Malformed: closing a tag that was never opened or mismatches
                    if (tagStack.isEmpty() || !tagStack.peek().equalsIgnoreCase(tagName)) {
                        return "malformed HTML";
                    }
                    tagStack.pop();
                    currentDepth--;
                }
                case TEXT -> {
                    if (currentDepth > maxDepth) {
                        maxDepth = currentDepth;
                        deepestText = line;
                    }
                }
                case UNKNOWN -> {
                    // A line that doesn't fit any expected pattern → malformed
                    return "malformed HTML";
                }
            }
        }

        // If there are unclosed tags remaining, the HTML is malformed
        if (!tagStack.isEmpty()) {
            return "malformed HTML";
        }

        // Edge case: no text found at all
        if (deepestText == null) {
            return "malformed HTML";
        }

        return deepestText;
    }

    // ── Line classification ────────────────────────────────────────────────

    enum LineType {
        OPEN_TAG,   // e.g. <div>
        CLOSE_TAG,  // e.g. </div>
        TEXT,       // plain text content
        UNKNOWN     // doesn't match any expected pattern
    }

    /**
     * Determines whether a trimmed, non-empty line is an opening tag,
     * closing tag, text, or something unrecognisable.
     */
    private static LineType classifyLine(String line) {
        if (line.startsWith("</") && line.endsWith(">")) {
            // Closing tag — validate it contains only a tag name
            String inner = line.substring(2, line.length() - 1).trim();
            return isValidTagName(inner) ? LineType.CLOSE_TAG : LineType.UNKNOWN;
        }
        if (line.startsWith("<") && line.endsWith(">")) {
            // Opening tag — validate it contains only a tag name (no attributes)
            String inner = line.substring(1, line.length() - 1).trim();
            return isValidTagName(inner) ? LineType.OPEN_TAG : LineType.UNKNOWN;
        }
        // If the line contains '<' or '>' but isn't a clean tag → ambiguous
        // Treat as text only if it has NO angle brackets at all
        if (line.contains("<") || line.contains(">")) {
            return LineType.UNKNOWN;
        }
        return LineType.TEXT;
    }

    // ── Tag-name helpers ───────────────────────────────────────────────────

    /**
     * Extracts the tag name from an opening or closing tag line.
     * @param isClosing true if the line starts with "</"
     */
    private static String extractTagName(String line, boolean isClosing) {
        int start = isClosing ? 2 : 1;
        return line.substring(start, line.length() - 1).trim();
    }

    /**
     * A valid HTML tag name: starts with a letter, followed by
     * letters or digits only (simplified rule matching the problem scope).
     */
    private static boolean isValidTagName(String name) {
        if (name.isEmpty()) return false;
        if (!Character.isLetter(name.charAt(0))) return false;
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c)) return false;
        }
        return true;
    }
}
