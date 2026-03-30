package io.github.sinri.keel.app.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineParserUnitTest {

    private CommandLineArgumentsParser createParser(CommandLineOption... options) {
        var parser = CommandLineArgumentsParser.create();
        for (var opt : options) {
            parser.addOption(opt);
        }
        return parser;
    }

    // --- CommandLineOption tests ---

    @Test
    void validatedAlias_acceptsValidNames() {
        assertEquals("foo", CommandLineOption.validatedAlias("foo"));
        assertEquals("foo.bar", CommandLineOption.validatedAlias("foo.bar"));
        assertEquals("foo-bar", CommandLineOption.validatedAlias("foo-bar"));
        assertEquals("foo_bar", CommandLineOption.validatedAlias("foo_bar"));
        assertEquals("A1", CommandLineOption.validatedAlias("A1"));
    }

    @Test
    void validatedAlias_rejectsInvalidNames() {
        assertThrows(IllegalArgumentException.class, () -> CommandLineOption.validatedAlias("-foo"));
        assertThrows(IllegalArgumentException.class, () -> CommandLineOption.validatedAlias(""));
        assertThrows(IllegalArgumentException.class, () -> CommandLineOption.validatedAlias("foo bar"));
    }

    @Test
    void option_alias_fluent() {
        var opt = new CommandLineOption().alias("name").alias("n").description("desc").flag();
        assertTrue(opt.getAliasSet().contains("name"));
        assertTrue(opt.getAliasSet().contains("n"));
        assertEquals("desc", opt.description());
        assertTrue(opt.isFlag());
    }

    // --- Parser: empty args ---

    @Test
    void parse_emptyArgs_returnsEmptyResult() throws CommandLineArgumentsParseError {
        var parser = createParser(new CommandLineOption().alias("verbose").flag());
        var result = parser.parse(new String[]{});
        assertFalse(result.readFlag("verbose"));
        assertNull(result.readParameter(0));
    }

    // --- Parser: flags ---

    @Test
    void parse_longFlag() throws CommandLineArgumentsParseError {
        var parser = createParser(new CommandLineOption().alias("verbose").flag());
        var result = parser.parse(new String[]{"--verbose"});
        assertTrue(result.readFlag("verbose"));
    }

    @Test
    void parse_shortFlag() throws CommandLineArgumentsParseError {
        var parser = createParser(new CommandLineOption().alias("v").flag());
        var result = parser.parse(new String[]{"-v"});
        assertTrue(result.readFlag("v"));
        assertTrue(result.readFlag('v'));
    }

    // --- Parser: options with values ---

    @Test
    void parse_longOptionWithValue() throws CommandLineArgumentsParseError {
        var parser = createParser(new CommandLineOption().alias("port"));
        var result = parser.parse(new String[]{"--port", "8080"});
        assertEquals("8080", result.readOption("port"));
    }

    @Test
    void parse_shortOptionWithValue() throws CommandLineArgumentsParseError {
        var parser = createParser(new CommandLineOption().alias("p"));
        var result = parser.parse(new String[]{"-p", "8080"});
        assertEquals("8080", result.readOption("p"));
        assertEquals("8080", result.readOption('p'));
    }

    // --- Parser: parameters ---

    @Test
    void parse_pureParameters() throws CommandLineArgumentsParseError {
        var parser = createParser();
        var result = parser.parse(new String[]{"file1.txt", "file2.txt"});
        assertEquals("file1.txt", result.readParameter(0));
        assertEquals("file2.txt", result.readParameter(1));
        assertNull(result.readParameter(2));
    }

    @Test
    void parse_parametersAfterDoubleDash() throws CommandLineArgumentsParseError {
        var parser = createParser(new CommandLineOption().alias("verbose").flag());
        var result = parser.parse(new String[]{"--verbose", "--", "param1", "param2"});
        assertTrue(result.readFlag("verbose"));
        assertEquals("param1", result.readParameter(0));
        assertEquals("param2", result.readParameter(1));
    }

    // --- Parser: mixed options, flags, and parameters ---

    @Test
    void parse_mixedOptionsAndParameters() throws CommandLineArgumentsParseError {
        var parser = createParser(
                new CommandLineOption().alias("output").alias("o"),
                new CommandLineOption().alias("verbose").alias("v").flag()
        );
        var result = parser.parse(new String[]{"--output", "/tmp/out", "-v", "--", "input.txt"});
        assertEquals("/tmp/out", result.readOption("output"));
        assertEquals("/tmp/out", result.readOption("o"));
        assertTrue(result.readFlag("verbose"));
        assertTrue(result.readFlag("v"));
        assertEquals("input.txt", result.readParameter(0));
    }

    // --- Parser: value validation ---

    @Test
    void parse_valueValidation_passes() throws CommandLineArgumentsParseError {
        var parser = createParser(
                new CommandLineOption().alias("port").setValueValidator(s -> {
                    try {
                        int port = Integer.parseInt(s);
                        return port >= 1 && port <= 65535;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
        );
        var result = parser.parse(new String[]{"--port", "8080"});
        assertEquals("8080", result.readOption("port"));
    }

    @Test
    void parse_valueValidation_fails() {
        var parser = createParser(
                new CommandLineOption().alias("port").setValueValidator(s -> {
                    try {
                        int port = Integer.parseInt(s);
                        return port >= 1 && port <= 65535;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
        );
        assertThrows(CommandLineArgumentsParseError.class,
                () -> parser.parse(new String[]{"--port", "99999"}));
    }

    // --- Parser: error cases ---

    @Test
    void parse_unknownOption_throws() {
        var parser = createParser(new CommandLineOption().alias("verbose").flag());
        assertThrows(CommandLineArgumentsParseError.class,
                () -> parser.parse(new String[]{"--unknown"}));
    }

    @Test
    void parse_optionMissingValue_throws() {
        var parser = createParser(new CommandLineOption().alias("port"));
        assertThrows(CommandLineArgumentsParseError.class,
                () -> parser.parse(new String[]{"--port"}));
    }

    @Test
    void addOption_duplicateAlias_throws() {
        var parser = CommandLineArgumentsParser.create();
        parser.addOption(new CommandLineOption().alias("name"));
        assertThrows(CommandLineArgumentsDefinitionError.class,
                () -> parser.addOption(new CommandLineOption().alias("name")));
    }

    @Test
    void addOption_noAlias_throws() {
        var parser = CommandLineArgumentsParser.create();
        assertThrows(CommandLineArgumentsDefinitionError.class,
                () -> parser.addOption(new CommandLineOption()));
    }

    // --- Parameter index boundary ---

    @Test
    void readParameter_negativeIndex_returnsNull() throws CommandLineArgumentsParseError {
        var parser = createParser();
        var result = parser.parse(new String[]{"a"});
        assertNull(result.readParameter(-1));
    }
}
