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
import com.mrb.fixme.core.handler.InternalMessageHandler;
import com.mrb.fixme.core.handler.MandatoryTagsValidator;
import com.mrb.fixme.core.handler.MessageHandler;
import com.mrb.fixme.core.handler.ChecksumValidator;
import com.mrb.fixme.router.handler.MessageProcessor;

public class MessageRouter {

    private final AtomicInteger id = new AtomicInteger(Core.INITIAL_ID);
    private final Map<String, AsynchronousSocketChannel> routingTable = new ConcurrentHashMap<>();

    private void start() {
        System.out.println("Message Router turned ON");
        try {
            final MessageHandler messageHandler = getMessageHandler();

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

    private MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = new InternalMessageHandler();
        final MessageHandler mandatoryTagsValidator = new MandatoryTagsValidator();
        final MessageHandler checksumValidator = new ChecksumValidator();
        final MessageHandler messageParser = new MessageProcessor(routingTable);
        messageHandler.setNext(mandatoryTagsValidator);
        mandatoryTagsValidator.setNext(checksumValidator);
        checksumValidator.setNext(messageParser);
        return messageHandler;
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
        private final String clientType;
        private final AtomicInteger id;
        private final MessageHandler messageHandler;

        private ClientCompletionHandler(AsynchronousServerSocketChannel clientListener, Map<String,AsynchronousSocketChannel> routingTable,
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
            System.out.println(clientType + " connection failed");
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
}
