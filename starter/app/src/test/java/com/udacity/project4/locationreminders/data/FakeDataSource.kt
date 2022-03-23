package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

//     Create a fake data source to act as a double to the real data source
    private var shouldErrorReturn = false

    fun setReturnError(value: Boolean) {
        shouldErrorReturn = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //("Return the reminders")
        if (shouldErrorReturn) {
            return Result.Error("Reminders not found")
        }
        return if (reminders != null) {
            Result.Success(reminders)
        } else {
            Result.Error("Reminders not found")
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        //("save the reminder")
        reminders?.add(reminder)

    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //("return the reminder with the id")
        if (shouldErrorReturn) {
            return Result.Error("Reminders not found")
        }
        return if (reminders != null) {
            for (item in reminders) {
                if (item.id == id) {
                    Result.Success(item)
                }
            }
            Result.Error("Reminder not found")
        } else {
            Result.Error("Reminder not found")
        }
    }

    override suspend fun deleteAllReminders() {
        //("delete all the reminders")
        reminders?.clear()
    }


}