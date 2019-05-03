package com.mrb.fixme.router;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.Utils;

public class MessageRouter {

    // todo: 3 steps
    // 1. validate checksum
    // 2. identify destination by routing table
    // 3. forward message

    private final AtomicInteger id = new AtomicInteger(1);
    private final Map<String, AsynchronousSocketChannel> routingTable = new HashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private void start() {
        System.out.println("Message Router turned ON");
        try {
            final AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel
                    .open()
                    .bind(new InetSocketAddress(Core.HOST_NAME, Core.BROKER_PORT));

            listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel brokerChannel, Void attachment) {
                    listener.accept(null, this);

                    final String currentId = getId();
                    System.out.println("Someone connected, get ID: " + currentId);
                    Utils.sendMessage(brokerChannel, currentId);
                    routingTable.put(currentId, brokerChannel);
                    System.out.println("Routing table: " + routingTable.keySet().toString());

                    final ByteBuffer buffer = ByteBuffer.allocate(4096);
                    try {
                        while (true) {
                            final String message = Utils.readMessage(brokerChannel, buffer);
                            if (message.length() == 0) {
                                break;
                            }
                            executor.execute(() -> processMessage(brokerChannel, message));
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    routingTable.remove(currentId);
                    System.out.println("Connection ended, Bye - #" + currentId);
                    System.out.println("Routing table: " + routingTable.keySet().toString());
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    System.out.println("Connection failed");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(AsynchronousSocketChannel brokerChannel, String message) {
        System.out.println();
        System.out.println("Processing message: " + message);
        String[] m = message.split(" ");
        if (m.length  == 2) {
            final String id = m[0];
            final String mess = m[1];
            System.out.println("id: " + id + ", message: " + mess);
            final AsynchronousSocketChannel targetChannel = routingTable.get(id);
            if (targetChannel != null) {
                Utils.sendMessage(targetChannel, mess);
            } else {
                Utils.sendMessage(brokerChannel, "No client with such id");
                System.out.println("No client with such id");
            }
        } else {
            System.out.println("Wrong message");
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

    private String getId() {
        return String.valueOf(id.getAndIncrement());
    }
}
