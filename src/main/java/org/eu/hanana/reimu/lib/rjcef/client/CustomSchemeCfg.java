package org.eu.hanana.reimu.lib.rjcef.client;

import lombok.RequiredArgsConstructor;
import org.eu.hanana.reimu.lib.rjcef.common.BufUtil;

@RequiredArgsConstructor
public class CustomSchemeCfg {
    public final String schemeName ;
    public final boolean isStandard ;;
    public final boolean isLocal ;
    public final boolean isDisplayIsolated;
    public final boolean isSecure;
    public final boolean isCorsEnabled ;
    public final boolean isCspBypassing;
    public final boolean isFetchEnabled;
}
