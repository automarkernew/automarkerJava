package com.tagging.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "mark_information")

public class MarkInformation {
    @Id
    @Column(name = "MARK_INFORMATION_ID")
    private String markInformationId;

    @Column(name = "MARK_INFORMATION_NAME")
    private String markInformationName;

    @Column(name = "MARK_INFORMATION_TYPE")
    private String markInformationType;

    @Column(name = "MARK_INFORMATION_FILE_URL")
    private String markInformationFileUrl;

    @Column(name = "MARK_INFORMATION_FILE_NAME")
    private String markInformationFileName;

    @Column(name = "CREATE_TIME")
    private String createTime;

    @Column(name = "DATA_SOURCE_ID")
    private String dataSourceId;

    @Column(name = "DATA_SOURCE_FILE_URL")
    private  String dataSourceFileUrl;

    @Column(name = "SPEC_CODE_01")
    private String specCode01;

    @Column(name = "SPEC_CODE_02")
    private String specCode02;

    @Column(name = "SPEC_CODE_03")
    private String specCode03;

    @Column(name = "CREATE_TIMESTAMP")
    private String createTimestamp;

    @Column(name = "UPDATE_TIMESTAMP")
    private String updateTimestamp;

    @Column(name = "DATA_STATUS")
    private String dataStatus;

    @Column(name = "VERSION")
    private Integer version;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "RESERVE")
    private String reserve;
}
