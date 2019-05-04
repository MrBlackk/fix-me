package com.mrb.fixme.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class Client {

    private static final String FAKE_ID = "00-00-00";

    private final ByteBuffer buffer = ByteBuffer.allocate(Core.DEFAULT_BUFFER_SIZE);
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

    private void invalidateConnection() {
        socketChannel = null;
    }

    protected String getId() {
        return id;
    }

    protected void readFromSocket() {
        getSocketChannel().read(buffer, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                final String message = Utils.read(result, buffer);
                if (Utils.EMPTY_MESSAGE.equals(message)) {
                    System.out.println("Message router died! Have to reconnect somehow");
                    invalidateConnection();
                } else {
                    onSuccessRead(message);
                }
                getSocketChannel().read(buffer, null, this);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.println("Reading failed");
            }
        });
    }

    protected void onSuccessRead(String message) {
        // do nothing
    }
}
