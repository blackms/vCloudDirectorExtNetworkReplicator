package com.brenner;

import com.vmware.vcloud.api.rest.schema.*;
import com.vmware.vcloud.api.rest.schema.extension.VMWExternalNetworkType;
import com.vmware.vcloud.api.rest.schema.extension.VimObjectRefType;
import com.vmware.vcloud.sdk.VCloudException;
import com.vmware.vcloud.sdk.VcloudClient;
import com.vmware.vcloud.sdk.admin.ExternalNetwork;
import com.vmware.vcloud.sdk.admin.VcloudAdmin;
import com.vmware.vcloud.sdk.constants.FenceModeValuesType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

class ExternalNetworks extends VCloudBaseObjectImpl {

    ExternalNetworks() {

    }

    ExternalNetworks(VcloudClient client) {
        super(client);
    }

        private Collection<ReferenceType> getExternalNetworksRefs() throws VCloudException {
        VcloudAdmin admin = this.client.getVcloudAdmin();
        return admin.getExternalNetworkRefs();
    }

    private Collection<ReferenceType> getExternalNetworksRefs(String extNetFilterName) throws VCloudException {
        VcloudAdmin admin = this.client.getVcloudAdmin();
        return admin.getExternalNetworkRefs().stream().filter(
                extNet -> extNet.getName().contains(extNetFilterName)).collect(
                Collectors.toList()
        );
    }

    /**
     * Creates External Network
     *
     * @param vimServerRef {@link ReferenceType}
     * @param moRef        {@link String}
     * @return VMWExternalNetworkType
     * @throws VCloudException Fail to perform the Task
     */
    private VMWExternalNetworkType _createExternalNetworkParams(
            ReferenceType vimServerRef,
            String moRef,
            String externalNetworkName,
            IpScopeType ipScope,
            IpRangeType ipRange,
            String description,
            FenceModeValuesType fenceMode
    ) throws VCloudException {
        VMWExternalNetworkType vmwExternalNetworkType = new VMWExternalNetworkType();
        vmwExternalNetworkType.setName(externalNetworkName);
        vmwExternalNetworkType.setDescription(description);

        VimObjectRefType vimObjRef = new VimObjectRefType();
        vimObjRef.setMoRef(moRef);
        String portGroupType = "";
        vimObjRef.setVimObjectType(portGroupType);
        vimObjRef.setVimServerRef(vimServerRef);

        // creating an isolated vapp network
        NetworkConfigurationType networkConfiguration = new NetworkConfigurationType();
        networkConfiguration.setFenceMode(fenceMode.value());

        IpScopesType ipScopes = new IpScopesType();
        ipScopes.getIpScope().add(ipScope);

        // IP Ranges
        IpRangesType ipRangesType = new IpRangesType();
        ipRangesType.getIpRange().add(ipRange);
        ipScope.setIpRanges(ipRangesType);
        networkConfiguration.setIpScopes(ipScopes);
        vmwExternalNetworkType.setConfiguration(networkConfiguration);
        vmwExternalNetworkType.setVimPortGroupRef(vimObjRef);

        return vmwExternalNetworkType;
    }

    /**
     * @param filter {@link String} Optional: String representing filter for ExtNet
     * @return {@link ArrayList<ExternalNetwork>} Array of the retrieved Networks
     * @throws VCloudException Fails to get External Networks
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    ArrayList<ExternalNetwork> getExternalNetworks(Optional<String> filter) throws VCloudException {
        ArrayList<ExternalNetwork> extNetList = new ArrayList<>();
        Collection<ReferenceType> externalNetworksRefs;
        if (filter.isPresent()) {
            externalNetworksRefs = this.getExternalNetworksRefs(filter.get());
        } else {
            externalNetworksRefs = this.getExternalNetworksRefs();
        }
        try {
            for (ReferenceType externalNetworkRef : externalNetworksRefs) {
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
