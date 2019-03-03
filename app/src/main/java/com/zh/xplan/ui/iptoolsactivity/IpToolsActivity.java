package com.zh.xplan.ui.iptoolsactivity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.module.common.view.snackbar.SnackbarUtils;
import com.zh.xplan.R;
import com.zh.xplan.ui.base.BaseActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * IP获取，ping测试
 */
public class IpToolsActivity extends BaseActivity implements View.OnClickListener{
	private View mContentView;
	private Button btn_ping;
	private EditText ed_ip;
	private TextView tv_ip,tv_info;
	private LinearLayout ll_loading;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContentView = View.inflate(this, R.layout.activity_ip_tools,null);
		setContentView(mContentView);
		initTitle();
		btn_ping = (Button) findViewById(R.id.btn_ping);
		tv_info = (TextView) findViewById(R.id.tv_info);
		tv_ip = (TextView) findViewById(R.id.tv_ip);
		ed_ip = (EditText) findViewById(R.id.ed_ip);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		String ip = getPhoneIp();
		tv_ip.setText("本机IP: " + ip);
		btn_ping.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ping();
			}
		});
	}

	private void initTitle() {
		findViewById(R.id.title_bar_back).setOnClickListener(this);
		setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark),0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.title_bar_back:
				finish();
				break;
			default:
				break;
		}
	}


	public void ping() {
		tv_info.setText(null);
		final String ip = ed_ip.getText().toString();
		if (ip.isEmpty()) {
//			Toast.makeText(this, "ip地址不能空", Toast.LENGTH_SHORT).show();
			SnackbarUtils.ShortToast(mContentView,"ip地址不能空");
			return;
		}
		ll_loading.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					BufferedReader buf = getPing1(ip);
					if (buf == null) {
						runOnUiThread(new Runnable() {
							public void run() {
								tv_info.setText("faild");
								ll_loading.setVisibility(View.GONE);
							}
						});
						return;
					}
					String str = new String();
					String str2 = "";
					// 读出所有信息并显示
					while ((str = buf.readLine()) != null) {

						str = str + "\r\n";
						str2 += str;
						System.out.println(str);
					}
					final String str1 = str2;
					runOnUiThread(new Runnable() {
						public void run() {
							tv_info.setText(str1);
							ll_loading.setVisibility(View.GONE);
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	/**
	 * 测试ping ip地址，并返回结果
	 * @param IP
	 * @return
	 */
	public static BufferedReader getPing1(String IP) {
		Process p;
		try {
			p = Runtime.getRuntime().exec(
					"ping -c 2 -w 10 " + IP );
			int status = p.waitFor();

			if (status == 0) {
				System.out.println("success"); 
			} else {
				System.out.println("failed"); 
				return null;
			}
			String lost = new String();
			String delay = new String();
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			
			/*// 读出所有信息并显示
			while ((str = buf.readLine()) != null) {
				str = str + "\r\n";
				//tv_PingInfo.append(str);
				System.out.println(str);
			}*/
			return buf;
		} catch (Exception  e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	/**
	 * 获取手机ip地址
	 * 
	 * @return
	 */
	public static String getPhoneIp() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& inetAddress instanceof Inet4Address) {
						// if (!inetAddress.isLoopbackAddress() && inetAddress
						// instanceof Inet6Address) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception e) {
		}
		return "";
	}
}
