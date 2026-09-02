package com.maarifa.app.navigation

import android.net.Uri

object Routes {
    // Auth graph
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val OTP = "otp/{verificationId}?phone={phone}"
    
    fun otp(verificationId: String, phone: String = ""): String {
        val encodedPhone = Uri.encode(phone)
        return "otp/$verificationId?phone=$encodedPhone"
    }

    // Student graph
    const val STUDENT_HOME = "student_home"
    const val SEARCH = "search"
    const val MATERIAL_DETAIL = "material/{materialId}"
    
    fun materialDetail(materialId: String): String {
        return "material/${Uri.encode(materialId)}"
    }
    
    const val READER = "reader/{materialId}"
    
    fun reader(materialId: String): String {
        return "reader/${Uri.encode(materialId)}"
    }
    
    const val SUBSCRIPTION = "subscription"
    const val DOWNLOADS = "downloads"
    const val STUDENT_PROFILE = "student_profile"

    // Teacher graph
    const val TEACHER_HOME = "teacher_home"
    const val TEACHER_VERIFICATION_PENDING = "teacher_pending"
    const val UPLOAD_MATERIAL = "upload_material"
    const val TEACHER_MATERIALS = "teacher_materials"
    const val TEACHER_EARNINGS = "teacher_earnings"
    const val TEACHER_PROFILE = "teacher_profile"
}
