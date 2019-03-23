package com.mrb.fixme.router;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageRouter {

    private static AtomicInteger id = new AtomicInteger(100000);

    public static void main(String[] args) {
        System.out.println("Router turned ON");
        final String hostname = "127.0.0.1";
        final int port = 5000;

        try {
            final ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(hostname, port));

            final Selector selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.print("Waiting");
            int i = 0;
            while (true) {
                System.out.print('.');
                i++;
                if (i % 50 == 0) {
                    System.out.println();
                }
                selector.select(500);

                final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    System.out.println();
                    final SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        final String id = getId();
                        System.out.println("Accepting connection... With id - " + id);
                        final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        final SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }

                    if (key.isReadable()) {
                        final SocketChannel socketChannel = (SocketChannel) key.channel();
                        final ByteBuffer buffer = ByteBuffer.allocate(1024);
                        final int byteCount = socketChannel.read(buffer);
                        if (byteCount > 0) {
                            buffer.flip();

                            final String message = new String(Arrays.copyOf(buffer.array(), byteCount));
                            System.out.println("Got Message - '" + message + "'");

                            if (message.length() > 0) {
                                final ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes());
                                socketChannel.write(writeBuffer);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Router turned OFF");
    }

    private static String getId() {
        return String.valueOf(id.getAndIncrement());
    }
}
