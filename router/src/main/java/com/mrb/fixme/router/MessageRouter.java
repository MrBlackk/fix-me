package com.mrb.fixme.router;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.FixTag;
import com.mrb.fixme.core.Utils;

public class MessageRouter {

    // todo: 3 steps
    // 1. validate checksum
    // 2. identify destination by routing table
    // 3. forward message

    private final AtomicInteger id = new AtomicInteger(Core.INITIAL_ID);
    private final Map<String, AsynchronousSocketChannel> brokersRoutingTable = new HashMap<>();
    private final Map<String, AsynchronousSocketChannel> marketsRoutingTable = new HashMap<>();

    private void start() {
        System.out.println("Message Router turned ON");
        try {
            final AsynchronousServerSocketChannel brokersListener = AsynchronousServerSocketChannel
                    .open()
                    .bind(new InetSocketAddress(Core.HOST_NAME, Core.BROKER_PORT));
            brokersListener.accept(null, new ClientCompletionHandler(brokersListener, brokersRoutingTable, Core.BROKER_NAME, id) {
                @Override
                void processMessage(AsynchronousSocketChannel clientChannel, String message) {
                    processBrokerMessage(clientChannel, message);
                }
            });

            final AsynchronousServerSocketChannel marketsListener = AsynchronousServerSocketChannel
                    .open()
                    .bind(new InetSocketAddress(Core.HOST_NAME, Core.MARKET_PORT));
            marketsListener.accept(null, new ClientCompletionHandler(marketsListener, marketsRoutingTable, Core.MARKET_NAME, id) {
                @Override
                void processMessage(AsynchronousSocketChannel clientChannel, String message) {
                    System.out.println("Market back message: " + message);
                    final String brokerId = Core.getFixValueByTag(message, FixTag.TARGET_ID);
                    final AsynchronousSocketChannel targetChannel = brokersRoutingTable.get(brokerId);
                    if (targetChannel != null) {
                        Utils.sendMessage(targetChannel, message);
                    } else {
                        System.out.println("No broker???");
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //todo: reformat to real FIX notation
    private void processBrokerMessage(AsynchronousSocketChannel brokerChannel, String message) {
        System.out.println();
        System.out.println("Processing message: " + message);
        final String targetId = Core.getFixValueByTag(message, FixTag.TARGET_ID);
        final AsynchronousSocketChannel targetChannel = marketsRoutingTable.get(targetId);
        if (targetChannel != null) {
            Utils.sendMessage(targetChannel, message);
        } else {
            Utils.sendMessage(brokerChannel, "No client with such id: " + targetId);
            System.out.println("No client with such id: " + targetId);
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

    private abstract static class ClientCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

        private final ExecutorService executor = Executors.newFixedThreadPool(5);
        private final AsynchronousServerSocketChannel clientListener;
        private final Map<String, AsynchronousSocketChannel> routingTable;
        private final String clientName;
        private final AtomicInteger id;

        private ClientCompletionHandler(AsynchronousServerSocketChannel clientListener, Map<String, AsynchronousSocketChannel> routingTable,
                                        String clientName, AtomicInteger id) {
            this.clientListener = clientListener;
            this.routingTable = routingTable;
            this.clientName = clientName;
            this.id = id;
        }

        @Override
        public void completed(AsynchronousSocketChannel channel, Object attachment) {
            clientListener.accept(null, this);

            final String currentId = getNextId();
            sendClientId(currentId, channel);

            final ByteBuffer buffer = ByteBuffer.allocate(4096);
            while (true) {
                final String message = Utils.readMessage(channel, buffer);
                if (Utils.EMPTY_MESSAGE.equals(message)) {
                    break;
                }
                executor.execute(() -> processMessage(channel, message));
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
            return String.valueOf(id.getAndIncrement());
        }

        abstract void processMessage(AsynchronousSocketChannel clientChannel, String message);
    }
}
