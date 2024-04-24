package com.dwsoft.webapp.hr.attend.report;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dwsoft.core.jpa.support.PageQueryParams;
import com.dwsoft.core.jpa.support.QueryField;
import com.dwsoft.core.tool.utils.DateUtil;
import com.dwsoft.core.tool.utils.StringUtil;
import com.dwsoft.webapp.hr.attend.engine.rule.rules.byday.AttendResultByDay;
import com.dwsoft.webapp.hr.attend.handler.ExcelMergeUtil;
import com.dwsoft.webapp.hr.attend.handler.WriteHandlerStrategy;
import com.dwsoft.webapp.hr.attend.service.IHrAttendReportService;
import com.dwsoft.webapp.hr.staff.vo.simple.HrStaffInfoSimpleVo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Aspect
@Component
public class HrAttendReportControllerAspect {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    protected IHrAttendReportService hrAttendReportService;

    @Pointcut("execution(* com.dwsoft.webapp.hr.attend.controller.HrAttendReportController.exportExcel(..))")
    public void exportExcel() {
        //exportExcel
    }

    @Around("exportExcel()")
    public Object exportExcelAroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = (HttpServletRequest) pjp.getArgs()[0];
        HttpServletResponse response = (HttpServletResponse) pjp.getArgs()[1];
        PageQueryParams pageQueryParams = (PageQueryParams) pjp.getArgs()[2];

