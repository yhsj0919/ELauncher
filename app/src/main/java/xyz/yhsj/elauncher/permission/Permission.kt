package xyz.yhsj.elauncher.permission


/**
 * @param name The name of the permission
 * @param granted If the permission has been granted to the given package.
 * @param shouldShowRequestPermissionRationale Gets whether you should show UI with rationale for requesting a permission.
 */
data class Permission(val name: String, val granted: Boolean, val shouldShowRequestPermissionRationale: Boolean)