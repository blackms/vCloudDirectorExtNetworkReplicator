package com.brenner;

import com.vmware.vcloud.api.rest.schema.NetworkConfigurationType;
import com.vmware.vcloud.api.rest.schema.ReferenceType;
import com.vmware.vcloud.api.rest.schema.TasksInProgressType;
import com.vmware.vcloud.api.rest.schema.extension.VMWExternalNetworkType;
import com.vmware.vcloud.api.rest.schema.extension.VimObjectRefType;
import com.vmware.vcloud.sdk.Task;
import com.vmware.vcloud.sdk.VCloudException;
import com.vmware.vcloud.sdk.VcloudClient;
import com.vmware.vcloud.sdk.admin.ExternalNetwork;
import com.vmware.vcloud.sdk.admin.extensions.VMWExternalNetwork;
import com.vmware.vcloud.sdk.constants.Version;
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
import java.util.Optional;
import java.util.concurrent.TimeoutException;

public class Main {
    private static VcloudClient client;

    public static void main(String[] args) throws VCloudException, TimeoutException {
        ArgumentParser parser = ArgumentParsers.newFor("BrennerComExtNetworks").build()
                .defaultHelp(true)
                .description("Create external networks on vCloud");
        parser.addArgument("-v", "--vcloud")
                .required(true)
                .help("vCloud URL");
        parser.addArgument("-u", "--username")
                .required(true)
                .help("vCloud Username");
        parser.addArgument("-p", "--password")
                .required(true)
                .help("vCloud Password");
        parser.addArgument("--vcenter")
                .required(true)
                .help("Name of the vCenter Server");
        parser.addArgument("--ext-net-filter")
                .required(false)
                .help("Filter only selected external networks")
                .setDefault("NO_FILTER");
        parser.addArgument("--suffix")
                .required(true)
                .help("Suffix to append to External Network Name");
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

        /* Connecting to vCloud Director... */
        try {
            client = Connect(
                    ns.getString("username"),
                    ns.getString("password"),
                    ns.getString("vcloud")
            );
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (VCloudException e) {
            System.out.println(String.format("Error during vCloud Director Connection. vCD: %s",
                    ns.getString("vcloud")));
            System.out.println(String.format("Error: %s", e.getMessage()));
            System.exit(1);
            e.printStackTrace();
        }

        ExternalNetworks externalNetworks = new ExternalNetworks(client);
        ArrayList<ExternalNetwork> extNets;

        String suffix = ns.getString("suffix");
        String vCenterName = ns.getString("vcenter");

        if (ns.getString("ext_net_filter").equals("NO_FILTER")) {
            extNets = externalNetworks.getExternalNetworks(
                    Optional.empty()
            );
        } else {
            extNets = externalNetworks.getExternalNetworks(
                    Optional.ofNullable(ns.getString("ext_net_filter"))
            );
        }
        for (ExternalNetwork extNet : extNets) {
            /* Reading existing informations */
            System.out.println(extNet);
            VMWExternalNetworkType vmwExternalNetworkType = new VMWExternalNetworkType();
            String newExternalNetworkName = String.format("%s%s", extNet.getResource().getName(), suffix);
            vmwExternalNetworkType.setName(newExternalNetworkName);

            /* Retrieve vCenter Objects Information */
            ReferenceType vimServerRef = externalNetworks.getVimServerRef(vCenterName);
            String portGroupName = "";
            PortGroup portGroup;
            portGroupName = newExternalNetworkName;
            portGroup = new PortGroup(client, portGroupName, suffix, vCenterName);
            VimObjectRefType vimObjRef = new VimObjectRefType();
            vimObjRef.setMoRef(portGroup.getMoRef());
            vimObjRef.setVimObjectType(portGroup.getPortGroupType());
            vimObjRef.setVimServerRef(vimServerRef);

            /* Clone the existent Network Configuration */
            NetworkConfigurationType networkConfiguration = extNet.getResource().getConfiguration();

            /* Set Configuration to Network Type */
            vmwExternalNetworkType.setConfiguration(networkConfiguration);
            vmwExternalNetworkType.setVimPortGroupRef(vimObjRef);

            /* Execute Creation Task in vCD */
            try {
                VMWExternalNetwork externalNetwork = client.getVcloudAdminExtension()
                        .createVMWExternalNetwork(vmwExternalNetworkType);
                Task externalNetworkTask = returnTask(externalNetwork);
                if (externalNetworkTask != null) {
                    externalNetworkTask.waitForTask(0);
                }
            } catch (VCloudException exc) {
                System.out.println("Failed to create extNet: " + extNet.getReference().getName());
            }
        }
    }

    /**
     * @param Username vCloud Director Administrative Username
     * @param Password vCloud Director Administrative Password
     * @param Url      vCloud Director UI
     * @throws UnrecoverableKeyException Cert Exception
     * @throws NoSuchAlgorithmException  Cert Exception
     * @throws KeyStoreException         Cert Exception
     * @throws KeyManagementException    Cert Exception
     * @throws VCloudException           Connection error.
     */
    private static VcloudClient Connect(String Username, String Password, String Url)
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException,
            VCloudException {
        VcloudClient client = new VcloudClient(Url, Version.V5_6);
        client.registerScheme("https", 443, FakeSSLSocketFactory.getInstance());
        client.login(Username, Password);
        return client;
    }


    /**
     * Check for tasks if any
     *
     * @param externalNetwork {@link VMWExternalNetwork}
     * @return {@link Task}
     * @throws VCloudException Error retrieving Tasks
     */
    private static Task returnTask(VMWExternalNetwork externalNetwork) throws VCloudException {
        TasksInProgressType tasksInProgress = externalNetwork.getResource()
                .getTasks();
        if (tasksInProgress != null)
            return tasksInProgress.getTask().stream().findFirst().map(
                    task -> new Task(client, task)
            ).orElse(null);
        return null;
    }
}
 