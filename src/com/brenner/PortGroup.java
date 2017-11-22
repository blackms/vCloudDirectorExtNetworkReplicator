package com.brenner;

import com.vmware.vcloud.api.rest.schema.QueryResultPortgroupRecordType;
import com.vmware.vcloud.sdk.*;
import com.vmware.vcloud.sdk.constants.query.ExpressionType;
import com.vmware.vcloud.sdk.constants.query.FilterType;
import com.vmware.vcloud.sdk.constants.query.QueryPortgroupField;
import com.vmware.vcloud.sdk.constants.query.QueryRecordType;

import java.util.ArrayList;
import java.util.List;

public class PortGroup extends VCloudBaseObjectImpl {
    private String sourcePortGroupName;
    private String destinationPortGroupName;
    private String portGroupType;
    private String portGroupSuffix;
    public String moRef;

    PortGroup() {

    }

    PortGroup(VcloudClient client, String sourcePortGroupName, String portGroupSuffix) {
        super(client);
        this.setSourcePortGroupName(sourcePortGroupName);
        this.setPortGroupSuffix(portGroupSuffix);
    }

    public VcloudClient getClient() {
        return client;
    }

    public void setClient(VcloudClient client) {
        this.client = client;
    }

    public String getSourcePortGroupName() {
        return sourcePortGroupName;
    }

    public void setSourcePortGroupName(String sourcePortGroupName) {
        this.sourcePortGroupName = sourcePortGroupName;
    }

    public String getDestinationPortGroupName() {
        return destinationPortGroupName;
    }

    public void setDestinationPortGroupName(String destinationPortGroupName) {
        this.destinationPortGroupName = destinationPortGroupName;
    }

    public String getPortGroupType() {
        return portGroupType;
    }

    public void setPortGroupType(String portGroupType) {
        this.portGroupType = portGroupType;
    }

    public String getPortGroupSuffix() {
        return portGroupSuffix;
    }

    public void setPortGroupSuffix(String portGroupSuffix) {
        this.portGroupSuffix = portGroupSuffix;
    }

    public void setVimServerName(String vimServerName) {
        this.vimServerName = vimServerName;
    }

    public String getVimServerName() {
        return this.vimServerName;
    }

    public String getMoRef() throws VCloudException {
        Expression portGroupNameExpression = new Expression(
                QueryPortgroupField.NAME, this.sourcePortGroupName,
                ExpressionType.EQUALS
        );
        Expression vcNameExpression = new Expression(
                QueryPortgroupField.VCNAME, this.getVimServerName(),
                ExpressionType.EQUALS
        );

        List<Expression> expressions = new ArrayList<>();
        expressions.add(portGroupNameExpression);
        expressions.add(vcNameExpression);

        Filter filter = new Filter(FilterType.AND, expressions);

        QueryParams<QueryPortgroupField> queryParams = new QueryParams<>();
        queryParams.setFilter(filter);

        QueryService queryService = this.client.getQueryService();
        RecordResult<QueryResultPortgroupRecordType> portGroupResult = queryService
                .queryRecords(QueryRecordType.PORTGROUP, queryParams);
        if (portGroupResult.getRecords().size() > 0) {
            this.setMoRef(portGroupResult.getRecords().get(0).getMoref());
            this.setPortGroupType(portGroupType = portGroupResult.getRecords().get(0).getPortgroupType());
        } else {
            throw new VCloudException(String.format("Cannot retrieve PortGroup: %s", this.sourcePortGroupName));
        }
        return this.moRef;
    }

    public void setMoRef(String moRef) {
        this.moRef = moRef;
    }
}
