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
import java.util.concurrent.atomic.AtomicInteger;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.Utils;

public class MessageRouter {

    private static final AtomicInteger id = new AtomicInteger(1);
    private static final Map<String, AsynchronousSocketChannel> routingTable = new HashMap<>();

    private void start() {
        System.out.println("Message Router turned ON");
        try {
            final AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel
                    .open()
                    .bind(new InetSocketAddress(Core.HOST_NAME, Core.BROKER_PORT));

            listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel channel, Void attachment) {
                    listener.accept(null, this);

                    final String currentId = getId();
                    System.out.println("Someone connected, get ID: " + currentId);
                    Utils.sendMessage(channel, currentId);
                    routingTable.put(currentId, channel);
                    System.out.println("Routing table: " + routingTable.keySet().toString());

                    final ByteBuffer buffer = ByteBuffer.allocate(4096);
                    try {
                        String message = "message";
                        while (message.length() > 0) {
                            message = Utils.readMessage(channel, buffer);
                            processMessage(message);
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

    private static void processMessage(String message) {
        System.out.println();
        System.out.println("Processing message: " + message);
        String[] m = message.split(" ");
        if (m.length  == 2) {
            final String id = m[0];
            final String mess = m[1];
            System.out.println("id: " + id + ", message: " + mess);
            final AsynchronousSocketChannel channel = routingTable.get(id);
            Utils.sendMessage(channel, mess);
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

    private static String getId() {
        return String.valueOf(id.getAndIncrement());
    }
}
