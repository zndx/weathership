package org.zndx.wx.core;
/*
 * Copyright (C) 2018-2019 Lightbend Inc. <https://www.lightbend.com>
 */

import org.zndx.wx.api.EchoMessage;
import org.zndx.wx.api.EchoService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class EchoServiceImpl implements EchoService {

    @Override
    public CompletionStage<EchoMessage> echo(EchoMessage in) {
        return CompletableFuture.completedFuture(in);
    }
}
