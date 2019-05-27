package com.mrb.fixme.market.handler;

import com.mrb.fixme.core.Core;
import com.mrb.fixme.core.FixTag;
import com.mrb.fixme.core.MessageType;
import com.mrb.fixme.core.Utils;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

public class MessageExecutor extends MessageHandlerWithId {

    private final Map<String, Integer> instruments;

    public MessageExecutor(String clientId, String name, Map<String, Integer> instruments) {
        super(clientId, name);
        this.instruments = instruments;
    }

    @Override
    public void handle(AsynchronousSocketChannel clientChannel, String message) {
        final String instrument = Core.getFixValueByTag(message, FixTag.INSTRUMENT);
        if (instruments.containsKey(instrument)) {
            final int quantity = Integer.parseInt(Core.getFixValueByTag(message, FixTag.QUANTITY));
            final int marketQuantity = instruments.get(instrument);
            final String type = Core.getFixValueByTag(message, FixTag.TYPE);
            if (type.equals(MessageType.Buy.toString())) {
                if (marketQuantity < quantity) {
                    rejectedMessage(clientChannel, message, "Not enough instruments");
                    return;
                } else {
                    instruments.put(instrument, marketQuantity - quantity);
                }
            } else {
                instruments.put(instrument, marketQuantity + quantity);
            }
            System.out.println("Market instruments: " + instruments.toString());
            executedMessage(clientChannel, message, "OK");
            super.handle(clientChannel, message);
        } else {
            rejectedMessage(clientChannel, message, instrument + " instrument is not traded on the market");
        }
    }
}
