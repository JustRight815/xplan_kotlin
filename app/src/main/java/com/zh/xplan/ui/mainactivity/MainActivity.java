package com.zh.xplan.ui.mainactivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SwitchCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;
import com.module.common.log.LogUtil;
import com.module.common.view.bottombar.BottomBar;
import com.module.common.view.bottombar.BottomBarItem;
import com.module.common.view.bottombar.BottomBarTextColorAttrHandler;
import com.module.common.view.snackbar.SnackbarUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;
import com.zh.xplan.R;
import com.zh.xplan.XPlanApplication;
import com.zh.xplan.ui.aboutapp.AboutAppActivity;
import com.zh.xplan.ui.base.BaseActivity;
import com.zh.xplan.ui.base.BaseFragment;
import com.zh.xplan.ui.base.FragmentsFactory;
import com.zh.xplan.ui.citypicker.CityPickerActivity;
import com.zh.xplan.ui.iptoolsactivity.IpToolsActivity;
import com.zh.xplan.ui.menuvideo.kaiyanonlinevideo.KaiYanOnlineVideoFragment;
import com.zh.xplan.ui.pulltorefreshdemo.PullToRefreshDemoActivity;
import com.zh.xplan.ui.skin.SkinChangeHelper;
import com.zh.xplan.ui.skin.SkinConfigHelper;
import com.zh.xplan.ui.webviewActivity.WeatherDetailsActivity;

import org.qcode.qskinloader.SkinManager;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * 主界面
 */
public class MainActivity extends BaseActivity
        implements View.OnClickListener,MainView {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private long ExitTime; // 延时退出应用时间变量
    //头部布局
    private TextView header_tv_temperature;
    private TextView header_tv_city_name;
    private String cityName;
    private static FragmentManager mFragmentManager;
    private static BaseFragment mCurrentFragment;// 当前FrameLayout中显示的Fragment
    private static Boolean isFirst = true;// 是否是第一次进入应用
    private SwitchCompat night_mode_switch;
    private BottomBar mBottomBar;
    private MainPresenter mainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SkinManager.getInstance().registerSkinAttrHandler(
                BottomBarTextColorAttrHandler.BOTTOM_BAR_TEXT_COLOR, new BottomBarTextColorAttrHandler());
        setContentView(R.layout.activity_main);
        setSwipeBackEnable(false);
        mFragmentManager = getSupportFragmentManager();
        initViews();

        initDatas();
        int badgeCount = 10;
        ShortcutBadger.applyCount(this, badgeCount); //for 1.1.4+
    }

    @Override
    public boolean isSupportSwipeBack() {
        return false;
    }

    @Override
    public boolean isSupportSkinChange() {
        return true;
    }

    @Override
    public boolean isSwitchSkinImmediately() {
        return true;
    }

    private void initViews() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        StatusBarUtil.setColorNoTranslucentForDrawerLayout(MainActivity.this, drawerLayout, getResources().getColor(R.color.colorPrimaryDark));
