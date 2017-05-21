package com.android.startupmenu.adapter;

import android.content.Context;
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
import com.android.startupmenu.util.AppInfo;
import com.android.startupmenu.util.StartupMenuSqliteOpenHelper;

import java.util.List;

public class StartupMenuUsuallyAdapter extends BaseAdapter {
    public static final int START_MENU_RIGHT_MOUSE_UI_NUMBER = 57;

    private List<AppInfo> mlistViewAppInfo = null;
    LayoutInflater mInfater = null;

    private Context mContext;
    private SQLiteDatabase mdb;
    private StartupMenuSqliteOpenHelper mMsoh;
    private int mStartMenuCommonlWidth;
    private int mStartMenuCommonlHeight;
    private StartupMenuActivity mStartupMenuActivity;

    public StartupMenuUsuallyAdapter(Context context, List<AppInfo> apps) {
        mInfater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mlistViewAppInfo = apps;
        mContext=context;
        mStartMenuCommonlWidth = mContext.getResources()
                                  .getDimensionPixelSize(R.dimen.start_menu_commonl_width);
        mStartMenuCommonlHeight = mContext.getResources()
                                  .getDimensionPixelSize(R.dimen.start_menu_commonl_height);
        mMsoh = new StartupMenuSqliteOpenHelper(mContext, "StartupMenu_database.db", null, 1);
        mdb = mMsoh.getWritableDatabase();
        mStartupMenuActivity = getStartupMenuActivity();
    }

    @Override
    public int getCount() {
        return mlistViewAppInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mlistViewAppInfo.get(position);
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
            view = mInfater.inflate(R.layout.activity_listview_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertview;
            holder = (ViewHolder) convertview.getTag();
        }
        AppInfo appInfo = (AppInfo) getItem(position);
        holder.appIcon.setImageDrawable(appInfo.getAppIcon());
        final String appName = appInfo.getAppLabel();
        holder.tvAppLabel.setText(appInfo.limitNameLength(appName, mContext, appInfo));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartupMenuActivity.setClickItem(mContext, position, mlistViewAppInfo);
            }
        });
        /**
         *
         view.setOnGenericMotionListener(new View.OnGenericMotionListener() {
        @Override
        public boolean onGenericMotion(View view, MotionEvent motionEvent) {
        int what = motionEvent.getButtonState();
        StartupMenuActivity.setFocus(true);
        switch (what) {
        case MotionEvent.BUTTON_PRIMARY:
        String pkgName = StartupMenuActivity.mlistViewAppInfo
        .get(position).getPkgName();
        Intent intent = StartupMenuActivity.mlistViewAppInfo
        .get(position).getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        StartupMenuAdapter.openAppBroadcast(mContext);
        StartupMenuUtil.updateDataStorage(mContext, pkgName);
        break;
        case MotionEvent.BUTTON_TERTIARY:
        break;
        case MotionEvent.BUTTON_SECONDARY:
        if (position < 0 || position >= mlistViewAppInfo.size()) {
        return false;
        }
        showMenuDialog(position,motionEvent);
        break;
        default :
        StartupMenuActivity.setFocus(false);
        break;
        }
        return false;
        }
        });
         view.setOnHoverListener(hoverListener);*/
        return view;
    }

    View.OnHoverListener hoverListener = new View.OnHoverListener() {
        public boolean onHover(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.setBackgroundResource(R.drawable.power_background);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setBackgroundResource(R.color.appUsuallyBackground);
                    break;
            }
            return false;
        }
    };

    private void showMenuDialog(int position,MotionEvent motionEvent){
        StartupMenuActivity.mStartMenuUsuallyDialog.setPosition(position);
        int[] location = new int[2];
        //((StartupMenuActivity)infater).mBackBtn.getLocationOnScreen(location);
        StartupMenuActivity.mStartMenuUsuallyDialog.showDialog(
                (int)motionEvent.getRawX() - location[0],
                (int)motionEvent.getRawY() - location[1] + START_MENU_RIGHT_MOUSE_UI_NUMBER,
                mStartMenuCommonlWidth, mStartMenuCommonlHeight);
    }

    class ViewHolder {
        ImageView appIcon;
        TextView tvAppLabel;

        public ViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.list_package_image);
            this.tvAppLabel = (TextView) view.findViewById(R.id.list_package_name);
        }
    }

    public StartupMenuActivity getStartupMenuActivity() {
        return (StartupMenuActivity) mContext;
    }
}
