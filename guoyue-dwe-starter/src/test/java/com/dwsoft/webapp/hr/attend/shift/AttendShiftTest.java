package com.dwsoft.webapp.hr.attend.shift;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.alibaba.fastjson.JSON;
import com.dwsoft.core.tool.support.PagableSupplier;
import com.dwsoft.core.tool.utils.JsonUtil;
import com.dwsoft.webapp.ApplicationTestRunner;
import com.dwsoft.webapp.ScheduledTestLine;
import com.dwsoft.webapp.Starter;
import com.dwsoft.webapp.hr.attend.api.attendance.definition.AttendGroupDefine;
import com.dwsoft.webapp.hr.attend.api.attendance.definition.schedule.ScheduleConfig;
import com.dwsoft.webapp.hr.attend.api.attendance.definition.shift.ShiftConfig;
import com.dwsoft.webapp.hr.attend.api.attendance.runtime.AttendSchedule;
import com.dwsoft.webapp.hr.attend.api.command.clock.ComputeClockParameter;
import com.dwsoft.webapp.hr.attend.api.command.clock.DeviceClockInParameter;
import com.dwsoft.webapp.hr.attend.api.command.schedule.CreateAttendFlightsJobParameter;
import com.dwsoft.webapp.hr.attend.engine.AttendScheduleService;
import com.dwsoft.webapp.hr.attend.engine.AttendanceEngine;
import com.dwsoft.webapp.hr.attend.engine.impl.cmd.CreateAsyncJobComputeClockResultCmd;
import com.dwsoft.webapp.hr.attend.engine.impl.cmd.CreateAttendScheduleByAttendDefineCmd;
import com.dwsoft.webapp.hr.attend.entity.HrAttendGroup;
import com.dwsoft.webapp.hr.attend.entity.HrAttendScheSet;
import com.dwsoft.webapp.hr.attend.entity.HrAttendShift;
import com.dwsoft.webapp.hr.attend.repository.HrAttendClockRecordRepository;
import com.dwsoft.webapp.hr.attend.repository.HrAttendClockResultRepository;
import com.dwsoft.webapp.hr.attend.repository.HrAttendFlightsRepository;
import com.dwsoft.webapp.hr.attend.repository.HrAttendGroupRepository;
import com.dwsoft.webapp.hr.attend.repository.HrAttendJobRepository;
import com.dwsoft.webapp.hr.attend.repository.HrAttendScheSetRepository;
import com.dwsoft.webapp.hr.attend.repository.HrAttendScheduleRepository;
import com.dwsoft.webapp.hr.attend.repository.HrAttendShiftRepository;
import com.dwsoft.webapp.hr.attend.service.IHrAttendDefineHistoryService;
import com.dwsoft.webapp.hr.attend.service.IHrAttendGroupService;
import com.dwsoft.webapp.hr.attend.service.IHrAttendScheSetService;
import com.dwsoft.webapp.hr.attend.service.IHrAttendShiftService;
import com.dwsoft.webapp.hr.attend.service.IHrAttendStaffHistoryService;
import com.dwsoft.webapp.hr.staff.service.IHrStaffInfoService;
import com.dwsoft.webapp.sys.org.entity.SysOrgElement;
import com.dwsoft.webapp.sys.org.interfaces.ISysOrgCoreService;
import com.google.common.collect.Lists;

import cn.hutool.core.lang.ObjectId;

/**
 * 
 */
