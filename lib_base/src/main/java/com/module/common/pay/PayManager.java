package com.module.common.pay;

import android.widget.Toast;
import com.module.common.BaseLib;
import com.module.common.pay.alipay.Alipay;
import com.module.common.pay.weixin.WXPay;

/**
 * 微信支付、支付宝支付封装使用demo
 *
 * 微信支付
 * 支付流程参见https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=8_3
 * 商户系统和微信支付系统主要交互说明：
 步骤1：用户在商户APP中选择商品，提交订单，选择微信支付。
 步骤2：商户后台收到用户支付单，调用微信支付统一下单接口。参见【统一下单API】。
 步骤3：统一下单接口返回正常的prepay_id，再按签名规范重新生成签名后，将数据传输给APP。
 参与签名的字段名为appid，partnerid，prepayid，noncestr，timestamp，package。注意：package的值格式为Sign=WXPay
 步骤4：商户APP调起微信支付。api参见本章节【app端开发步骤说明】
 步骤5：商户后台接收支付通知。api参见【支付结果通知API】
 步骤6：商户后台查询支付结果。，api参见【查询订单API】


 微信客户端支付完成后会返回给你客户端一个支付结果。
 同时微信的服务端会主动调用你服务端的接口发送支付结果通知。
 逻辑处理应该是你服务端接收到支付结果后处理，比如修改订单状态，发货等等。
 不能依赖客户端的返回结果认为支付成功，是不可靠的，微信的文档也是这么建议的。
 你前后端的时序可以这样。客户端支付完成收到支付结果后，在一定时间内不断轮询查询服务端订单的状态有没有修改。
 （比如5s内每s查询一次）这样以服务端的交易状态为准（参见微信流程图）
 */
public class PayManager {

    /**
     * 微信支付
     * @param pay_param 支付服务生成的支付参数
     */
    private void doWXPay(String pay_param) {
        String wx_appid = "wxXXXXXXX";     //替换为自己的appid
        WXPay.init(BaseLib.getContext(), wx_appid);      //要在支付前调用
        WXPay.getInstance().doPay(pay_param, new WXPay.WXPayResultCallBack() {
            @Override
            public void onSuccess() {
                Toast.makeText(BaseLib.getContext(), "支付成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(BaseLib.getContext(), "支付取消", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int error_code) {
                switch (error_code) {
                    case WXPay.NO_OR_LOW_WX:
                        Toast.makeText(BaseLib.getContext(), "未安装微信或微信版本过低", Toast.LENGTH_SHORT).show();
                        break;
                    case WXPay.ERROR_PAY_PARAM:
                        Toast.makeText(BaseLib.getContext(), "参数错误", Toast.LENGTH_SHORT).show();
                        break;
                    case WXPay.ERROR_PAY:
                        Toast.makeText(BaseLib.getContext(), "支付失败", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 支付宝支付
     * @param pay_param 支付服务生成的支付参数
     */
    private void doAlipay(String pay_param) {
        new Alipay(BaseLib.getContext(), pay_param, new Alipay.AlipayResultCallBack() {
            @Override
            public void onSuccess() {
                Toast.makeText(BaseLib.getContext(), "支付成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDealing() {
                Toast.makeText(BaseLib.getContext(), "支付处理中...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(BaseLib.getContext(), "支付取消", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int error_code) {
                switch (error_code) {
                    case Alipay.ERROR_RESULT:
                        Toast.makeText(BaseLib.getContext(), "支付失败:支付结果解析错误", Toast.LENGTH_SHORT).show();
                        break;
                    case Alipay.ERROR_NETWORK:
                        Toast.makeText(BaseLib.getContext(), "支付失败:网络连接错误", Toast.LENGTH_SHORT).show();
                        break;
                    case Alipay.ERROR_PAY:
                        Toast.makeText(BaseLib.getContext(), "支付错误:支付码支付失败", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(BaseLib.getContext(), "支付错误", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }).doPay();
    }
}
