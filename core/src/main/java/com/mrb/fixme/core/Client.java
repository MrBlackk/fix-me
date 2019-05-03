package com.mrb.fixme.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class Client {

    private static final String FAKE_ID = "00-00-00";
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private final ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
    private final int port;

    private AsynchronousSocketChannel socketChannel;
    private String id = FAKE_ID;

    public Client(int port) {
        this.port = port;
    }

    protected AsynchronousSocketChannel getSocketChannel() {
        if (socketChannel == null) {
            socketChannel = connectToMessageRouter();
            id = Utils.readMessage(socketChannel, buffer);
            System.out.println("Current ID: " + id);
            return socketChannel;
        }
        return socketChannel;
    }

    private AsynchronousSocketChannel connectToMessageRouter() {
        final AsynchronousSocketChannel socketChannel;
        try {
            socketChannel = AsynchronousSocketChannel.open();
            final Future future = socketChannel.connect(new InetSocketAddress(Core.HOST_NAME, port));
            future.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            System.out.println("Could not connect to Message Router, reconnecting...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            return connectToMessageRouter();
        }
        return socketChannel;
    }

    protected void invalidateConnection() {
        socketChannel = null;
    }

    protected String getId() {
        return id;
    }

    protected ByteBuffer getBuffer() {
        return buffer;
    }
}
