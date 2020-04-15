package xyz.yhsj.elauncher.permission

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.reactivex.Flowable
import io.reactivex.functions.Function
import io.reactivex.processors.PublishProcessor

class RxPermission(activity: Activity) {

    val TAG = "RxPermission"

    val mRxPermissionFragment: RxPermissionFragment

    init {
        mRxPermissionFragment = getRxPermissionFragment(activity)
    }

    private fun getRxPermissionFragment(activity: Activity): RxPermissionFragment {
        var rxPermissionFragment = findRxPermissionFragment(activity)
        if (rxPermissionFragment == null) {
            rxPermissionFragment = RxPermissionFragment()
            val fragmentManager = activity.fragmentManager
            fragmentManager.beginTransaction()
                .add(rxPermissionFragment, TAG)
                .commitAllowingStateLoss()
            fragmentManager.executePendingTransactions()
        }
        return rxPermissionFragment
    }

    private fun findRxPermissionFragment(activity: Activity): RxPermissionFragment? {
        return activity.fragmentManager.findFragmentByTag(TAG) as RxPermissionFragment?
    }

    @SuppressWarnings("WeakerAccess", "unused")
    @TargetApi(Build.VERSION_CODES.M)
    fun request(vararg permissions: String): Flowable<Boolean> {

        if (permissions.isEmpty()) {
            throw IllegalArgumentException("RxPermission.request/requestEach requires at least one input permission")
        }

        return requestImplementation(permissions)
            .buffer(permissions.size)
            .flatMap(Function<List<Permission>, Flowable<Boolean>> { permissions ->
                if (permissions.isEmpty()) {
                    return@Function Flowable.empty()
                }
                for ((_, granted) in permissions) {
                    if (!granted) {
                        return@Function Flowable.just(false)
                    }
                }
                Flowable.just(true)
            })
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestImplementation(permissions: Array<out String>): Flowable<Permission> {

        val list = ArrayList<Flowable<Permission>>(permissions.size)
        val unRequestedPermissions = ArrayList<String>()

        for (permission in permissions) {
            // If the permission has been granted to the given package.
            if (isGranted(permission)) {
                list.add(Flowable.just(Permission(permission, true, false)))
                continue
            }

            // The user cannot grant policy revoked permissions.
            // hence the only way for an app to get such a permission is by a policy change.
            if (isRevoked(permission)) {
                list.add(Flowable.just(Permission(permission, false, false)))
                continue
            }

            var processor = mRxPermissionFragment.getProcessorByPermission(permission)
            if (processor == null) {
                unRequestedPermissions.add(permission)
                processor = PublishProcessor.create()
                mRxPermissionFragment.setProcessorForPermission(permission, processor)
            }

            list.add(processor!!)
        }

        if (unRequestedPermissions.isNotEmpty()) {
            val unRequestedPermissionsArray = unRequestedPermissions.toTypedArray()
            if (isMarshmallow()) {
                requestPermissionsFromFragment(unRequestedPermissionsArray)
            } else {
                Log.w(TAG, "Make sure you are android 6.0 above.")
            }
        }

        return Flowable.concat(list)
    }

    fun shouldShowRequestPermissionRationale(
        activity: Activity,
        vararg permissions: String
    ): Flowable<Boolean> {
        if (!isMarshmallow()) {
            return Flowable.just(false)
        }
        return Flowable.just(
            shouldShowRequestPermissionRationaleImplementation(
                activity,
                permissions
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun shouldShowRequestPermissionRationaleImplementation(
        activity: Activity,
        permissions: Array<out String>
    ): Boolean {
        return permissions.none {
            !isGranted(it) && !activity.shouldShowRequestPermissionRationale(
                it
            )
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermissionsFromFragment(permissions: Array<String>) {
        mRxPermissionFragment.requestPermissions(permissions)
    }

    /**
     * Returns true if the permission is already granted.
     * Always true if SDK < 23.
     */
    @SuppressWarnings("WeakerAccess")
    fun isGranted(permission: String): Boolean {
        return isMarshmallow() && mRxPermissionFragment.isGranted(permission)
    }

    /**
     * Returns true if the permission has been revoked by a policy.
     * Always false if SDK < 23.
     */
    @SuppressWarnings("WeakerAccess")
    fun isRevoked(permission: String): Boolean {
        return isMarshmallow() && mRxPermissionFragment.isRevoked(permission)
    }

    fun isMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }
}