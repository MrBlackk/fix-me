package com.mrb.fixme.market;

import com.mrb.fixme.core.Client;
import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.Utils;
import com.mrb.fixme.core.handler.MessageHandler;
import com.mrb.fixme.market.handler.MarketTagsValidator;
import com.mrb.fixme.market.handler.MessageExecutor;

import java.util.Map;

public class Market extends Client {

    private final Map<String, Integer> instruments;

    private Market() {
        super(Core.MARKET_PORT);
        instruments = Utils.getRandomInstruments();
    }

    private void start() {
        System.out.println("Market instruments: " + instruments.toString());
        readFromSocket();

        while (true) ;
    }

    @Override
    protected MessageHandler getMessageHandler() {
        final MessageHandler messageHandler = super.getMessageHandler();
        final MessageHandler tagsValidator = new MarketTagsValidator(getId());
        final MessageHandler messageExecutor = new MessageExecutor(getId(), instruments);
        messageHandler.setNext(tagsValidator);
        tagsValidator.setNext(messageExecutor);
        return messageHandler;
    }

    public static void main(String[] args) {
        new Market().start();
    }
}
