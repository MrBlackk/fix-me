package com.mrb.fixme.market;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Market {

    // todo: rewrite to nio2 socket
    // todo: use chain of responsibility pattern for validating and executing messages(is valid message/checksum->check data->is available resources->could execute->etc.)
    // todo: rules - is instrument traded on this market (buy/sell) and is available quantity (buy) ? enough money on market to trade(sell)

    public static void main(String[] args) {
        System.out.println("Market turned ON");
        final String serverHostname = "127.0.0.1";
        final int serverPort = 5001;

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
                        Thread.sleep(2000);

                        SelectionKey key = (SelectionKey) iterator.next();
                        iterator.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isConnectable()) {
                            if (connect(channel)) {
                                channel.register(selector, SelectionKey.OP_WRITE);
                                System.out.println("Market Connected, ready to send messages");
                                break;
                            }
                        }

                        if (key.isWritable()) {
                            final String message = "_##_MARKET_MESSAGE 0# " + (int)(Math.random() * 100 * Math.random() * 10);
                            final ByteBuffer byteMessage = ByteBuffer.wrap(message.getBytes());
                            try {
                                channel.write(byteMessage);
                                System.out.println("Sending - " + new String(byteMessage.array()));
                            } catch (IOException e) {
                                System.out.println("Connection LOST, you have to reconnect");
                            }
                        }
                    }
                }
            }

        } catch (ConnectException e) {
            System.out.println("Failed to connect to remote server.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Market turned OFF");
    }

    private static boolean connect(SocketChannel channel) throws IOException{
        while (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        return true;
    }
}
