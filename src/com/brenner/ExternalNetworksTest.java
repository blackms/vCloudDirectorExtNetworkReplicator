package com.brenner;

import com.vmware.vcloud.sdk.VcloudClient;
import com.vmware.vcloud.sdk.admin.ExternalNetwork;
import com.vmware.vcloud.sdk.constants.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;

public class ExternalNetworksTest {
    private VcloudClient client;

    @Before
    public void setUp() throws Exception {
        this.client = new VcloudClient("https://vcd-01a.corp.local", Version.V5_6);
        this.client.registerScheme("https", 443, FakeSSLSocketFactory.getInstance());
        this.client.login("administrator@System", "VMware1!");
    }

    @After
    public void tearDown() throws Exception {
        this.client.logout();
    }

    @Test
    public void getClient() throws Exception {
    }

    @Test
    public void setClient() throws Exception {
    }

    @Test
    public void getExternalNetworks() throws Exception {
        ExternalNetworks ext = new ExternalNetworks(this.client);
        ArrayList<ExternalNetwork> nets = ext.getExternalNetworks(Optional.empty());
        assertNotNull(nets);
    }
}