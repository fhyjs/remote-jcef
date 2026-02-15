package org.eu.hanana.reimu.lib.rjcef.common;

import org.cef.network.CefPostData;
import org.cef.network.CefPostDataElement;
import org.cef.network.CefRequest;

import java.io.Serializable;
import java.util.*;

public class CefRequestDTO implements Serializable {

    public long identifier;

    public String url;
    public String method;

    public String referrerUrl;
    public CefRequest.ReferrerPolicy referrerPolicy;

    public int flags;

    public String firstPartyForCookies;

    public CefRequest.ResourceType resourceType;
    public int transitionTypeValue;

    public Map<String, String> headers;

    public byte[] postData; // 只支持字节类型
    public static CefRequest toNative(CefRequestDTO dto) {
        CefRequest request = new PureJavaCefRequest();

        request.setURL(dto.url);
        request.setMethod(dto.method);
        request.setFlags(dto.flags);
        request.setFirstPartyForCookies(dto.firstPartyForCookies);

        if (dto.referrerUrl != null) {
            request.setReferrer(dto.referrerUrl, dto.referrerPolicy);
        }

        if (dto.headers != null) {
            request.setHeaderMap(dto.headers);
        }

        if (dto.postData != null) {
            CefPostData postData = CefPostData.create();
            CefPostDataElement element = CefPostDataElement.create();
            element.setToBytes(dto.postData.length, dto.postData);
            postData.addElement(element);
            request.setPostData(postData);
        }

        return request;
    }

    public static CefRequestDTO from(CefRequest request) {
        CefRequestDTO dto = new CefRequestDTO();

        dto.identifier = request.getIdentifier();
        dto.url = request.getURL();
        dto.method = request.getMethod();

        dto.referrerUrl = request.getReferrerURL();
        dto.referrerPolicy = request.getReferrerPolicy();

        dto.flags = request.getFlags();
        dto.firstPartyForCookies = request.getFirstPartyForCookies();

        dto.resourceType = request.getResourceType();
        dto.transitionTypeValue = request.getTransitionType().getValue();

        Map<String, String> headers = new HashMap<>();
        request.getHeaderMap(headers);
        dto.headers = headers;

        // 处理 postData
        CefPostData postData = request.getPostData();
        if (postData != null) {
            dto.postData = extractPostData(postData);
        }

        return dto;
    }
    private static byte[] extractPostData(CefPostData postData) {
        Vector<CefPostDataElement> elements = new Vector<>();
        postData.getElements(elements);
        if (elements.isEmpty()) return null;

        for (CefPostDataElement el : elements) {
            if (el.getType() == CefPostDataElement.Type.PDE_TYPE_BYTES) {
                int size = el.getBytesCount();
                byte[] data = new byte[size];
                el.getBytes(size, data);
                return data;
            }
        }
        return null;
    }

}
