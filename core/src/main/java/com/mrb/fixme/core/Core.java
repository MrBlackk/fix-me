package com.mrb.fixme.core;

import com.mrb.fixme.core.exception.UserInputValidationException;
import com.mrb.fixme.core.exception.WrongFixTagException;

import java.util.regex.Pattern;

public class Core {

    public static final String HOST_NAME = "127.0.0.1";
    public static final int BROKER_PORT = 5000;
    public static final int MARKET_PORT = 5001;
    public static final int INITIAL_ID = 1;
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final String ID_FORMAT = "%06d";
    public static final String USER_MESSAGE_FORMAT = "'MARKET_NAME BUY_OR_SELL INSTRUMENT_NAME QUANTITY PRICE'";

    private static final String USER_INPUT_DELIMITER = " ";
    private static final String TAG_VALUE_DELIMITER = "=";
    private static final String FIELD_DELIMITER = "|";

    public static String userInputToFixMessage(String input, String id, String name) throws UserInputValidationException {
        final String[] m = input.split(USER_INPUT_DELIMITER);
        if (m.length != 5) {
            throw new UserInputValidationException("Wrong input, should be: " + USER_MESSAGE_FORMAT);
        }
        final StringBuilder builder = new StringBuilder();
        addTag(builder, FixTag.ID, id);
        addTag(builder, FixTag.SOURCE_NAME, name);
        addTag(builder, FixTag.TARGET_NAME, m[0]);
        addTag(builder, FixTag.TYPE, m[1]);
        addTag(builder, FixTag.INSTRUMENT, m[2]);
        addTag(builder, FixTag.QUANTITY, m[3]);
        addTag(builder, FixTag.PRICE, m[4]);
        addTag(builder, FixTag.CHECKSUM, calculateChecksum(builder.toString()));
        return builder.toString();
    }

    public static String resultFixMessage(String message, String id, String srcName, String targetName, Result result) {
        final StringBuilder builder = new StringBuilder();
        addTag(builder, FixTag.ID, id);
        addTag(builder, FixTag.SOURCE_NAME, srcName);
        addTag(builder, FixTag.TARGET_NAME, targetName);
        addTag(builder, FixTag.RESULT, result.toString());
        addTag(builder, FixTag.MESSAGE, message);
        addTag(builder, FixTag.CHECKSUM, calculateChecksum(builder.toString()));
        return builder.toString();
    }

    private static void addTag(StringBuilder builder, FixTag tag, String value) {
        builder.append(tag.getValue())
                .append(TAG_VALUE_DELIMITER)
                .append(value)
                .append(FIELD_DELIMITER);
    }

    public static String calculateChecksum(String message) {
        final byte[] bytes = message.getBytes();
        int sum = 0;
        for (byte aByte : bytes) {
            sum += aByte;
        }
        return String.format("%03d", sum % 256);
    }

    public static String getMessageWithoutChecksum(String fixMessage) {
        final int checksumIndex = fixMessage.lastIndexOf(FixTag.CHECKSUM.getValue() + Core.TAG_VALUE_DELIMITER);
        return fixMessage.substring(0, checksumIndex);
    }

    public static String getFixValueByTag(String fixMessage, FixTag tag) {
        final String[] tagValues = fixMessage.split(Pattern.quote(FIELD_DELIMITER));
        final String searchPattern = tag.getValue() + TAG_VALUE_DELIMITER;
        for (String tagValue : tagValues) {
            if (tagValue.startsWith(searchPattern)) {
                return tagValue.substring(searchPattern.length());
            }
        }
        throw new WrongFixTagException("No '" + tag + "' tag in message + '" + fixMessage + "'");
    }
}
