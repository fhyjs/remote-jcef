package org.eu.hanana.reimu.lib.rjcef.common;

import org.cef.network.CefPostData;
import org.cef.network.CefRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PureJavaCefRequest extends CefRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private long identifier;
    private String url;
    private String method = "GET";

    private String referrerUrl;
    private ReferrerPolicy referrerPolicy = ReferrerPolicy.REFERRER_POLICY_DEFAULT;

    private CefPostData postData;

    private final Map<String, String> headers = new HashMap<>();

    private int flags;
    private String firstPartyForCookies;

    private ResourceType resourceType = ResourceType.RT_SUB_RESOURCE;
    private TransitionType transitionType = TransitionType.TT_LINK;

    private boolean readOnly = false;

    // ===================== 实现抽象方法 =====================

    @Override
    public void dispose() {
        // 无 native，无需释放
    }

    @Override
    public long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public void setURL(String url) {
        this.url = url;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public void setReferrer(String url, ReferrerPolicy policy) {
        this.referrerUrl = url;
        this.referrerPolicy = policy;
    }

    @Override
    public String getReferrerURL() {
        return referrerUrl;
    }

    @Override
    public ReferrerPolicy getReferrerPolicy() {
        return referrerPolicy;
    }

    @Override
    public CefPostData getPostData() {
        return postData;
    }

    @Override
    public void setPostData(CefPostData postData) {
        this.postData = postData;
    }

    @Override
    public String getHeaderByName(String name) {
        return headers.get(name);
    }

    @Override
    public void setHeaderByName(String name, String value, boolean overwrite) {
        if (!overwrite && headers.containsKey(name)) return;
        headers.put(name, value);
    }

    @Override
    public void getHeaderMap(Map<String, String> headerMap) {
        headerMap.putAll(headers);
    }

    @Override
    public void setHeaderMap(Map<String, String> headerMap) {
        headers.clear();
        headers.putAll(headerMap);
    }

    @Override
    public void set(String url, String method, CefPostData postData, Map<String, String> headerMap) {
        this.url = url;
        this.method = method;
        this.postData = postData;
        setHeaderMap(headerMap);
    }

    @Override
    public int getFlags() {
        return flags;
    }

    @Override
    public void setFlags(int flags) {
        this.flags = flags;
    }

    @Override
    public String getFirstPartyForCookies() {
        return firstPartyForCookies;
    }

    @Override
    public void setFirstPartyForCookies(String url) {
        this.firstPartyForCookies = url;
    }

    @Override
    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public TransitionType getTransitionType() {
        return transitionType;
    }

    public void setTransitionType(TransitionType transitionType) {
        this.transitionType = transitionType;
    }
}
