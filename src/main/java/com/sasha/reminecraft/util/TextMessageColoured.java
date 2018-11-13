package com.sasha.reminecraft.util;

import com.github.steveice10.mc.protocol.data.message.TextMessage;

import java.util.regex.Pattern;

public class TextMessageColoured extends TextMessage {

    private static final char SECTION_SIGN = '\247';
    private static final Pattern COLOUR_REGEX = Pattern.compile("&([0-9a-fm-or])");

    public TextMessageColoured(String text) {
        super(ampersandToSectionSign(text));
    }

    /**
     * Replaces ampersand signs with section signs if they are followed by a valid color code character (0-9a-fm-or)
     * @return The string with section signs
     */
    private static String ampersandToSectionSign(String input) {
        return COLOUR_REGEX.matcher(input).replaceAll(SECTION_SIGN + "$1");
    }

    public static TextMessageColoured of(String text) {
        return new TextMessageColoured(text);
    }

}
