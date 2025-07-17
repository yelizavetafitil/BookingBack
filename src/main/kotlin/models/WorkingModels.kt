package com.example.models

import kotlinx.serialization.Serializable


@Serializable
data class WorkingHoursRequest(
    val employeeId: Int,
    val scheduleType: String,
    val workTimeSlots: List<WorkTimeSlotRequest>
)

@Serializable
data class WorkTimeSlotRequest(
    val startTime: String,
    val endTime: String,
    val validFrom: String,
    val validTo: String,
    val breaks: List<BreakTimeRequest>
)

@Serializable
data class BreakTimeRequest(
    val startTime: String,
    val endTime: String
)

@Serializable
data class WorkingHoursResponse(
    val scheduleId: Int,
    val scheduleType: String,
    val workTimeSlots: List<WorkTimeSlotResponse>
)

@Serializable
data class WorkTimeSlotResponse(
    val id: Int,
    val startTime: String,
    val endTime: String,
    val validFrom: String,
    val validTo: String,
    val breaks: List<BreakTimeResponse>
)

@Serializable
data class BreakTimeResponse(
    val id: Int,
    val startTime: String,
    val endTime: String
)
@Serializable
data class WorkingWeeksHoursRequest(
    val employeeId: Int,
    val scheduleType: String,
    val dayOfWeek: String, // например: "1,2,3,4,5" для рабочих дней
    val scheduleSubType: String,
    val workTimeSlots: List<WorkTimeSlotRequest>
)
@Serializable
data class WeekDayScheduleResponse(
    val dayOfWeek: String,
    val scheduleType: String,
    val scheduleSubType: String,
    val workTimeSlots: List<WorkTimeSlotResponse>
)
@Serializable
data class WorkTime(
    val start: String, // формат HH:mm
    val end: String   // формат HH:mm
)
@Serializable
data class WorkPeriod(
    val start: String, // формат dd.MM.yy
    val end: String   // формат dd.MM.yy
)
@Serializable
data class BreakTime(
    val start: String, // формат HH:mm
    val end: String   // формат HH:mm
)
