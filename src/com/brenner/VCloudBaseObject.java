package com.brenner;

import com.vmware.vcloud.api.rest.schema.ReferenceType;
import com.vmware.vcloud.sdk.VCloudException;

public interface VCloudBaseObject {
    ReferenceType getVimServerRef(String vimServerName) throws VCloudException;
}