//        navigationView = (NavigationView) findViewById(R.id.nav_view);
//        View headerView = navigationView.getHeaderView(0);
        View headerView = drawerLayout;
        header_tv_temperature = (TextView) headerView.findViewById(R.id.header_tv_temperature);
        header_tv_city_name = (TextView) headerView.findViewById(R.id.header_tv_city_name);
        header_tv_city_name.setOnClickListener(this);
        header_tv_temperature.setOnClickListener(this);
        headerView.findViewById(R.id.tv_temperature_point).setOnClickListener(this);
        headerView.findViewById(R.id.ll_city_layout).setOnClickListener(this);
        headerView.findViewById(R.id.ll_refresh).setOnClickListener(this);
        headerView.findViewById(R.id.ll_about).setOnClickListener(this);
        headerView.findViewById(R.id.ll_ip_tools).setOnClickListener(this);
        headerView.findViewById(R.id.ll_night_mode).setOnClickListener(this);
        night_mode_switch = (SwitchCompat) headerView.findViewById(R.id.night_mode_switch);
        initBottomMenus();
        changeNightModeSwitch();
        night_mode_switch.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(SkinConfigHelper.isDefaultSkin() == isChecked ){
                    LogUtil.e("zh","onCheckedChanged  " + isChecked);
                    SkinChangeHelper.getInstance().switchSkinMode(
                            new SkinChangeHelper.OnSkinChangeListener() {
                                @Override
                                public void onSuccess() {
                                    LogUtil.e("zh","换肤成功");
                                    changeStatusBarColor();
                                }

                                @Override
                                public void onError() {
                                    LogUtil.e("zh","换肤失败");
                                }
                            }
                    );
                }
            }
        });

    }

    /**
     * 初始化底部菜单的图片。（也可以扩展动态更新菜单文字）
     */
    private void initBottomMenus() {
        mBottomBar = (BottomBar) findViewById(R.id.bottom_bar);
        mBottomBar.init(null);
        mBottomBar.setOnItemSelectedListener(new BottomBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(BottomBarItem bottomBarItem, int position) {
                GSYVideoPlayer.releaseAllVideos();
                switch (position) {
                    case 0:
                        changeStatusBarColor();
                        setFragment(mCurrentFragment,
                                FragmentsFactory.createFragment(FragmentsFactory.MENU_PICTURE));
                        break;
                    case 1:
                        changeStatusBarColor();
                        setFragment(mCurrentFragment,
                                FragmentsFactory.createFragment(FragmentsFactory.MENU_VIDEO));
                        break;
                    case 2:
                        changeStatusBarColor();
                        setFragment(mCurrentFragment,
                                FragmentsFactory.createFragment(FragmentsFactory.MENU_TOU_TIAO));
                        break;
                    case 3:
                        changeStatusBarColor();
                        setFragment(mCurrentFragment,
                                FragmentsFactory.createFragment(FragmentsFactory.MENU_SETTING));
                        break;
                    default:
                        break;
                }
            }
        });
        // 设置默认选中第一个菜单
