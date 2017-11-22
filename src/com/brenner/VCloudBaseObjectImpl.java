package com.brenner;

import com.vmware.vcloud.api.rest.schema.ReferenceType;
import com.vmware.vcloud.sdk.VCloudException;
import com.vmware.vcloud.sdk.VcloudClient;

public class VCloudBaseObjectImpl implements VCloudBaseObject {
    protected VcloudClient client;
    protected String vimServerName;

    VCloudBaseObjectImpl() {

    }

    VCloudBaseObjectImpl(VcloudClient client) {
        this.setClient(client);
    }

    public VcloudClient getClient() {
        return client;
    }

    public void setClient(VcloudClient client) {
        this.client = client;
    }


    /**
     * Get Vim Server Reference
     *
     * @param vimServerName {@link String}
     * @return ReferenceType
     * @throws VCloudException Cannot retrieve vimServer Name
     */
    @Override
    public ReferenceType getVimServerRef(String vimServerName) throws VCloudException {
        return this.client.getVcloudAdminExtension().getVMWVimServerRefsByName().get(vimServerName);
    }
}
