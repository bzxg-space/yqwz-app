package bzxg.yqwz.Util;

/**
 * Created by bzxg on 2021/3/7.
 * by
 * https://github.com/SamanLan/Peimissions/blob/master/lib_permisshelper/src/main/java/com/samanlan/lib_permisshelper/PermissionsUtils.java
 */
public interface PermissionsListener {

    void onDenied(String[] deniedPermissions);

    void onGranted();
}