//        mBottomBar.getBottomItem(0).performClick();
        mBottomBar.setCurrentItem(0);
        mBottomBar.setMsg(3,"NEW");//设置第四个页签显示NEW提示文字
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_tv_temperature:// 打开web天气预报
                Intent intent = new Intent(this,
                        WeatherDetailsActivity.class);
                intent.putExtra("URL", "http://www.uc123.com/tianqi.html");
                startActivity(intent);
                break;
            case R.id.tv_temperature_point:// 打开web天气预报
                Intent intent1 = new Intent(this,
                        WeatherDetailsActivity.class);
                intent1.putExtra("URL", "http://www.uc123.com/tianqi.html");
                startActivity(intent1);
                break;
            case R.id.header_tv_city_name:// 城市选择
                Intent cityIntent = new Intent(this, CityPickerActivity.class);
                startActivityForResult(cityIntent, 0);
                break;
            case R.id.ll_refresh:// 下拉刷新
                startActivity(new Intent(this,PullToRefreshDemoActivity.class));
                break;
            case R.id.ll_about:// 关于软件
                startActivity(new Intent(this,AboutAppActivity.class));
                break;
            case R.id.ll_ip_tools:// ip工具
                startActivity(new Intent(this,IpToolsActivity.class));
                break;
            case R.id.ll_night_mode:// 夜间模式
                SkinChangeHelper.getInstance().switchSkinMode(
                    new SkinChangeHelper.OnSkinChangeListener() {
                        @Override
                        public void onSuccess() {
                            LogUtil.e(TAG,"换肤成功");
                            changeStatusBarColor();
                            changeNightModeSwitch();
                        }
                        @Override
                        public void onError() {
//                            LogUtil.e(TAG,"换肤失败");
                        }
                    }
                );
                break;
            default:
                break;
        }
    }

    public void closeDrawerDelay() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            },320);
        }
    }

    public void closeDrawer() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void openDrawer() {
        if(drawerLayout != null){
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public void initDatas() {
        mainPresenter = new MainPresenter();
        mainPresenter.attachView(this);
        mainPresenter.getCityWeather("","");
        mainPresenter.updateAdPicture();
    }

    private void changeStatusBarColor(){
        int color = SkinConfigHelper.isDefaultSkin() ? getResources().getColor(R.color.colorPrimaryDark) : getResources().getColor(R.color.colorPrimaryDark_night);
        if(mBottomBar.getCurrentItem() == 2){
            color = SkinConfigHelper.isDefaultSkin() ? getResources().getColor(R.color.btn_pressed_grey_solid) : getResources().getColor(R.color.colorPrimaryDark_night);
        }
        StatusBarUtil.setColorNoTranslucentForDrawerLayout(MainActivity.this, drawerLayout, color);
    }

    private void changeNightModeSwitch(){
        if(SkinConfigHelper.isDefaultSkin()){
            night_mode_switch.setChecked(false);
        }else{
            night_mode_switch.setChecked(true);
        }
    }

    /**
     * 切换显示不同的fragment
     */
    public static  void setFragment(Fragment fromFragment, Fragment toFragment) {
        if (isFirst == true) {
            // 如果是第一次进入应用，把菜单1对应的fragment加载进去，并显示
            FragmentTransaction transaction = mFragmentManager
                    .beginTransaction();
            transaction.add(R.id.id_content, toFragment).commit();
            mCurrentFragment = (BaseFragment) toFragment;
            isFirst = false;
            return;
        }
        if (mCurrentFragment != toFragment) {
            // 隐藏之前的fragment,显示下一个fragment
            mCurrentFragment = (BaseFragment) toFragment;
            FragmentTransaction transaction = mFragmentManager
                    .beginTransaction();
            if (!toFragment.isAdded()) {
                transaction.hide(fromFragment).add(R.id.id_content, toFragment).commit();
            } else {
                transaction.hide(fromFragment).show(toFragment).commit();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == 66 && data != null) {
            String cityName = data.getStringExtra(CityPickerActivity.KEY_PICKED_CITY);
            if (cityName != null && header_tv_city_name != null) {
                if(! cityName.equals(this.cityName)){
                    this.cityName = cityName;
                    header_tv_city_name.setText(cityName);
                    mainPresenter.getCityWeather(null, cityName);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showCityWeather(String cityName, String temperature) {
        header_tv_city_name.setText(cityName);
        header_tv_temperature.setText(temperature);
    }

    @Override
    public void isShowLoading(boolean isShow, String message) {

    }

    /**
     * 按两下返回键退出程序
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            //如果侧滑菜单是打开的，则关闭
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

            //如果正在全屏播放视频则关闭全屏视频，不提示退出程序
            BaseFragment curFragment =  FragmentsFactory.getFragment(4);
            if ( curFragment  instanceof KaiYanOnlineVideoFragment) {
                LogUtil.e("zh","curFragment  instanceof " );
                KaiYanOnlineVideoFragment KaiYanOnlineVideoFragment = (KaiYanOnlineVideoFragment) curFragment;
                if(KaiYanOnlineVideoFragment.onBackPressed()){
                    LogUtil.e("zh","curFragment  onBackPressed " );
                    return true;
                }
            }

            if ((System.currentTimeMillis() - ExitTime) > 2000) {
                // 自定义Toast的样式
//                Toast toast = new Toast(this);
//                View view = LayoutInflater.from(this).inflate(
//                        R.layout.toast_exit_app, null);
//                TextView textView = (TextView) view
//                        .findViewById(R.id.tv_exit_toast);
//                textView.setText("再按一次退出程序");
//                toast.setView(view);
//                toast.setDuration(Toast.LENGTH_SHORT);
//                toast.show();
                // 防止通知栏关闭后Toast不提示
                SnackbarUtils.ShortToast(mBottomBar,"再按一次退出程序");
                ExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if(mainPresenter != null){
            mainPresenter.onDestory();
        }
        XPlanApplication.getInstance().destroyApp();
        super.onDestroy();
    }
}
