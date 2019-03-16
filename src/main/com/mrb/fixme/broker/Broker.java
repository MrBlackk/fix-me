package com.mrb.fixme.broker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Broker {

    public static void main(String[] args) {
        System.out.println("Broker turned ON");
        final String serverHostname = "127.0.0.1";
        final int serverPort = 5000;

        final InetSocketAddress serverAddress = new InetSocketAddress(serverHostname, serverPort);
        try {
            final Selector selector = Selector.open();
            final SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(serverAddress);

            channel.register(selector, SelectionKey.OP_CONNECT);
            while (true) {
                if (selector.select() > 0) {
                    final Iterator iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = (SelectionKey) iterator.next();
                        iterator.remove();
                        if (key.isConnectable()) {
                            if (connect(channel)) {
                                System.out.println("Connected, ready to send messages");
                                channel.register(selector, SelectionKey.OP_WRITE);
                                break;
                            }
                        }

                        if (key.isWritable()) {
                            System.out.println("write");
                            channel.write(ByteBuffer.wrap("BROKER_MESSAGE".getBytes()));
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("Broker turned OFF");
    }

    private static boolean connect(SocketChannel channel) throws IOException{
        while (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        return true;
    }
}
