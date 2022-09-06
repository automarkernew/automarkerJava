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
@Table(name = "target_track_t")
public class TargetTrackT {

    @Id
    @Column(name = "UNIQUE_ID")
    private String uniqueId;

    @Column(name = "LINK_ID") //新增字段
    private String linkId;

    @Column(name = "TRACK_ID")
    private String trackId;

    @Column(name = "VIDEO_ID")
    private String videoId;

    @Column(name = "TRACK_TYPE_ID")
    private String trackTypeId;

    @Column(name = "TARGET_TYPE_ID")
    private String targetTypeId;

    @Column(name = "COORDINATE_FILE_URL")
    private String coordinateFileUrl;

    @Column(name = "APPEAR_FRAME") //新增字段
    private String appearFrame;

    @Column(name = "MOT_IMG_URL")
    private String motImgUrl;

    @Column(name = "MARK_INFORMATION_SAVE_TIME")
    private String markInformationSaveTime;

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
