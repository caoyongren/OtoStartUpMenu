package com.android.startupmenu;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.startupmenu.util.StartupMenuSqliteOpenHelper;
import com.android.startupmenu.util.TableIndexDefine;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class StartupMenuApplication extends Application {

    private Context mContext = this;
    private SQLiteDatabase mdb;
    private StartupMenuSqliteOpenHelper mMsoh;

    @Override
    public void onCreate() {
        super.onCreate();
        mMsoh = new StartupMenuSqliteOpenHelper(mContext,
                "StartupMenu_database.db", null, 1);
        mdb = mMsoh.getWritableDatabase();
        initStartupMenuData(mContext);
    }

    public void initStartupMenuData(Context context) {
        PackageManager pkgManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pkgManager.queryIntentActivities(intent, 0);
        Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pkgManager));
        int clickNumber = 0;
        for (ResolveInfo reInfo : resolveInfos) {
            File file = new File(reInfo.activityInfo.applicationInfo.sourceDir);
            Date systemDate = new Date(file.lastModified());
            //ApplicationInfo applicationInfo = reInfo.activityInfo.applicationInfo;
            //String activityName = reInfo.activityInfo.name;
            //Drawable icon = reInfo.loadIcon(pkgManager);
            String pkgName = reInfo.activityInfo.packageName;
            String appLabel = (String) reInfo.loadLabel(pkgManager);
            Cursor cursor = mdb.rawQuery("select * from " + TableIndexDefine.TABLE_APP_PERPO +
                            " where " + TableIndexDefine.COLUMN_PERPO_PKGNAME + " = ?",
                    new String[] { pkgName });
            insertData(cursor, pkgName, appLabel, systemDate, clickNumber);
            cursor.close();
        }
    }

    /**
     * Insert data /pkgName/label/date/num into
     * StartupMenu_database.db {@linkStartupMenuSqliteOpenHelper}
     * could Reusing in {@link com.android.startupmenu.service.StartupMenuInstalledReceiver}
     */
    public void insertData(Cursor cursor, String pkgName, String appLabel,
                           Date systemDate, int clickNumber) {
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (pkgName.equals(cursor.getString(cursor.getColumnIndex(
                        TableIndexDefine.COLUMN_PERPO_PKGNAME)))) {
                    return;
                }
            }
            mdb.execSQL("insert into " +
                            TableIndexDefine.TABLE_APP_PERPO + "(" +
                            TableIndexDefine.COLUMN_PERPO_LABEL + "," +
                            TableIndexDefine.COLUMN_PERPO_PKGNAME + "," +
                            TableIndexDefine.COLUMN_PERPO_INSTALL_DATE + "," +
                            TableIndexDefine.COLUMN_PERPO_CLICK_NUM + ")" +
                            "values (?, ?, ?, ?)"  ,
                    new Object[] { appLabel, pkgName, systemDate, clickNumber});
        }
    }
}
