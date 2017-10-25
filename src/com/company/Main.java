package com.company;

import com.vmware.vcloud.api.rest.schema.*;
import com.vmware.vcloud.api.rest.schema.extension.VMWExternalNetworkType;
import com.vmware.vcloud.api.rest.schema.extension.VimObjectRefType;
import com.vmware.vcloud.sdk.*;
import com.vmware.vcloud.sdk.constants.FenceModeValuesType;
import com.vmware.vcloud.sdk.constants.Version;
import com.vmware.vcloud.sdk.constants.query.ExpressionType;
import com.vmware.vcloud.sdk.constants.query.FilterType;
import com.vmware.vcloud.sdk.constants.query.QueryPortgroupField;
import com.vmware.vcloud.sdk.constants.query.QueryRecordType;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static VcloudClient client;
    private static String portGroupType = "";

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("BrennerComExtNetworks").build()
                .defaultHelp(true)
                .description("Create external networks on vCloud");
        parser.addArgument("-v", "--vcloud")
                .help("vCloud URL");
        parser.addArgument("-u", "--username")
                .help("vCloud Username");
        parser.addArgument("-p", "--password")
                .help("vCloud Password");
        parser.addArgument("--vcenter")
                .help("vCenter URI");
        parser.addArgument("--vcenter-username")
                .help("vCenter Username");
        parser.addArgument("--vcenter-password")
                .help("vCenter Password");
        parser.addArgument("--whatif").action(Arguments.storeTrue())
                .setDefault(true)
                .required(false);
        parser.addArgument("--verbose").action(Arguments.storeTrue())
                .setDefault(false)
                .required(false);
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
    }

    public static void Connect(String Username, String Password, String Url)
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        VcloudClient client = new VcloudClient(Url, Version.V5_6);
        client.registerScheme("https", 443, FakeSSLSocketFactory.getInstance());
        try {
            client.login(Username, Password);
        } catch (VCloudException e) {
            System.out.println(String.format("Error durint vCloud Director Connection. vCD: %s", Url));
            System.out.println(String.format("Error: %s", e.getMessage()));
            System.exit(1);
        }
    }

    /**
     * Creates External Network
     *
     * @param vimServerRef {@link ReferenceType}
     * @param moRef        {@link String}
     * @return VMWExternalNetworkType
     * @throws VCloudException
     */
    private static VMWExternalNetworkType createExternalNetworkParams(
            ReferenceType vimServerRef, String moRef, String externalNetworkName)
            throws VCloudException {
        VMWExternalNetworkType vmwExternalNetworkType = new VMWExternalNetworkType();
        vmwExternalNetworkType.setName(externalNetworkName);
        vmwExternalNetworkType.setDescription("external network description");

        VimObjectRefType vimObjRef = new VimObjectRefType();
        vimObjRef.setMoRef(moRef);
        vimObjRef.setVimObjectType(portGroupType);
        vimObjRef.setVimServerRef(vimServerRef);

        // creating an isolated vapp network
        NetworkConfigurationType networkConfiguration = new NetworkConfigurationType();
        networkConfiguration.setFenceMode(FenceModeValuesType.ISOLATED.value());
        IpScopeType ipScope = new IpScopeType();
        ipScope.setNetmask("255.255.255.0");
        ipScope.setGateway("192.168.111.254");
        ipScope.setDns1("1.2.3.4");
        ipScope.setDnsSuffix("sample.vmware.com");
        ipScope.setIsInherited(false);

        IpScopesType ipScopes = new IpScopesType();
        ipScopes.getIpScope().add(ipScope);

        // IP Ranges
        IpRangesType ipRangesType = new IpRangesType();
        IpRangeType ipRangeType = new IpRangeType();
        ipRangeType.setStartAddress("192.168.111.1");
        ipRangeType.setEndAddress("192.168.111.19");
        ipRangesType.getIpRange().add(ipRangeType);
        ipScope.setIpRanges(ipRangesType);
        networkConfiguration.setIpScopes(ipScopes);
        vmwExternalNetworkType.setConfiguration(networkConfiguration);
        vmwExternalNetworkType.setVimPortGroupRef(vimObjRef);

        return vmwExternalNetworkType;
    }

    /**
     * Get Vim Server Reference
     *
     * @param vimServerName {@link String}
     * @return ReferenceType
     * @throws VCloudException
     */
    private static ReferenceType getVimServerRef(String vimServerName)
            throws VCloudException {
        return client.getVcloudAdminExtension().getVMWVimServerRefsByName()
                .get(vimServerName);
    }

    /**
     * Get Port Group Moref
     *
     * @param portGroup     {@link String}
     * @param vimServerName {@link String}
     * @return {@link String}
     * @throws VCloudException
     */
    private static String getPortGroupMoref(String portGroup,
                                            String vimServerName) throws VCloudException {
        String moref = "";

        Expression portGroupNameExpression = new Expression(
                QueryPortgroupField.NAME, portGroup, ExpressionType.EQUALS);
        Expression vcNameExpression = new Expression(
                QueryPortgroupField.VCNAME, vimServerName,
                ExpressionType.EQUALS);

        List<Expression> expressions = new ArrayList<Expression>();
        expressions.add(portGroupNameExpression);
        expressions.add(vcNameExpression);

        Filter filter = new Filter(FilterType.AND, expressions);

        QueryParams<QueryPortgroupField> queryParams = new QueryParams<QueryPortgroupField>();
        queryParams.setFilter(filter);

        QueryService queryService = client.getQueryService();
        RecordResult<QueryResultPortgroupRecordType> portGroupResult = queryService
                .queryRecords(QueryRecordType.PORTGROUP, queryParams);
        if (portGroupResult.getRecords().size() > 0) {
            moref = portGroupResult.getRecords().get(0).getMoref();
            portGroupType = portGroupResult.getRecords().get(0)
                    .getPortgroupType();
        } else {
            System.err.println("Port Group " + portGroup + " not found in vc "
                    + vimServerName);
        }

        return moref;
    }
}
