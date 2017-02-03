package com.datecs.examples.PrinterSample.network;

import java.net.Socket;

public interface PrinterServerListener {
    public void onConnect(Socket socket);
}