        personnelExportExcel(request, response, pageQueryParams);
        return null;
    }


    public void personnelExportExcel(HttpServletRequest request, HttpServletResponse response,
                                     @RequestBody PageQueryParams pageQueryParams) {
        List<List<String>> heads = new ArrayList<>();
        List<String> head = new ArrayList<>();
        List<String> keyList = new ArrayList<>();
        head.add("部门");
        head.add("部门");
        heads.add(head);

        head = new ArrayList<>();
        head.add("序号");
        head.add("序号");
        heads.add(head);

        head = new ArrayList<>();
        head.add("姓名");
        head.add("姓名");
        heads.add(head);


        head = new ArrayList<>();
        head.add("时间");
        head.add("时间");
        heads.add(head);

        List<QueryField> queries = pageQueryParams.getQueries();
        String begin = queries.get(0).getValue()[0];
        String end = queries.get(0).getValue()[1];
        Date startDate = DateUtil.parse(begin, "yyyy-MM-dd");
        Date endDate = DateUtil.parse(end, "yyyy-MM-dd");
        Long startSecond=startDate.getTime();
        Long endSecond=endDate.getTime();
        int countDay= (int) ((endSecond-startSecond)/(1000*3600*24)+1);
        for (int i=0;i<countDay;i++){
            head = new ArrayList<>();
            DayOfWeek dayOfWeek = DateUtil.fromDate(startDate).getDayOfWeek();
            int week=dayOfWeek.getValue();
            String strWeek;
            switch (week){
                case 1:
                    strWeek="一";
                    break;
                case 2:
                    strWeek="二";
                    break;
                case 3:
                    strWeek="三";
                    break;
                case 4:
                    strWeek="四";
                    break;
                case 5:
                    strWeek="五";
                    break;
                case 6:
                    strWeek="六";
                    break;
                default:
                    strWeek="日";
            }
            head.add(startDate.getDate()+"");
            head.add(strWeek+"");
            heads.add(head);
            String dateKey = DateUtil.format(startDate, "yyyyMMdd");
            keyList.add("AttendResultByDay_"+dateKey);

            startDate=DateUtil.plusDays(startDate,1);
        }

        head = new ArrayList<>();
        head.add("旷工");
        head.add("旷工");
        heads.add(head);
        keyList.add("AbsentDays");

        head = new ArrayList<>();
        head.add("事假");
        head.add("事假");
        heads.add(head);
        keyList.add("18dfd7813eac364231d9e87406881bac");

        head = new ArrayList<>();
        head.add("病假");
        head.add("病假");
        heads.add(head);
        keyList.add("18dfd7813fb4b18b54ea6c84124b2e1a");

        head = new ArrayList<>();
        head.add("育儿假");
        head.add("育儿假");
        heads.add(head);
        keyList.add("18dfd7814234fcc6fc4ebf44e12a1b8b");

        head = new ArrayList<>();
        head.add("护理假");
        head.add("护理假");
        heads.add(head);
        keyList.add("18dfd7813c875e7010cb2584c238cd1a");

        head = new ArrayList<>();
        head.add("出勤天数");
        head.add("出勤天数");
        heads.add(head);
        keyList.add("WorkdayAttendDays");

        head = new ArrayList<>();
        head.add("法定上班天数");
        head.add("法定上班天数");
        heads.add(head);
        keyList.add("ExpectedAttendDays");

        head = new ArrayList<>();
        head.add("备注");
        head.add("备注");
        heads.add(head);

        String reportId = request.getParameter("reportId");
        pageQueryParams.setPageSize(99999);
        List<Map<String, Object>> listContents = hrAttendReportService.listReportByPage(reportId, pageQueryParams).getContent();
        List<List<Object>> list = new ArrayList<>();
        List<Object> listContent;

        for (int i = 0; i < listContents.size(); i++) {
            Map<String, Object> map = listContents.get(i);
            listContent = new ArrayList<>();
            HrStaffInfoSimpleVo staff = (HrStaffInfoSimpleVo) map.get("staff");
            String Dept = (String) map.get("StaffDept1");
            listContent.add(Dept);
            listContent.add(i+1);
            listContent.add(staff.getFdName());
            listContent.add("上午");
            for (String key : keyList) {
                if (map.containsKey(key)){
                    Object keyVaule = map.get(key);
                    if (key.startsWith(AttendResultByDay.NAME + "_")) {
                        JSONObject json = JSONObject.parseObject((String) keyVaule);
                        JSONArray texts = json.getJSONArray("texts");
                        if(texts.size()!=0){
                            JSONObject object = (JSONObject) texts.get(0);
                            String text = object.getString("text");
                            if (StringUtil.isNotBlank(text)){
                                if(text.contains("正常")){
                                    listContent.add("√");
                                }else if(text.contains("早退")){
                                    listContent.add("R");
                                }else if(text.contains("迟到")){
                                    listContent.add("C");
                                }else if(text.contains("休息")){
                                    listContent.add("-");
                                }else{
                                    listContent.add(text);
                                }
                            }else{
                                listContent.add(" ");
                            }
                        }else{
                            listContent.add(" ");
                        }
                    }else if(key.contains("AbsentDays")){
                        listContent.add(new BigDecimal(String.valueOf(keyVaule)).stripTrailingZeros().toPlainString());
                    }else if(key.contains("18dfd7813eac364231d9e87406881bac")){
                        listContent.add(new BigDecimal(String.valueOf(keyVaule)).stripTrailingZeros().toPlainString());

                    }else if(key.contains("18dfd7813fb4b18b54ea6c84124b2e1a")){
                        listContent.add(new BigDecimal(String.valueOf(keyVaule)).stripTrailingZeros().toPlainString());

                    }else if(key.contains("18dfd7814234fcc6fc4ebf44e12a1b8b")){
                        listContent.add(new BigDecimal(String.valueOf(keyVaule)).stripTrailingZeros().toPlainString());

                    }else if(key.contains("18dfd7813c875e7010cb2584c238cd1a")){
                        listContent.add(new BigDecimal(String.valueOf(keyVaule)).stripTrailingZeros().toPlainString());

                    }else if(key.contains("WorkdayAttendDays")){
                        BigDecimal WorkdayAttendDays = (BigDecimal) map.get("WorkdayAttendDays");
                        int work = WorkdayAttendDays.intValue();
                        BigDecimal RestdayAttendDays = (BigDecimal) map.get("RestdayAttendDays");
                        int rest= RestdayAttendDays.intValue();
                        listContent.add(String.valueOf(work+rest));

                    }else if(key.contains("ExpectedAttendDays")){
                        listContent.add(new BigDecimal(String.valueOf(keyVaule)).stripTrailingZeros().toPlainString());
                    }else{
                        listContent.add(" ");
                    }
                }
            }
            list.add(listContent);
            listContent = new ArrayList<>();
            listContent.add(Dept);
            listContent.add(i+1);
            listContent.add(staff.getFdName());
            listContent.add("下午");
            for (String key : keyList) {
                if (map.containsKey(key)){
                    Object keyVaule = map.get(key);
                    if (key.startsWith(AttendResultByDay.NAME + "_")) {
                        JSONObject json = JSONObject.parseObject((String) keyVaule);
                        JSONArray texts = json.getJSONArray("texts");
                        if(texts.size()==2){
                            JSONObject object = (JSONObject) texts.get(1);
                            String text = object.getString("text");
                            if (StringUtil.isNotBlank(text)){
                                if(text.contains("正常")){
                                    listContent.add("√");
                                }else if(text.contains("早退")){
                                    listContent.add("R");
                                }else if(text.contains("迟到")){
                                    listContent.add("C");
                                }else if(text.contains("休息")){
                                    listContent.add("-");
                                }else{
                                    listContent.add(text);
                                }
                            }else{
                                listContent.add(" ");
                            }
                        }else{
                            listContent.add(" ");
                        }
                    }else if(key.contains("AbsentDays")){
                        listContent.add(new BigDecimal(String.valueOf(keyVaule)).stripTrailingZeros().toPlainString());
                    }else if(key.contains("18dfd7813eac364231d9e87406881bac")){
                        listContent.add(new BigDecimal(String.valueOf(keyVaule)).stripTrailingZeros().toPlainString());

                    }else if(key.contains("18dfd7813fb4b18b54ea6c84124b2e1a")){
                        listContent.add(new BigDecimal(String.valueOf(keyVaule)).stripTrailingZeros().toPlainString());

                    }else if(key.contains("18dfd7814234fcc6fc4ebf44e12a1b8b")){
                        listContent.add(new BigDecimal(String.valueOf(keyVaule)).stripTrailingZeros().toPlainString());

                    }else if(key.contains("18dfd7813c875e7010cb2584c238cd1a")){
                        listContent.add(new BigDecimal(String.valueOf(keyVaule)).stripTrailingZeros().toPlainString());

                    }else if(key.contains("WorkdayAttendDays")){
                        BigDecimal WorkdayAttendDays = (BigDecimal) map.get("WorkdayAttendDays");
                        int work = WorkdayAttendDays.intValue();
                        BigDecimal RestdayAttendDays = (BigDecimal) map.get("RestdayAttendDays");
                        int rest= RestdayAttendDays.intValue();
                        listContent.add(String.valueOf(work+rest));

                    }else if(key.contains("ExpectedAttendDays")){
                        listContent.add(new BigDecimal(String.valueOf(keyVaule)).stripTrailingZeros().toPlainString());
                    }else{
                        listContent.add(" ");
                    }
                }
            }
            list.add(listContent);
        }




        try {
            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Pragma", "public");
            response.setHeader("Content-Disposition", "attachment");

            ServletOutputStream outputStream = response.getOutputStream();
            int headCount=heads.size();
            //需要合并的列
            int[] mergeColumnIndex = {0,1,2,headCount-8,headCount-7,headCount-6,headCount-5,headCount-4,headCount-3,headCount-2,headCount-1,headCount-8};
            //从第二行后开始合并
            int mergeRowIndex = 2;

            try {
                if (outputStream != null) {
                    logger.info("export start------------------------------" + System.currentTimeMillis());

                    EasyExcel.write(outputStream).registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                            .registerWriteHandler(new WriteHandlerStrategy())
                            .registerWriteHandler(new ExcelMergeUtil(mergeRowIndex,mergeColumnIndex))
                            .inMemory(true)
                            .head(heads).sheet("考勤报表").doWrite(list);
                    logger.info("export end------------------------------" + System.currentTimeMillis());
                }
            } catch (Exception e) {
                logger.error("exportMainExcelInfo error", e);
            } finally {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            }
        } catch (Exception e) {
            logger.error("导出数据失败", e);
        }
    }
}
