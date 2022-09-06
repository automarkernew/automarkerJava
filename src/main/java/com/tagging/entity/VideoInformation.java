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
@Table(name = "video_information")

public class VideoInformation {
    @Id
    @Column(name = "VIDEO_ID")
    private String videoId;

    @Column(name = "VIDEO_FILE_URL")
    private String videoFileUrl;

    @Column(name = "VIDEO_NAME")
    private String videoName;

    @Column(name = "SHOOT_TIME")
    private String shootTime;

    @Column(name = "SHOOT_PLACE")
    private String shootPlace;

    @Column(name = "LENGTH")
    private String length;

    @Column(name = "SENSOR_TYPE")
    private String sensorType;

    @Column(name = "TYPE_LINK_ID") //新增
    private String typeLinkId;

    @Column(name = "TAG_USER_ID")
    private String tagUserId;

    @Column(name = "MOT_USER_ID")
    private String motUserId;

    @Column(name = "TAG_IMG_URL")
    private String tagImgUrl;

    @Column(name = "MOT_IMG_URL")
    private String motImgUrl;

    @Column(name = "HEIGHT")
    private String height;

    @Column(name = "WIDTH")
    private String width;

    @Column(name = "TAG_TIME")
    private String tagTime;

    @Column(name = "TAG_STATUS")
    private String tagStatus;

    @Column(name = "IS_TAGGED")
    private String isTagged;

    @Column(name = "IS_MOTED")
    private String isMoted;

    @Column(name = "offsetX")
    private String offsetX;

    @Column(name = "offsetY")
    private String offsetY;

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
