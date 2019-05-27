package com.mrb.fixme.core;

import com.mrb.fixme.core.handler.ChecksumValidator;
import com.mrb.fixme.core.handler.InternalMessageHandler;
import com.mrb.fixme.core.handler.MandatoryTagsValidator;
import com.mrb.fixme.core.handler.MessageHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class Client {

    private static final String FAKE_ID = "000000";

    private final ByteBuffer buffer = ByteBuffer.allocate(Core.DEFAULT_BUFFER_SIZE);
    private final int port;
    private final String name;

    private AsynchronousSocketChannel socketChannel;
    private String id = FAKE_ID;

    public Client(int port, String name) {
        this.port = port;
        this.name = name;
    }

    protected AsynchronousSocketChannel getSocketChannel() {
        if (socketChannel == null) {
            socketChannel = connectToMessageRouter();
            Utils.sendMessage(socketChannel, name);
            id = Utils.readMessage(socketChannel, buffer);
            System.out.println(name + " ID: " + id);
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

    protected String getName() {
        return name;
    }

    protected void readFromSocket() {
        getSocketChannel().read(buffer, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                final String message = Utils.read(result, buffer);
                if (Utils.EMPTY_MESSAGE.equals(message)) {
                    System.out.println("Message router died! Have to reconnect");
                    invalidateConnection();
                } else {
                    onSuccessRead(message);
                }
                getSocketChannel().read(buffer, null, this);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.println("Message router died! Have to reconnect");
                invalidateConnection();
                getSocketChannel().read(buffer, null, this);
            }
        });
    }

    private void onSuccessRead(String message) {
        getMessageHandler().handle(getSocketChannel(), message);
    }

    protected MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = new InternalMessageHandler();
        final MessageHandler mandatoryTagsValidator = new MandatoryTagsValidator();
        final MessageHandler checksumValidator = new ChecksumValidator();
        messageHandler.setNext(mandatoryTagsValidator);
        mandatoryTagsValidator.setNext(checksumValidator);
        return messageHandler;
    }
}
