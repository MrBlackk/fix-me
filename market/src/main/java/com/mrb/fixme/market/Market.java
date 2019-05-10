package com.mrb.fixme.market;

import com.mrb.fixme.core.Client;
import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.handler.MessageHandler;
import com.mrb.fixme.market.handler.MessageExecutor;

public class Market extends Client {

    // todo: use chain of responsibility pattern for validating and executing messages(is valid message/checksum->check data->is available resources->could execute->etc.)
    // todo: rules - is instrument traded on this market (buy/sell) and is available quantity (buy) ? enough money on market to trade(sell)

    private Market() {
        super(Core.MARKET_PORT);
    }

    private void start() {
        System.out.println("Market turned ON");
        readFromSocket();

        while (true) ;
    }

    @Override
    protected void onSuccessRead(String message) {
        getMessageHandler().handle(getSocketChannel(), message);
    }

    private MessageHandler getMessageHandler() {
        return new MessageExecutor(getId());
    }

    public static void main(String[] args) {
        new Market().start();
    }
}
