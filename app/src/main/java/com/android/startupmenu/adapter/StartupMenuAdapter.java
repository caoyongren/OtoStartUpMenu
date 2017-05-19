package com.android.startupmenu.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.startupmenu.R;
import com.android.startupmenu.StartupMenuActivity;
import com.android.startupmenu.dialog.StartMenuDialog;
import com.android.startupmenu.util.AppInfo;
import com.android.startupmenu.util.Constants;
import com.android.startupmenu.util.StartupMenuSqliteOpenHelper;
import com.android.startupmenu.util.StartupMenuUtil;

import java.util.List;
import java.util.Map;

public class StartupMenuAdapter extends BaseAdapter implements View.OnTouchListener{
    public static final int START_MENU_RIGHT_MOUSE_UI_NUMBER = 57;
    public static final String TAG = "StartupMenu_DEBUG";
    public static String strPkgName;
    private List<AppInfo> mlistAppInfo = null;
    private Map<Integer,Boolean> isCheckedMap;
    LayoutInflater infater = null;
    private Context mContext;
    private StartupMenuActivity mStartupMenuActivity;
    private SQLiteDatabase mdb;
    private StartupMenuSqliteOpenHelper mMsoh;
    private int mStartMenuAppWidth;
    private int mStartMenuAppHeight;
    public static boolean mIsFullScreen;
    public static int mPositionItem;

    public StartupMenuAdapter(Context context, List<AppInfo> apps,
                              Map<Integer,Boolean> isCheckedMap) {
        mContext = context;
        mStartMenuAppWidth = mContext.getResources()
                                     .getDimensionPixelSize(R.dimen.start_menu_app_width);
        mStartMenuAppHeight = mContext.getResources()
                                      .getDimensionPixelSize(R.dimen.start_menu_app_height);
        infater = (LayoutInflater) context
                   .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mlistAppInfo = apps;
        this.isCheckedMap = isCheckedMap;
        mMsoh = new StartupMenuSqliteOpenHelper(mContext, "StartupMenu_database.db", null, 1);
        mdb = mMsoh.getWritableDatabase();
        mStartupMenuActivity = getStartupMenuActivity();
    }

    @Override
    public int getCount() {
        return mlistAppInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mlistAppInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertview, ViewGroup arg2) {
        View view = null;
        ViewHolder holder = null;
        if (convertview == null || convertview.getTag() == null) {
            view = infater.inflate(R.layout.activity_gridview, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertview;
            holder = (ViewHolder) convertview.getTag();
        }
        AppInfo appInfo = (AppInfo) getItem(position);
        holder.appIcon.setImageDrawable(appInfo.getAppIcon());
        String appName = appInfo.getAppLabel();
        holder.tvAppLabel.setText(appInfo.limitNameLength(appName, mContext, appInfo));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClickItem(position, mlistAppInfo);
            }
        });
        view.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                int what = motionEvent.getButtonState();
                StartupMenuActivity.setFocus(true);
                switch (what) {
                case MotionEvent.BUTTON_PRIMARY:
                    String pkgName = StartupMenuActivity.mlistAppInfo.get(position).getPkgName();
                    Intent intent = StartupMenuActivity.mlistAppInfo.get(position).getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    openAppBroadcast(mContext);
                    StartupMenuUtil.updateDataStorage(mContext, pkgName);
                    mStartupMenuActivity.killStartupMenu();
                    break;
                case MotionEvent.BUTTON_TERTIARY:
                    break;
                case MotionEvent.BUTTON_SECONDARY:
                    mPositionItem = position;
                    strPkgName = StartupMenuActivity.mlistAppInfo.get(position).getPkgName();
                    mIsFullScreen = false;
                    if (position < 0 || position >= mlistAppInfo.size())
                        return false;
                    showMenuDialog1(position,motionEvent);
                    break;
                default :
                    StartupMenuActivity.setFocus(false);
                    break;
                }
                return false;
                }
            });
        view.setOnHoverListener(hoverListener);
        return view;
    }

    public static void openAppBroadcast(Context context) {
        Intent openAppIntent = new Intent();
        openAppIntent.setAction(Constants.ACTION_OPEN_APPLICATION);
        context.sendBroadcast(openAppIntent);
    }

    View.OnHoverListener hoverListener = new View.OnHoverListener() {
        public boolean onHover(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.setBackgroundResource(R.color.app_background);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setBackgroundResource(android.R.color.transparent);
                    break;
            }
            return false;
        }
    };

    private void showMenuDialog1(int position,MotionEvent motionEvent){
        StartupMenuActivity.mStartMenuDialog.setPosition(position);
        int[] location = new int[2];
        //((StartupMenuActivity)infater).mBackBtn.getLocationOnScreen(location);
        StartMenuDialog startMenuDialog = new StartMenuDialog(mContext, R.style.dialog);
        startMenuDialog.showDialog((int)motionEvent.getRawX() - location[0]
                    ,(int)motionEvent.getRawY() - location[1] + START_MENU_RIGHT_MOUSE_UI_NUMBER
                    , mStartMenuAppWidth, mStartMenuAppHeight);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    class ViewHolder {
        ImageView appIcon;
        TextView tvAppLabel;
      //TextView tvPkgName;

        public ViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.package_image);
            this.tvAppLabel = (TextView) view.findViewById(R.id.package_name);
        //  this.tvPkgName = (TextView) view.findViewById(R.id.tvPkgName);
        }
    }

    public StartupMenuActivity getStartupMenuActivity() {
        return (StartupMenuActivity) mContext;
    }

    /**
     * click item to open app.
     * reused in {@StartupMenuUsuallyAdapter}
     * */
    public void setClickItem(int position, List<AppInfo> mlistAppInfo) {
        String pkgName = StartupMenuActivity.mlistAppInfo.get(position).getPkgName();
        Intent intent = StartupMenuActivity.mlistAppInfo.get(position).getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
