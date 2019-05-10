package com.mrb.fixme.router;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.Utils;
import com.mrb.fixme.core.handler.MessageHandler;
import com.mrb.fixme.router.handler.ChecksumValidator;
import com.mrb.fixme.router.handler.MessageProcessor;

public class MessageRouter {

    private final AtomicInteger id = new AtomicInteger(Core.INITIAL_ID);
    private final Map<String, AsynchronousSocketChannel> routingTable = new ConcurrentHashMap<>();

    private void start() {
        System.out.println("Message Router turned ON");
        try {
            final MessageHandler messageHandler = new ChecksumValidator();
            messageHandler.setNext(new MessageProcessor(routingTable));

            final AsynchronousServerSocketChannel brokersListener = AsynchronousServerSocketChannel
                    .open()
                    .bind(new InetSocketAddress(Core.HOST_NAME, Core.BROKER_PORT));
            brokersListener.accept(null,
                    new ClientCompletionHandler(brokersListener, routingTable, Core.BROKER_NAME, id, messageHandler));

            final AsynchronousServerSocketChannel marketsListener = AsynchronousServerSocketChannel
                    .open()
                    .bind(new InetSocketAddress(Core.HOST_NAME, Core.MARKET_PORT));
            marketsListener.accept(null,
                    new ClientCompletionHandler(marketsListener, routingTable, Core.MARKET_NAME, id, messageHandler));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MessageRouter().start();
        int i = 0;
        while (true) {
            System.out.print('.');
            i++;
            if (i % 50 == 0) {
                System.out.println();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ClientCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

        private static final int EXECUTOR_THREADS = 5;

        private final ExecutorService executor = Executors.newFixedThreadPool(EXECUTOR_THREADS);
        private final AsynchronousServerSocketChannel clientListener;
        private final Map<String, AsynchronousSocketChannel> routingTable;
        private final String clientName;
        private final AtomicInteger id;
        private final MessageHandler messageHandler;

        private ClientCompletionHandler(AsynchronousServerSocketChannel clientListener, Map<String,AsynchronousSocketChannel> routingTable,
                                        String clientName, AtomicInteger id, MessageHandler messageHandler) {
            this.clientListener = clientListener;
            this.routingTable = routingTable;
            this.clientName = clientName;
            this.id = id;
            this.messageHandler = messageHandler;
        }

        @Override
        public void completed(AsynchronousSocketChannel channel, Object attachment) {
            clientListener.accept(null, this);

            final String currentId = getNextId();
            sendClientId(currentId, channel);

            final ByteBuffer buffer = ByteBuffer.allocate(Core.DEFAULT_BUFFER_SIZE);
            while (true) {
                final String message = Utils.readMessage(channel, buffer);
                if (Utils.EMPTY_MESSAGE.equals(message)) {
                    break;
                }
                executor.execute(() -> messageHandler.handle(channel, message));
            }
            endConnection(currentId);
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println(clientName + " connection failed");
        }

        private void sendClientId(String currentId, AsynchronousSocketChannel channel) {
            System.out.println();
            System.out.println(clientName + " connected, ID: " + currentId);
            Utils.sendMessage(channel, currentId);
            routingTable.put(currentId, channel);
            System.out.println(clientName + " routing table: " + routingTable.keySet().toString());
        }

        private void endConnection(String currentId) {
            System.out.println();
            routingTable.remove(currentId);
            System.out.println(clientName + " connection ended, Bye - #" + currentId);
            System.out.println(clientName + " routing table: " + routingTable.keySet().toString());
        }

        private String getNextId() {
            return String.format(Core.ID_FORMAT, id.getAndIncrement());
        }
    }
}
