package com.mrb.fixme.market;

import com.mrb.fixme.core.Client;
import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.FixTag;
import com.mrb.fixme.core.Utils;

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
        final String brokerId = Core.getFixValueByTag(message, FixTag.SOURCE_ID);
        final String resultMessage = Core.resultFixMessage("Executed", getId(), brokerId);
        Utils.sendMessage(getSocketChannel(), resultMessage);
    }

    public static void main(String[] args) {
        new Market().start();
    }
}
