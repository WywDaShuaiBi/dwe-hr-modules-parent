package com.dwsoft.webapp.hr.attend.timeservices;
import cn.hutool.core.date.DateTime;

import com.dwsoft.core.annotation.DesignedTimingJob;
import com.dwsoft.core.spring.TransactionHelper;
import com.dwsoft.core.tool.utils.DateUtil;
import com.dwsoft.webapp.hr.attend.api.command.clock.DeviceClockInParameter;

import com.dwsoft.webapp.hr.attend.api.exception.AttendanceIllegalArgumentException;
import com.dwsoft.webapp.hr.attend.dto.HrAttendData;
import com.dwsoft.webapp.hr.attend.engine.AttendanceEngine;
import com.dwsoft.webapp.hr.attend.entity.HrAttendStaffHistory;
import com.dwsoft.webapp.hr.attend.entity.QHrAttendStaffHistory;
import com.dwsoft.webapp.hr.attend.service.IHrAttendStaffHistoryService;
import com.dwsoft.webapp.hr.staff.entity.HrStaffInfo;
import com.dwsoft.webapp.hr.staff.entity.QHrStaffInfo;
import com.dwsoft.webapp.hr.staff.service.IHrStaffInfoService;
import com.dwsoft.webapp.sys.datasrc.ds.DSTemplate;
import com.dwsoft.webapp.sys.datasrc.ds.DataSet;
import com.dwsoft.webapp.sys.datasrc.ds.IDsAction;
import com.dwsoft.webapp.sys.org.entity.QSysOrgPerson;
import com.dwsoft.webapp.sys.org.entity.SysOrgPerson;
import com.dwsoft.webapp.sys.org.service.ISysOrgPersonService;
import com.dwsoft.webapp.sys.timing.interfaces.TimingJobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;

/**
 *
 *@author  王耀文
 * @since 2024/2/219:52
 *
 */
@Service
@DesignedTimingJob
public class HrAttendInfoData {
    private static final Logger logger = LoggerFactory.getLogger(HrAttendInfoData.class);

    @Autowired
    protected ISysOrgPersonService sysOrgPersonService;

    @Autowired
    protected IHrStaffInfoService hrStaffInfoService;

    @Autowired
    protected IHrAttendStaffHistoryService hrAttendStaffHistoryService;

    @Value("${dwsoft.yh.attend.ignoring_devices}")
    protected String ignoringDevices;

    private volatile boolean locked = false;

    private volatile boolean isRunning = false;

    QHrAttendStaffHistory qHrAttendStaffHistory=QHrAttendStaffHistory.hrAttendStaffHistory;

    QSysOrgPerson qSysOrgPerson = QSysOrgPerson.sysOrgPerson;

    QHrStaffInfo qHrStaffInfo = QHrStaffInfo.hrStaffInfo;

    @Autowired
    private ObjectProvider<AttendanceEngine> attendanceEngine;
    @Autowired
    private TransactionHelper transactionHelper;

    @DesignedTimingJob(jobTitle = "定时同步考勤打卡数据", cron = "0 0 0 * * ?")
    public void trigger(TimingJobContext context) throws Exception {
        if (isRunning == true) {
            context.logError("已经有一个任务正在运行了!");
            return;
        }

        isRunning = true;
        try {
            trigger0(context);
        }catch (Exception e) {
            context.logError(e);
        }finally {
            isRunning = false;
        }
    }

