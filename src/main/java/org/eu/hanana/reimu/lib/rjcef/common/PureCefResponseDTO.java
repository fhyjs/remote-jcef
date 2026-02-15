package org.eu.hanana.reimu.lib.rjcef.common;

import org.cef.handler.CefLoadHandler;
import org.cef.network.CefResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PureCefResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    public boolean readOnly;

    public int errorCode;          // CefLoadHandler.ErrorCode.ordinal()
    public int status;             // HTTP status

    public String statusText;
    public String mimeType;

    public Map<String, String> headers = new HashMap<>();

    public Map<String, String> getHeaders() {
        return headers;
    }

    public static PureCefResponseDTO fromNative(CefResponse response) {
        PureCefResponseDTO dto = new PureCefResponseDTO();

        dto.readOnly = response.isReadOnly();

        CefLoadHandler.ErrorCode error = response.getError();
        dto.errorCode = (error != null) ? error.ordinal() : 0;

        dto.status = response.getStatus();
        dto.statusText = response.getStatusText();
        dto.mimeType = response.getMimeType();

        Map<String, String> headers = new HashMap<>();
        response.getHeaderMap(headers);
        dto.headers = headers;

        return dto;
    }
    public static PureJavaCefResponse toNative(PureCefResponseDTO dto) {

        PureJavaCefResponse response = new PureJavaCefResponse();

        response.setStatus(dto.status);
        response.setStatusText(dto.statusText);
        response.setMimeType(dto.mimeType);

        if (dto.errorCode >= 0 &&
                dto.errorCode < CefLoadHandler.ErrorCode.values().length) {

            response.setError(
                    CefLoadHandler.ErrorCode.values()[dto.errorCode]
            );
        }

        if (dto.headers != null && !dto.headers.isEmpty()) {
            response.setHeaderMap(dto.headers);
        }

        return response;
    }
}
