package org.eu.hanana.reimu.lib.rjcef.client;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefCallback;
import org.cef.callback.CefResourceReadCallback;
import org.cef.callback.CefResourceSkipCallback;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefResourceHandler;
import org.cef.handler.CefResourceHandlerAdapter;
import org.cef.misc.BoolRef;
import org.cef.misc.IntRef;
import org.cef.misc.LongRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;
import org.eu.hanana.reimu.lib.rjcef.common.BufUtil;
import org.eu.hanana.reimu.lib.rjcef.common.CallbackResult;
import org.eu.hanana.reimu.lib.rjcef.common.CefRequestDTO;
import org.eu.hanana.reimu.lib.rjcef.common.PureCefResponseDTO;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.eu.hanana.reimu.lib.rjcef.common.CefUtil.getKeyByValue;

@RequiredArgsConstructor
public class NetCefSchemeHandlerFactory implements CefSchemeHandlerFactory {
    public final String uuid;
    public final ClientMain client;
    public final String schemeName;

    @Override
    public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {
        System.out.println("NetCefSchemeHandlerFactory#create "+schemeName);
        var bb = client.cnc.channel.alloc().directBuffer();
        BufUtil.writeString(client.remoteCommands.NetCefSchemeHandlerFactory_create,bb);
        BufUtil.writeString(uuid,bb);//Factory uuid
        BufUtil.writeString(getKeyByValue(client.cefClientMap,browser.getClient()),bb);//client uuid
        BufUtil.writeString(getKeyByValue(client.browserMCHashMap.get(getKeyByValue(client.cefClientMap,browser.getClient())),((CefBrowserMC) browser)),bb);//browser uuid

        var rqUUid = UUID.randomUUID().toString();
        BufUtil.writeString(rqUUid,bb);// handler uuid
        BufUtil.writeString(schemeName,bb);

        AtomicReference<CallbackResult> cr = new AtomicReference<>();
        BufUtil.sendPacketWithCallback(bb, client.cnc.channel, tuple -> {
            cr.set(new CallbackResult(tuple.a()));
        },client);
        while (cr.get()==null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!cr.get().success){
            try {
                throw cr.get().throwable;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return new CefResourceHandlerAdapter() {
            @SneakyThrows
            @Override
            public boolean processRequest(CefRequest request, CefCallback callback) {
                System.out.println("CefResourceHandlerAdapter#processRequest "+schemeName);

                var bb = client.cnc.channel.alloc().directBuffer();
                BufUtil.writeString(client.remoteCommands.CefResourceHandlerAdapter_processRequest,bb);
                BufUtil.writeString(uuid,bb);
                BufUtil.writeString(rqUUid,bb);

                BufUtil.writeBytes(BufUtil.toBytes(CefRequestDTO.from(request)),bb);

                BufUtil.sendPacketWithCallback(bb, client.cnc.channel, tuple -> {
                    var cr = new CallbackResult(tuple.a());
                    if (cr.success&&cr.result.readBoolean()) {
                        System.out.println("CefResourceHandlerAdapter#processRequest Continue "+schemeName);
                        callback.Continue();
                    }else {
                        System.out.println("CefResourceHandlerAdapter#processRequest cancel "+schemeName);
                        callback.cancel();
                    }
                },client);

                return true;
            }

            @Override
            public void cancel() {
                System.out.println("CefResourceHandlerAdapter#cancel "+schemeName);
                var bb = client.cnc.channel.alloc().directBuffer();
                BufUtil.writeString(client.remoteCommands.CefResourceHandlerAdapter_cancel,bb);
                BufUtil.writeString(uuid,bb);
                BufUtil.writeString(rqUUid,bb);
                client.cnc.channel.writeAndFlush(bb);
                super.cancel();
            }

            @Override
            public boolean skip(long bytesToSkip, LongRef bytesSkipped, CefResourceSkipCallback callback) {
                System.out.println("CefResourceHandlerAdapter#skip "+schemeName);
                return super.skip(bytesToSkip, bytesSkipped, callback);
            }

            @Override
            public boolean readResponse(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefCallback callback) {
                System.out.println("CefResourceHandlerAdapter#readResponse "+schemeName);

                var bb = client.cnc.channel.alloc().directBuffer();
                BufUtil.writeString(client.remoteCommands.CefResourceHandlerAdapter_readResponse,bb);

                BufUtil.writeString(uuid,bb);
                BufUtil.writeString(rqUUid,bb);

                bb.writeInt(bytesToRead);

                AtomicReference<CallbackResult> cr = new AtomicReference<>();
                BufUtil.sendPacketWithCallback(bb, client.cnc.channel, tuple -> {
                    cr.set(new CallbackResult(tuple.a()));
                },client);
                while (cr.get()==null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (!cr.get().success){
                    try {
                        throw cr.get().throwable;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                byte[] bytes = BufUtil.readBytes(cr.get().result);
                System.arraycopy(bytes,0,dataOut,0,bytes.length);
                bytesRead.set(cr.get().result.readInt());
                return cr.get().result.readBoolean();

            }

            @Override
            public boolean read(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefResourceReadCallback callback) {
                System.out.println("CefResourceHandlerAdapter#read "+schemeName);
                return super.read(dataOut, bytesToRead, bytesRead, callback);
            }

            @SneakyThrows
            @Override
            public void getResponseHeaders(CefResponse response, IntRef responseLength, StringRef redirectUrl) {
                System.out.println("CefResourceHandlerAdapter#getResponseHeaders "+schemeName);
                super.getResponseHeaders(response, responseLength, redirectUrl);

                var bb = client.cnc.channel.alloc().directBuffer();
                BufUtil.writeString(client.remoteCommands.CefResourceHandlerAdapter_getResponseHeaders,bb);

                BufUtil.writeString(uuid,bb);
                BufUtil.writeString(rqUUid,bb);

                BufUtil.writeBytes(BufUtil.toBytes(PureCefResponseDTO.fromNative(response)),bb);

                AtomicReference<CallbackResult> cr = new AtomicReference<>();
                BufUtil.sendPacketWithCallback(bb, client.cnc.channel, tuple -> {
                    cr.set(new CallbackResult(tuple.a()));
                },client);
                while (cr.get()==null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (!cr.get().success){
                    try {
                        throw cr.get().throwable;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                responseLength.set(cr.get().result.readInt());
                redirectUrl.set(BufUtil.readString(cr.get().result));
                var newReponse = PureCefResponseDTO.toNative(BufUtil.fromBytes(BufUtil.readBytes(cr.get().result), PureCefResponseDTO.class));
                response.setError(newReponse.getError());
                response.setHeaderMap(newReponse.getHeaders());
                response.setStatus(newReponse.getStatus());
                response.setStatusText(newReponse.getStatusText());
                response.setMimeType(newReponse.getMimeType());

                System.out.println("CefResourceHandlerAdapter#getResponseHeaders Finish"+newReponse.getStatus());
            }

            @Override
            public boolean open(CefRequest request, BoolRef handleRequest, CefCallback callback) {
                System.out.println("CefResourceHandlerAdapter#open "+schemeName);
                return super.open(request, handleRequest, callback);
            }

            @Override
            protected void finalize() throws Throwable {
                var bb = client.cnc.channel.alloc().directBuffer();
                BufUtil.writeString(client.remoteCommands.CefResourceHandlerAdapter_finalize,bb);
                BufUtil.writeString(uuid,bb);
                BufUtil.writeString(rqUUid,bb);
                client.cnc.channel.writeAndFlush(bb);
                super.finalize();
            }
        };
    }
}
