package com.brenner;

import com.vmware.vcloud.api.rest.schema.ReferenceType;
import com.vmware.vcloud.sdk.VCloudException;
import com.vmware.vcloud.sdk.VcloudClient;
import com.vmware.vcloud.sdk.admin.ExternalNetwork;
import com.vmware.vcloud.sdk.admin.VcloudAdmin;

import java.util.ArrayList;
import java.util.Collection;

public class ExternalNetworks {
    private VcloudClient client;

    ExternalNetworks() {

    }

    ExternalNetworks(VcloudClient client) {
        this.client = client;
    }

    public VcloudClient getClient() {
        return this.client;
    }

    public void setClient(VcloudClient client) {
        this.client = client;
    }

    private Collection<ReferenceType> getExternalNetworksRefs() throws VCloudException {
        VcloudAdmin admin = this.client.getVcloudAdmin();
        return admin.getExternalNetworkRefs();
    }

    public ArrayList<ExternalNetwork> getExternalNetworks() {
        ArrayList<ExternalNetwork> extNetList = new ArrayList<>();
        try {
            for (ReferenceType externalNetworkRef : this.getExternalNetworksRefs()) {
                ExternalNetwork extNetFinal;
                extNetFinal = ExternalNetwork.getExternalNetworkByReference(this.client, externalNetworkRef);
                extNetList.add(extNetFinal);
            }
        } catch (VCloudException e) {
            e.printStackTrace();
        }
        return extNetList;
    }
}
