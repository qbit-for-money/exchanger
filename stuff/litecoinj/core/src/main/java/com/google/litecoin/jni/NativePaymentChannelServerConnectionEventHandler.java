package com.google.litecoin.jni;

import com.google.litecoin.core.*;
import com.google.litecoin.protocols.channels.PaymentChannelCloseException;
import com.google.litecoin.protocols.channels.ServerConnectionEventHandler;

import java.math.BigInteger;

/**
 * An event listener that relays events to a native C++ object. A pointer to that object is stored in
 * this class using JNI on the native side, thus several instances of this can point to different actual
 * native implementations.
 */
public class NativePaymentChannelServerConnectionEventHandler extends ServerConnectionEventHandler {
    public long ptr;

    @Override
    public native void channelOpen(Sha256Hash channelId);

    @Override
    public native void paymentIncrease(BigInteger by, BigInteger to);

    @Override
    public native void channelClosed(PaymentChannelCloseException.CloseReason reason);
}
