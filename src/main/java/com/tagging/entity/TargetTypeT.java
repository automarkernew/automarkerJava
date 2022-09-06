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
@Table(name = "target_type_t")
public class TargetTypeT {
    @Id
    @Column(name = "UNIQUE_ID")
    private String uniqueId;

    @Column(name = "LINK_ID") //新增字段
    private String linkId;

    @Column(name = "TARGET_TYPE_ID")
    private String targetTypeId;

    @Column(name = "TRACK_TYPE_ID")
    private String trackTypeId;

    @Column(name = "OBJECT_TYPE")
    private String ObjectType;

    @Column(name = "OBJECT_MODEL")
    private String ObjectModel;

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
