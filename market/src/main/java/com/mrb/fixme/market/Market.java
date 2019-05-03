package com.mrb.fixme.market;

import com.mrb.fixme.core.Client;
import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.Utils;

import java.nio.channels.*;

public class Market extends Client {

    // todo: use chain of responsibility pattern for validating and executing messages(is valid message/checksum->check data->is available resources->could execute->etc.)
    // todo: rules - is instrument traded on this market (buy/sell) and is available quantity (buy) ? enough money on market to trade(sell)

    private Market() {
        super(Core.MARKET_PORT);
    }

    private void start() {
        System.out.println("Market turned ON");
        getSocketChannel().read(getBuffer(), null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                final String message = Utils.read(result, getBuffer());
                if (Utils.EMPTY_MESSAGE.equals(message)) {
                    System.out.println("Message router died! Have to reconnect somehow");
                    invalidateConnection();
                }
                getSocketChannel().read(getBuffer(), null, this);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.println("Market reading failed");
            }
        });
        while (true) ;
    }

    public static void main(String[] args) {
        new Market().start();
    }
}
