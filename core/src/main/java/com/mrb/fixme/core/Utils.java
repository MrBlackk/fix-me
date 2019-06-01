package com.mrb.fixme.core;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Utils {

    public static final String INTERNAL_MESSAGE = "INTERNAL_MESSAGE:";
    public static final String EMPTY_MESSAGE = "";
    private static final String[] INSTRUMENTS = {
            "bolt", "nail", "screwdriver", "screw",
            "hammer", "saw", "drill", "wrench", "knife",
            "scissors", "toolbox", "tape", "needle"
    };

    public static String readMessage(AsynchronousSocketChannel channel, ByteBuffer readBuffer) {
        try {
            return read(channel.read(readBuffer).get(), readBuffer);
        } catch (InterruptedException | ExecutionException e) {
            return EMPTY_MESSAGE;
        }
    }

    public static String read(int bytesRead, ByteBuffer readBuffer) {
        if (bytesRead != -1) {
            readBuffer.flip();
            byte[] bytes = new byte[bytesRead];
            readBuffer.get(bytes, 0, bytesRead);
            readBuffer.clear();
            String message = new String(bytes);
            System.out.println("Got: " + message);
            return message;
        }
        return EMPTY_MESSAGE;
    }

    public static Future<Integer> sendMessage(AsynchronousSocketChannel channel, String message) {
        System.out.println("Send: " + message);
        return channel.write(ByteBuffer.wrap(message.getBytes()));
    }

    public static Future<Integer> sendInternalMessage(AsynchronousSocketChannel channel, String message) {
        System.out.println("Send internal: " + message);
        final String internalMessage = INTERNAL_MESSAGE + message;
        return channel.write(ByteBuffer.wrap(internalMessage.getBytes()));
    }

    public static Map<String, Integer> getRandomInstruments() {
        final Map<String, Integer> instruments = new HashMap<>();
        final Random random = new Random();
        for(String instrument : INSTRUMENTS) {
            if (random.nextBoolean()) {
                instruments.put(instrument, random.nextInt(9) + 1);
            }
        }
        return instruments;
    }

    public static String getClientName(String[] args) {
        return args.length == 1
                ? args[0]
                : DateTimeFormatter.ofPattern("mmss").format(LocalDateTime.now());
    }
}
