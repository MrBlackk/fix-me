package com.mrb.fixme.market;

import com.mrb.fixme.core.Client;
import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.Utils;
import com.mrb.fixme.core.handler.MessageHandler;
import com.mrb.fixme.market.handler.MarketTagsValidator;
import com.mrb.fixme.market.handler.MessageExecutor;

import java.util.Map;

public class Market extends Client {

    public static final String NAME_PREFIX = "M";
    private final Map<String, Integer> instruments;

    private Market(String name) {
        super(Core.MARKET_PORT, NAME_PREFIX + name);
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
        final MessageHandler tagsValidator = new MarketTagsValidator(getId(), getName());
        final MessageHandler messageExecutor = new MessageExecutor(getId(), getName(), instruments);
        messageHandler.setNext(tagsValidator);
        tagsValidator.setNext(messageExecutor);
        return messageHandler;
    }

    public static void main(String[] args) {
        new Market(Utils.getClientName(args)).start();
    }
}
