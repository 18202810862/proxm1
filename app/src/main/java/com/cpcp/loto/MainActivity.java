package com.cpcp.loto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.cpcp.loto.activity.WebPageActivity;
import com.cpcp.loto.base.BaseActivity;
import com.cpcp.loto.broadcast.LocalBroadcastManager;
import com.cpcp.loto.entity.WebFlag;
import com.cpcp.loto.fragment.ForumFragment;
import com.cpcp.loto.fragment.HomeFragment;
import com.cpcp.loto.fragment.BetFragment;
import com.cpcp.loto.fragment.MeFragment;
import com.cpcp.loto.uihelper.LoadingDialog;
import com.cpcp.loto.util.ExampleUtil;
import com.cpcp.loto.util.LogUtils;

import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.jpush.android.api.JPushInterface;

/**
 * @date 创建时间：2017/5/25
 * @description 首页
 */

public class MainActivity extends BaseActivity implements BottomNavigationBar.OnTabSelectedListener {

    @BindView(R.id.bottom_navigation_bar)
    BottomNavigationBar mBottomNavigationBar;

    public BottomNavigationBar getmBottomNavigationBar() {
        return mBottomNavigationBar;
    }

    /**
     * 记录退出按键的最后时刻，初始为0
     */
    private long lastTime = 0;

    //消息数量提示--消息列表
    private BadgeItem badgeItem;

    //各个Fragment操作页面
    public Fragment currentFragment;
    private HomeFragment homeFragment;
    private BetFragment betFragment;
    private MeFragment meFragment;
    private ForumFragment forumFragment;
    //
    public boolean isLogin = false;
    private int currentPage = 0;

    //极光推送记录
    public static boolean isForeground = false;
    //for receive customer msg from jpush server
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    //
    private final int personalPage = 2;

    @Override
    public void getIntentData() {
        super.getIntentData();

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        setTitle("六合彩库");
        initTab();
        registerMessageReceiver();  // used for receive msg
        init();
    }

    @Override
    protected void onResume() {
        isForeground = true;
        super.onResume();
    }


    /**
     * 初始化tab
     */
    private void initTab() {

        mToolBar.setNavigationIcon(null);


        //
        mBottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        mBottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);

        /*** the setting for BadgeItem ***/
        badgeItem = new BadgeItem();
        badgeItem.setHideOnSelect(false)
                .setText("")
                .setBackgroundColorResource(R.color.red)
                .setBorderWidth(0);

        /*** the setting for BottomNavigationBar ***/
        mBottomNavigationBar.
                addItem(new BottomNavigationItem(R.drawable.tab_home_checked, "六合彩库").
                        setInactiveIconResource(R.drawable.tab_home_normal).setActiveColorResource(R.color.whiteTab).
                        setInActiveColorResource(R.color.grayText))
                .addItem(new BottomNavigationItem(R.drawable.tab_bet_checked, "彩民投票").
                        setInactiveIconResource(R.drawable.tab_bet_normal).setActiveColorResource(R.color.whiteTab).
                        setInActiveColorResource(R.color.grayText))

                .addItem(new BottomNavigationItem(R.drawable.tab_forum_checked, "交流论坛").
                        setInactiveIconResource(R.drawable.tab_forum_normal).setActiveColorResource(R.color.whiteTab).
                        setInActiveColorResource(R.color.grayText))
                .addItem(new BottomNavigationItem(R.drawable.tab_me_checked, "个人中心").
                        setInactiveIconResource(R.drawable.tab_me_normal).setActiveColorResource(R.color.whiteTab).
                        setInActiveColorResource(R.color.grayText))//.setBadgeItem(badgeItem)
                .initialise();
        mBottomNavigationBar.setTabSelectedListener(this);
        mBottomNavigationBar.selectTab(currentPage);
    }

    @Override
    protected void initData() {
//        getMobData();
    }

    /**
     * 获取mob后台数据
     */
    private void getMobData() {
        BmobQuery<WebFlag> query = new BmobQuery<>();
        //执行查询，第一个参数为上下文，第二个参数为查找的回调
        LoadingDialog.showDialog(mActivity);
        query.findObjects(new FindListener<WebFlag>() {
            @Override
            public void done(List<WebFlag> list, BmobException e) {
                LoadingDialog.closeDialog(mActivity);
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        WebFlag webFlag = list.get(0);
                        if (webFlag.isFlag() == true) {
//                            Intent intent=new Intent(mContext,MainActivity.class);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
                            //
                            Bundle bundle = new Bundle();
                            bundle.putString("name", "彩票");
                            bundle.putString("url", webFlag.getUrl());
                            jumpToActivity(WebPageActivity.class, bundle, true);
                        }
                        LogUtils.i(TAG, "查询成功：" + webFlag.getUrl());
                    }
                } else {
                    LogUtils.i(TAG, "查询失败：" + e.getMessage());
                }
            }
        });


    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //处理回退键，
            long curTime = System.currentTimeMillis();
            if (curTime - lastTime >= 2000) {
                lastTime = curTime;
                Toast.makeText(this, "在按一次退出", Toast.LENGTH_SHORT).show();
                return true;
            }
            this.finish();
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onTabSelected(int position) {


        switch (position) {
            case 0:
                this.getWindow().getDecorView().setFitsSystemWindows(true);
                showToolbar();
                setTitle("六合彩库");

                if (homeFragment == null) {
                    homeFragment = HomeFragment.newInstance();
                }
                jumpFragment(currentFragment, homeFragment, R.id.sub_content, "六合彩库");
                currentFragment = homeFragment;
                break;

            case 1:
                this.getWindow().getDecorView().setFitsSystemWindows(true);
                showToolbar();
                setTitle("彩民投票");

                if (betFragment == null) {
                    betFragment = BetFragment.newInstance();
                }
                jumpFragment(currentFragment, betFragment, R.id.sub_content, "彩民投票");
                currentFragment = betFragment;
                break;

            case 2:
                this.getWindow().getDecorView().setFitsSystemWindows(true);
                showToolbar();
                setTitle("交流论坛");
                if (forumFragment == null) {
                    forumFragment = ForumFragment.newInstance();
                }
                jumpFragment(currentFragment, forumFragment, R.id.sub_content, "交流论坛");
                currentFragment = forumFragment;
                break;
            case 3:
                this.getWindow().getDecorView().setFitsSystemWindows(false);
                hideToolbar();
//                setTitle("个人中心");
                if (meFragment == null) {
                    meFragment = MeFragment.newInstance();
                }
                jumpFragment(currentFragment, meFragment, R.id.sub_content, "个人中心");
                currentFragment = meFragment;
                break;
        }
    }

    @Override
    public void onTabUnselected(int position) {
        LogUtils.i(TAG, "onTabUnselected");
    }

    @Override
    public void onTabReselected(int position) {
        LogUtils.i(TAG, "onTabReselected");

    }

    //极光相关

    // 初始化 JPush。如果已经初始化，但没有登录成功，则执行重新登录。
    private void init() {
        JPushInterface.init(getApplicationContext());
    }


    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                    String messge = intent.getStringExtra(KEY_MESSAGE);
                    String extras = intent.getStringExtra(KEY_EXTRAS);
                    StringBuilder showMsg = new StringBuilder();
                    showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                    if (!ExampleUtil.isEmpty(extras)) {
                        showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                    }
                    setCostomMsg(showMsg.toString());
                }
            } catch (Exception e) {
            }
        }
    }

    private void setCostomMsg(String msg) {
        LogUtils.i(TAG, "极光推送消息" + msg);
    }


    @Override
    protected void onPause() {
        isForeground = false;//推送判断是否在前台
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
}
