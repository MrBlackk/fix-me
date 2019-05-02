package com.mrb.fixme.core;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Utils {

    public static String readMessage(AsynchronousSocketChannel channel, ByteBuffer readBuffer)
            throws InterruptedException, ExecutionException {
        return read(channel.read(readBuffer).get(), readBuffer);
    }

    public static String read(int bytesRead, ByteBuffer readBuffer) {
        if (bytesRead != -1) {
            readBuffer.flip();
            byte[] bytes = new byte[bytesRead];
            readBuffer.get(bytes, 0, bytesRead);
            readBuffer.clear();
            return new String(bytes);
        }
        return "";
    }

    public static Future<Integer> sendMessage(AsynchronousSocketChannel channel, String message) {
        System.out.println("Sending: " + message);
        return channel.write(ByteBuffer.wrap(message.getBytes()));
    }
}
