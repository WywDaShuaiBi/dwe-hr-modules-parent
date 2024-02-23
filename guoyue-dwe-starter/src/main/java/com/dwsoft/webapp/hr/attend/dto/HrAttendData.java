package com.dwsoft.webapp.hr.attend.dto;

import com.dwsoft.core.jpa.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author 王耀文
 * @since 2024/2/2110:00
 */
public class HrAttendData {
    private String fdPersonId;

    private String fdAttendSn;


    private Date fdClockInTime;

    public String getFdPersonId() {
        return fdPersonId;
    }

    public void setFdPersonId(String fdPersonId) {
        this.fdPersonId = fdPersonId;
    }

    public String getFdAttendSn() {
        return fdAttendSn;
    }

    public void setFdAttendSn(String fdAttendSn) {
        this.fdAttendSn = fdAttendSn;
    }

    public Date getFdClockInTime() {
        return fdClockInTime;
    }

    public void setFdClockInTime(Date fdClockInTime) {
        this.fdClockInTime = fdClockInTime;
    }


}
