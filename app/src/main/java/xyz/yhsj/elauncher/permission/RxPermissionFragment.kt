package xyz.yhsj.elauncher.permission

import android.annotation.TargetApi
import android.app.Fragment
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import java.util.*

class RxPermissionFragment : Fragment() {

    private val PERMISSIONS_REQUEST_CODE = 0x42

    val processor = HashMap<String, PublishProcessor<Permission>>()
    var mLogging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissions(permissions: Array<String>) {
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != PERMISSIONS_REQUEST_CODE) return

        val shouldShowRequestPermissionRationale = BooleanArray(permissions.size)

        for (index in 0..permissions.size - 1) {
            shouldShowRequestPermissionRationale[index] = shouldShowRequestPermissionRationale(permissions[index])
        }

        onRequestPermissionsResult(permissions, grantResults, shouldShowRequestPermissionRationale)
    }

    fun onRequestPermissionsResult(permissions: Array<out String>, grantResults: IntArray, shouldShowRequestPermissionRationale: BooleanArray) {
        for (index in 0..permissions.size - 1) {
            val subject = processor[permissions[index]] ?: return
            processor.remove(permissions[index])
            val granted = grantResults[index] == PackageManager.PERMISSION_GRANTED
            subject.onNext(Permission(permissions[index], granted, shouldShowRequestPermissionRationale[index]))
            subject.onComplete()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun isGranted(permission: String): Boolean {
        return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun isRevoked(permission: String): Boolean {
        return activity.packageManager.isPermissionRevokedByPolicy(permission, activity.packageName)
    }

    fun containsByPermission(permission: String): Boolean {
        return processor.containsKey(permission)
    }

    fun getProcessorByPermission(permission: String): Flowable<Permission>? {
        return processor[permission]
    }

    fun setProcessorForPermission(permission: String, processor: PublishProcessor<Permission>) {
        this.processor[permission] = processor
    }
}