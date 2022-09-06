package com.tagging.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * system_config_param
 * @author
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "system_config_param")
public class SystemConfigParam implements Serializable {
    @Id
    @Column(name = "PARAM_ID")
    private String paramId;
    @Column(name = "PARAM_TYPE")
    private String paramType;
    @Column(name = "PARAM_CODE")
    private String paramCode;
    @Column(name = "PARAM_DESCRIPTION")
    private String paramDescription;
    @Column(name = "PARAM_SUB_CODE")
    private String paramSubCode;
    @Column(name = "PARAM_SUB_DESCRIPTION")
    private String paramSubDescription;
    @Column(name = "PARAM_CACHE_FLAG")
    private String paramCacheFlag;
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
