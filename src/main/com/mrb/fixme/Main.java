package com.mrb.fixme;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) {
        final String hostname = "127.0.0.1";
        final int port = 5000;

        try {
            final ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(hostname, port));

            final Selector selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select(100);

                final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    final SelectionKey key = iterator.next();
                    iterator.remove();
                    System.out.println("Key - " + key);

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        System.out.println("Accepting connection");
                        final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        final SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }

                    if (key.isReadable()) {
                        System.out.println("Reading connection");
                        final SocketChannel socketChannel = (SocketChannel) key.channel();
                        final ByteBuffer buffer = ByteBuffer.allocate(1024);
                        final int byteCount = socketChannel.read(buffer);
                        if (byteCount > 0) {
                            buffer.flip();

                            final String message = new String(buffer.array());
                            System.out.println("Message - '" + message + "'");

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
    }
}