    private void trigger0(TimingJobContext context) throws Exception {
        context.logMessage("定时任务[更新基础信息]开始");
        Date dateTime=new Date();
        DateTime beginDate = cn.hutool.core.date.DateUtil.beginOfDay(dateTime);
        DateTime endDate = cn.hutool.core.date.DateUtil.endOfDay(dateTime);
        //因为要同步15天的数据,每次同步一天的数据
        for (int i=1;i<=15;i++){
            Date startDate = com.dwsoft.core.tool.utils.DateUtil.plusDays(beginDate, -i);
            Date endTime=com.dwsoft.core.tool.utils.DateUtil.plusDays(endDate, -i);
            addAllData(startDate,endTime);
        }

    }
    public void addAllData(Date fdClockInStartTime, Date fdClockEndInTime) throws Exception {
        String dataSource = "ATT_INFO";
        /* 获得15天前的开始时间*/

        String startTimeStr = DateUtil.format(fdClockInStartTime, DateUtil.PATTERN_DATETIME);
        String endTimeStr = DateUtil.format(fdClockEndInTime, DateUtil.PATTERN_DATETIME);

        DSTemplate.execute(dataSource, new IDsAction()  {
            @Override
            public Object doAction(DataSet dataSet) throws Exception {

                Integer count = 0;
                Map<String, Integer> succMap = new HashMap<String, Integer>();
                succMap.put("successCount", 0);
                Integer pageId = 0;
                Integer pageSize = 1000;
                Integer pageCount = 1000;

                String countSql = "SELECT count(1) as zs from CHECKINOUT WHERE CHECKTIME >= ? AND CHECKTIME <= ?;";
                dataSet.prepareStatement(countSql);
                PreparedStatement countpst = dataSet.getPreparedStatement();
                countpst.setTimestamp(1, new Timestamp(fdClockInStartTime.getTime()));
                countpst.setTimestamp(2, new Timestamp(fdClockEndInTime.getTime()));
                ResultSet countRs = countpst.executeQuery();
                if (countRs.next()) {
                    count = countRs.getInt("zs");
                }
                String sql = "select c.*,u.BADGENUMBER from CHECKINOUT c join USERINFO u on c.USERID=u.USERID WHERE c.CHECKTIME >= ? AND c.CHECKTIME <= ? ORDER BY c.CHECKTIME ASC offset ? rows fetch next ? rows only;";
                Map<String, List<HrAttendData>> map = new HashMap<>();
                do {
                    dataSet.prepareStatement(sql);
                    PreparedStatement pst = dataSet.getPreparedStatement();
                    pst.setTimestamp(1, new Timestamp(fdClockInStartTime.getTime()));
                    pst.setTimestamp(2, new Timestamp(fdClockEndInTime.getTime()));
                    pst.setInt(3, pageId * pageSize);
                    pst.setInt(4, pageCount);
                    ResultSet rs = pst.executeQuery();
                    boolean isEmpty = true;
                    while (rs.next()) {
                        isEmpty = false;
                        /*员工ID号*/
                        String userID = rs.getString("BADGENUMBER");

                        Date fdCheckInDate = new Date(rs.getTimestamp("CHECKTIME").getTime());
                        String sn = rs.getString("sn");
                        /*如果是食堂打卡机则不同步数据*/
                        if (null != ignoringDevices && ignoringDevices.indexOf(sn)>-1){
                            continue;
                        }
                        HrAttendData hrAttendData = new HrAttendData();
                        hrAttendData.setFdPersonId(userID);
                        hrAttendData.setFdClockInTime(fdCheckInDate);
                        hrAttendData.setFdAttendSn(sn);
                        List<HrAttendData> hrAttendList = map.get(userID);
                        if (hrAttendList == null) {
                            hrAttendList = new ArrayList<>();
                            map.put(userID, hrAttendList);
                        }
                        hrAttendList.add(hrAttendData);
                    }
                    if (isEmpty) {
                        break;
                    }
                    pageId++;
                } while (true);


                Iterator<String> it = map.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    List<HrAttendData> hrAttendDataList = map.get(key);
                    /*对人员打卡时间进行排序*/
                    hrAttendDataList.sort(new Comparator<HrAttendData>() {
                                              @Override
                                              public int compare(HrAttendData o1, HrAttendData o2) {
                                                  Date fdClockInTime = o1.getFdClockInTime();
                                                  Date fdClockInTime1 = o2.getFdClockInTime();
                                                  long time = fdClockInTime.getTime();
                                                  long time1 = fdClockInTime1.getTime();
                                                  if (time > time1) {
                                                      return 1;
                                                  } else if (time < time1) {
                                                      return -1;
                                                  } else {
                                                      return 0;
                                                  }
                                              }
                    });

                    SysOrgPerson sysOrgPerson = sysOrgPersonService.query(
                            qSysOrgPerson.fdJobnumber.eq(key) ,
                            qSysOrgPerson.fdIsAvailable.eq(true)
                    ).fetchFirst();

                    if (null != sysOrgPerson) {
                        HrStaffInfo hrStaffInfo = hrStaffInfoService.query(qHrStaffInfo.fdOaPerson.fdId.eq(sysOrgPerson.getFdId())).fetchFirst();
                        if(hrStaffInfo!=null){
                                for (HrAttendData hrAttendData :hrAttendDataList){
                                    try {
                                        addAttend(hrAttendData,hrStaffInfo);
                                    }catch (Exception e){
                                        logger.error("数据写入时发生", e);
                                        continue;
                                    }
                                }
                        } else {
                            /* System.out.println("未找到员工ID：" + key);*/
                            if (logger.isDebugEnabled()) {
                                logger.debug("未找到员工ID：" + key);
                            }
                        }
                    }
                        }

                if (logger.isDebugEnabled()) {
                    logger.debug(startTimeStr + " ~ " + endTimeStr + "打卡记录同步总数：" + count);
                }


                return null;
        }
        });
    }

    public void addAttend(HrAttendData hrAttendDatas,HrStaffInfo hrStaffInfo){
                DeviceClockInParameter clockInParameter = new DeviceClockInParameter();
                clockInParameter.setClockTime(DateUtil.fromDate(hrAttendDatas.getFdClockInTime()));
                    if (hrAttendDatas.getFdAttendSn().equals("FAC1235000521")){
                        clockInParameter.setDeviceName("1号楼左边考勤机");
                    } else if (hrAttendDatas.getFdAttendSn().equals("FAC1235000236")) {
                        clockInParameter.setDeviceName("1号楼中间考勤机");
                    }else if (hrAttendDatas.getFdAttendSn().equals("FAC1235000251")) {
                        clockInParameter.setDeviceName("1号楼右边考勤机");
                    }else if (hrAttendDatas.getFdAttendSn().equals("FAC1235000530")) {
                        clockInParameter.setDeviceName("生产考勤机");
                    }else if (hrAttendDatas.getFdAttendSn().equals("CJDE233961238")) {
                        clockInParameter.setDeviceName("主考勤机");
                    }
                clockInParameter.setDeviceId(hrAttendDatas.getFdAttendSn());
                clockInParameter.setStaffId(hrStaffInfo.getFdId());
                transactionHelper.withNewTransaction(() -> {
                attendanceEngine.getIfAvailable().getRuntimeService().createAttendClock(clockInParameter);
        });

    }
}
