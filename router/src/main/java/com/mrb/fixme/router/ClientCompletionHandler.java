package com.mrb.fixme.router;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.Utils;
import com.mrb.fixme.core.handler.MessageHandler;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

class ClientCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

    private static final int EXECUTOR_THREADS = 5;

    private final ExecutorService executor = Executors.newFixedThreadPool(EXECUTOR_THREADS);
    private final AsynchronousServerSocketChannel clientListener;
    private final Map<String, AsynchronousSocketChannel> routingTable;
    private final String clientType;
    private final AtomicInteger id;
    private final MessageHandler messageHandler;

    ClientCompletionHandler(AsynchronousServerSocketChannel clientListener, Map<String, AsynchronousSocketChannel> routingTable,
                            String clientType, AtomicInteger id, MessageHandler messageHandler) {
        this.clientListener = clientListener;
        this.routingTable = routingTable;
        this.clientType = clientType;
        this.id = id;
        this.messageHandler = messageHandler;
    }

    @Override
    public void completed(AsynchronousSocketChannel channel, Object attachment) {
        clientListener.accept(null, this);
        final ByteBuffer buffer = ByteBuffer.allocate(Core.DEFAULT_BUFFER_SIZE);
        final String name = Utils.readMessage(channel, buffer);

        final String currentId = getNextId();
        sendClientId(channel, currentId, name);

        while (true) {
            final String message = Utils.readMessage(channel, buffer);
            if (Utils.EMPTY_MESSAGE.equals(message)) {
                break;
            }
            executor.execute(() -> messageHandler.handle(channel, message));
        }
        endConnection(currentId, name);
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        System.out.println(clientType + " connection ended");
    }

    private void sendClientId(AsynchronousSocketChannel channel, String currentId, String name) {
        System.out.println();
        System.out.println(clientType + " " + name + " connected, ID: " + currentId);
        Utils.sendMessage(channel, currentId);
        routingTable.put(name, channel);
        System.out.println("Routing table: " + routingTable.keySet().toString());
    }

    private void endConnection(String currentId, String name) {
        System.out.println();
        routingTable.remove(name);
        System.out.println(clientType + " " + name + " connection ended, Bye - #" + currentId);
        System.out.println("Routing table: " + routingTable.keySet().toString());
    }

    private String getNextId() {
        return String.format(Core.ID_FORMAT, id.getAndIncrement());
    }
}
