package org.eu.hanana.reimu.lib.rjcef.common;

import org.cef.handler.CefLoadHandler;
import org.cef.network.CefResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PureJavaCefResponse extends CefResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean readOnly = false;

    private CefLoadHandler.ErrorCode errorCode = CefLoadHandler.ErrorCode.ERR_NONE;

    private int status = 200;
    private String statusText = "OK";
    private String mimeType = "text/plain";

    private final Map<String, String> headers = new HashMap<>();

    // =============================
    // native 无需释放
    // =============================
    @Override
    public void dispose() {
        // no-op
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public CefLoadHandler.ErrorCode getError() {
        return errorCode;
    }

    @Override
    public void setError(CefLoadHandler.ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String getStatusText() {
        return statusText;
    }

    @Override
    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getHeaderByName(String name) {
        return headers.get(name);
    }

    @Override
    public void setHeaderByName(String name, String value, boolean overwrite) {
        if (!overwrite && headers.containsKey(name)) {
            return;
        }
        headers.put(name, value);
    }

    @Override
    public void getHeaderMap(Map<String, String> map) {
        map.putAll(headers);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void setHeaderMap(Map<String, String> map) {
        headers.clear();
        if (map != null) {
            headers.putAll(map);
        }
    }
}