@RunWith(ApplicationTestRunner.class)
@SpringBootTest(classes = { Starter.class }, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AttendShiftTest {

	@Autowired
	private HrAttendShiftRepository hrAttendShiftRepository;
	
	@Autowired
	private HrAttendClockRecordRepository hrAttendClockRecordRepository;
	
	@Autowired
	private HrAttendClockResultRepository hrAttendClockResultRepository;

	@Autowired
	private HrAttendScheSetRepository hrAttendScheSetRepository;

	@Autowired
	private HrAttendGroupRepository hrAttendGroupRepository;
	
	@Autowired
	private IHrAttendGroupService hrAttendGroupService;

	@Autowired
	private ISysOrgCoreService sysOrgCoreService;

	@Autowired
	protected IHrStaffInfoService hrStaffInfoService;

	@Autowired
	private HrAttendFlightsRepository hrAttendFlightsRepository;

	@Autowired
	private HrAttendScheduleRepository hrAttendScheduleRepository;
	
	@Autowired
	private IHrAttendScheSetService hrAttendScheSetService;
	
	@Autowired
	private IHrAttendShiftService hrAttendShiftService;
	
	@Autowired
	private IHrAttendStaffHistoryService hrAttendStaffHistoryService;
	
	@Autowired
	private ObjectProvider<AttendanceEngine> attendanceEngine;
	
	@Autowired
	private AttendScheduleService attendScheduleService;
	
	@Autowired
	private HrAttendJobRepository hrAttendJobRepository;
	
	@Autowired
	protected IHrAttendDefineHistoryService hrAttendDefineHistoryService;

	private void deleteRows() {
		
		hrAttendJobRepository.deleteAllInBatch();
		hrAttendClockRecordRepository.deleteAllInBatch();
		hrAttendClockResultRepository.deleteAllInBatch();
		hrAttendScheduleRepository.deleteAllInBatch();
		hrAttendFlightsRepository.deleteAllInBatch();
		hrAttendGroupRepository.deleteAllInBatch();
		hrAttendScheSetRepository.deleteAllInBatch();
		hrAttendShiftRepository.deleteAllInBatch();
	}

	/**
	 * 办公室坐班，朝九晚五，周末双休
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testShift1() throws Exception {
		// 上下班：08:30-17:30，中午休息：12:00-13:00， 允许晚到晚走、早到早走（15分钟弹性），迟到30分钟算严重迟到。
		// 上班卡打卡时间段：上班前90分钟 至 上班后60分钟
		HrAttendShift shift1 = createShift("shift1", "办公室行政班次", "08:30-17:30");
		// 考勤时间配置：周一到周5上班, 周末休息，法定节假日自动排休息
		HrAttendScheSet set = createHrAttendScheSet("set1", "办公室行政班次");

		// 考勤组配置
		List<SysOrgElement> fdAddOrgs = sysOrgCoreService.findDirectChildren(null, 2); // 顶层部门
		if (fdAddOrgs.isEmpty()) {
			fdAddOrgs = sysOrgCoreService.findDirectChildren(null, 1); // 顶层机构
		}
		
//		{
//			//随便搞一个人
//			fdAddOrgs = sysOrgCoreService.findAllElementsByIds(Lists.newArrayList("189f84005b1746acd0e273a49d8bc5e3"));
//		}
		
		HrAttendGroup group = createHrAttendGroup("办公室行政班次", fdAddOrgs, set);
		
		ScheduledTestLine line = new ScheduledTestLine(false);
		line.setSleepTime(10000L); //每步停10秒，以保证上一步走完了
		
		Set<String> staffIds = hrStaffInfoService.expandOrgListToStaffIds(group.getFdAddOrgs());
		
		assertTrue("获取不到排班员工", !staffIds.isEmpty());
		
		line.schedule(() -> {
			// 排班
			Set<String> excludePersonIds = hrStaffInfoService.expandOrgListToStaffIds(group.getFdMinusOrgs());
			staffIds.removeAll(excludePersonIds);
			
			//立即排一下今天昨天的班
			attendanceEngine.getIfAvailable().getRuntimeService().executeCommand(new CreateAttendScheduleByAttendDefineCmd(set.getScheduleConfig(), LocalDate.now().minusDays(1)));
			attendanceEngine.getIfAvailable().getRuntimeService().executeCommand(new CreateAttendScheduleByAttendDefineCmd(set.getScheduleConfig(), LocalDate.now()));
			attendanceEngine.getIfAvailable().getRuntimeService().executeCommand(new CreateAttendScheduleByAttendDefineCmd(set.getScheduleConfig(), LocalDate.now().plusDays(1)));
		});
		
		String firstOne = staffIds.iterator().next();
		line.schedule(() -> {
			// 删除某人排班
			//List<String> removeStaffIds = Lists.newArrayList(firstOne);

			//attendanceEngine.getScheduleService().removeAttendSchedules(removeStaffIds, group.getFdId());
		});
		
		line.schedule(() -> {
			//放几个人的打卡数据
			if(staffIds.size() > 0) {
				Random random = new Random();
				
				int minValue = 0;
		        List<String> staffList = Lists.newArrayList(staffIds);
		        int maxValue = staffList.size() - 1;
		        
				for (int i = 0; i < 200; i++) {
					 int randomInt = random.nextInt(maxValue - minValue + 1) + minValue;
					 String staffId = staffList.get(randomInt);
					 
					 LocalDate date = RandomDateGen();
					 LocalTime time = RandomTimeGen();
					 
					clock(LocalDateTime.of(date, time), staffId);
				}
			}
			
		});
		
		line.schedule(() -> {
			//算打卡结果
			CreateAttendFlightsJobParameter s1 = new CreateAttendFlightsJobParameter();
			s1.setAttendDefineId(set.getFdId());
			s1.setRelDate(LocalDate.now().minusDays(1));
			s1.setShiftId(shift1.getFdId());
			
			PagableSupplier<List<AttendSchedule>> pagableSupplier = attendScheduleService.createAttendSchedulePagable(100, s1);
			
			while (pagableSupplier.hasNext()) {
				List<AttendSchedule> scheduleList =	pagableSupplier.get();
				
				for (AttendSchedule attendSchedule : scheduleList) {
					ComputeClockParameter parameter = new ComputeClockParameter();
					
					parameter.setRelDate(attendSchedule.getRelDate());
					parameter.setShiftId(attendSchedule.getShiftId());
					
					if(attendSchedule.getElementType().intValue() == 1) {
						parameter.setAttendDefineId(attendSchedule.getElementId());
					}
					
					if(attendSchedule.getElementType().intValue() == 2) {
						parameter.setStaffId(attendSchedule.getElementId());
					}
					
					attendanceEngine.getIfAvailable().getRuntimeService().executeCommand(new CreateAsyncJobComputeClockResultCmd(parameter));
				}
			}
			
		});

		line.await();

		System.out.println("-------------------------");
		//Thread.currentThread().join();
	}
	
	private void clock(LocalDateTime time , String staffId) {
		DeviceClockInParameter clockInParameter = new DeviceClockInParameter();
		clockInParameter.setClockTime(time);
		clockInParameter.setDeviceId("testdeviceid");
		clockInParameter.setDeviceName("办公室打卡机1");
		clockInParameter.setStaffId(staffId);
		
		attendanceEngine.getIfAvailable().getRuntimeService().createAttendClock(clockInParameter);
	}

	@Before
	public void setUp() {
		deleteRows();
	}

	@After
	public void tearDown() {
		// 清理操作
	}

	private HrAttendShift createShift(String fileName, String shiftName, String shiftDisName) throws Exception {
		String path = "classpath:hrAttendShift/" + fileName + ".json";

		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		Resource resource = resourcePatternResolver.getResource(path);

		if (resource.isReadable()) {
			try (InputStream is = resource.getInputStream()) {
				ShiftConfig config = JsonUtil.readValue(is, ShiftConfig.class);
				System.out.println(JSON.toJSONString(config));

				HrAttendShift hrAttendShift = new HrAttendShift();
				hrAttendShift.setFdId(config.getShiftId());
				
				hrAttendShift.setFdName(shiftName);
				hrAttendShift.setFdDisplayName(shiftDisName);
				hrAttendShift.setFdIsDeleted(false);
				hrAttendShift.setFdShiftConfig(config);

				HrAttendShift added = hrAttendShiftService.add(hrAttendShift);
				return added;
			}
		}

		return null;
	}

	private HrAttendScheSet createHrAttendScheSet(String fileName, String name) throws Exception {
		String path = "classpath:hrAttendScheSet/" + fileName + ".json";

		PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		Resource resource = resourcePatternResolver.getResource(path);

		if (resource.isReadable()) {

			try (InputStream is = resource.getInputStream()) {
				ScheduleConfig config = JsonUtil.readValue(is, ScheduleConfig.class);

				HrAttendScheSet hrAttendScheSet = new HrAttendScheSet();
				hrAttendScheSet.setFdId(config.getId());
				hrAttendScheSet.setFdName(name);
				hrAttendScheSet.setFdAttendType(config.getAttendType());
				hrAttendScheSet.setScheduleConfig(config);

				HrAttendScheSet added = hrAttendScheSetService.add(hrAttendScheSet);
				return added;
			}
		}

		return null;
	}

	private HrAttendGroup createHrAttendGroup(String name, List<SysOrgElement> fdAddOrgs, HrAttendScheSet set)
			throws Exception {
		HrAttendGroup group = new HrAttendGroup();

		group.setFdAddOrgs(fdAddOrgs);
		group.setFdScheSet(set);
		group.setFdName(name);

		HrAttendGroup saved = hrAttendGroupRepository.save(group);
		Set<String> staffIds = hrAttendGroupService.expandGroupToStaffIds(saved);
		
		boolean match = hrAttendStaffHistoryService.checkAttendStaffHistoryMatch(staffIds, saved.getFdId(),
				set.getFdId());
		if (!match) {
			hrAttendStaffHistoryService.updateAttendStaffHistory(staffIds, saved.getFdId(), set.getFdId());
		}
		
		AttendGroupDefine groupDefine = new AttendGroupDefine();
		groupDefine.setId(saved.getFdId());
		groupDefine.setName(saved.getFdName());
		
		hrAttendDefineHistoryService.updateAttendDefineHistory(saved.getFdId(), groupDefine, null);

		return saved;
	}
	
	static LocalDate RandomDateGen() {
		Random random = new Random();

		int minValue = 0;
		List<LocalDate> list = Lists.newArrayList(LocalDate.now().minusDays(1), LocalDate.now(),
				LocalDate.now().plusDays(1));
		int maxValue = list.size() - 1;

		int randomInt = random.nextInt(maxValue - minValue + 1) + minValue;
		return list.get(randomInt);
	}
	
	public static LocalTime RandomTimeGen() {
		Random random = new Random();

		int hour = random.nextInt(24);
		int minute = random.nextInt(60);
		int second = random.nextInt(60);

		LocalTime time = LocalTime.of(hour, minute, second);
		return time;
	}
	 
	public static void main(String[] args) {
		System.out.println(ObjectId.next());
		System.out.println(System.currentTimeMillis());
		System.out.println(ObjectId.next().length() + String.valueOf(System.currentTimeMillis()).length() + 1);
		
		 // 创建一个Random对象
        Random random = new Random();
        
        // 生成一个0到1之间的随机小数
        double randomDouble = random.nextDouble();
        System.out.println("随机小数：" + randomDouble);
        
        // 生成一个指定范围内的随机整数（包括边界值）
        int minValue = 5;
        int maxValue = 20;
        int randomInt = random.nextInt(maxValue - minValue + 1) + minValue;
        System.out.println("随机整数：" + randomInt);
        
        RandomTimeGen();

	}

	@Test
	public void testShift2() throws Exception {
//		IHrAttendReportService hrAttendReportService = SpringUtil.getBean(IHrAttendReportService.class);
//		Map<String, Object> map = hrAttendReportService.staffScheduleQueryReport("189f8400b2a34e9343aaf044463a268f", LocalDate.of(2024, 2, 4), LocalDate.of(2024, 2, 6));
//		System.out.println(map);
		createShift("shift1", "办公室行政班次", "08:30-17:30");
	}
}
