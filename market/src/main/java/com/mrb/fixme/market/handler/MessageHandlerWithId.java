package com.mrb.fixme.market.handler;

import com.mrb.fixme.core.handler.BaseMessageHandler;

public abstract class MessageHandlerWithId extends BaseMessageHandler {

    private final String clientId;

    public MessageHandlerWithId(String clientId) {
        this.clientId = clientId;
    }

    protected String getClientId() {
        return clientId;
    }
}
