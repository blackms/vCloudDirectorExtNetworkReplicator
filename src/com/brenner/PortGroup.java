package com.brenner;

import com.vmware.vcloud.api.rest.schema.QueryResultPortgroupRecordType;
import com.vmware.vcloud.sdk.*;
import com.vmware.vcloud.sdk.constants.query.ExpressionType;
import com.vmware.vcloud.sdk.constants.query.FilterType;
import com.vmware.vcloud.sdk.constants.query.QueryPortgroupField;
import com.vmware.vcloud.sdk.constants.query.QueryRecordType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PortGroup extends VCloudBaseObjectImpl {
    private String sourcePortGroupName;
    private String destinationPortGroupName;
    private String portGroupType;
    private String portGroupSuffix;
    private String moRef;

    PortGroup(VcloudClient client, String sourcePortGroupName, String portGroupSuffix, String vimServerName)
            throws VCloudException {
        super(client);
        this.setSourcePortGroupName(sourcePortGroupName);
        this.setPortGroupSuffix(portGroupSuffix);
        this.setDestinationPortGroupName(this.getSourcePortGroupName() + this.getPortGroupSuffix());
        this.setVimServerName(vimServerName);

        /* Loading all the data from the API */
        RecordResult<QueryResultPortgroupRecordType> queryResult = doQuery();
        this.setPortGroupType(queryResult.getRecords().get(0).getPortgroupType());
        this.setMoRef(queryResult.getRecords().get(0).getMoref());
    }

    public VcloudClient getClient() {
        return client;
    }

    public void setClient(VcloudClient client) {
        this.client = client;
    }

    private String getSourcePortGroupName() {
        return sourcePortGroupName;
    }

    private void setSourcePortGroupName(String sourcePortGroupName) {
        this.sourcePortGroupName = sourcePortGroupName;
    }

    public String getDestinationPortGroupName() {
        return destinationPortGroupName;
    }

    private void setDestinationPortGroupName(String destinationPortGroupName) {
        this.destinationPortGroupName = destinationPortGroupName;
    }

    @SuppressWarnings("unchecked")
    private RecordResult<QueryResultPortgroupRecordType> doQuery() throws VCloudException {
        /* Build Expression for filter */
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

        /* Create the filter adding the Expression previously made */
        Filter filter = new Filter(FilterType.AND, expressions);

        QueryParams<QueryPortgroupField> queryParams = new QueryParams<>();
        queryParams.setFilter(filter);

        /* Executing the Query against queryService */
        QueryService queryService = client.getQueryService();
        return (RecordResult<QueryResultPortgroupRecordType>) queryService
                .queryRecords(QueryRecordType.PORTGROUP, queryParams);
    }

    String getPortGroupType() throws VCloudException {
        if (Objects.nonNull(portGroupType)) {
            return portGroupType;
        }
        RecordResult<QueryResultPortgroupRecordType> portGroupQueried = doQuery();
        return portGroupQueried.getRecords().get(0).getPortgroupType();
    }

    private void setPortGroupType(String portGroupType) {
        this.portGroupType = portGroupType;
    }

    private String getPortGroupSuffix() {
        return portGroupSuffix;
    }

    private void setPortGroupSuffix(String portGroupSuffix) {
        this.portGroupSuffix = portGroupSuffix;
    }

    public void setVimServerName(String vimServerName) {
        this.vimServerName = vimServerName;
    }

    private String getVimServerName() {
        return this.vimServerName;
    }

    String getMoRef() throws VCloudException {
        if (Objects.nonNull(moRef)) {
            return moRef;
        }

        /* We don't have retrieved the moRef yet, get it and set it */
        RecordResult<QueryResultPortgroupRecordType> portGroupQueried = doQuery();
        if (portGroupQueried.getRecords().size() > 0) {
            this.setMoRef(portGroupQueried.getRecords().get(0).getMoref());
        } else {
            throw new VCloudException(String.format("Cannot retrieve PortGroup: %s", this.sourcePortGroupName));
        }

        /* Finally return the moRef object */
        return this.moRef;
    }

    private void setMoRef(String moRef) {
        this.moRef = moRef;
    }
}
