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
@Table(name = "frame_information")

public class FrameInformation {
    @Id
    @Column(name = "FRAME_INFORMATION_ID")
    private String frameInformationId;

    @Column(name = "VIDEO_ID")
    private String videoId;

    @Column(name = "FRAME")
    private Integer frame;

    @Column(name = "FRAME_TYPE")
    private String frameType;

    @Column(name = "TRACK_ID")
    private String trackId;

    @Column(name = "LEFT_UPPER_CORNER_ABSCISSA")
    private String leftUpperCornerAbscissa;

    @Column(name = "LEFT_UPPER_CORNER_ORDINATE")
    private String leftUpperCornerOrdinate;

    @Column(name = "RIGHT_LOWER_QUARTER_ABSCISSA")
    private String rightLowerQuarterAbscissa;

    @Column(name = "RIGHT_LOWER_QUARTER_ORDINATE")
    private String rightLowerQuarterOrdinate;

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
