package com.mrb.fixme.core;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Utils {

    public static String readMessage(AsynchronousSocketChannel channel, ByteBuffer readBuffer)
            throws InterruptedException, ExecutionException, TimeoutException {
        int bytesRead = channel.read(readBuffer).get(120, TimeUnit.SECONDS);
        if (bytesRead != -1) {
            readBuffer.flip();
            byte[] bytes = new byte[bytesRead];
            readBuffer.get(bytes, 0, bytesRead);
            readBuffer.clear();
            return new String(bytes);
        }
        return "";
    }
}
