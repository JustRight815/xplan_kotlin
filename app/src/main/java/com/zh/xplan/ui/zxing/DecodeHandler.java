/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zh.xplan.ui.zxing;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.zh.xplan.R;
import com.zh.xplan.XPlanApplication;
import com.module.common.log.LogUtil;
import com.module.common.utils.PixelUtil;
import com.zh.xplan.ui.zxing.activity.CaptureActivity;
import com.zh.xplan.ui.zxing.camera.CameraManager;
import com.zh.zbar.Config;
import com.zh.zbar.Image;
import com.zh.zbar.ImageScanner;
import com.zh.zbar.Symbol;
import com.zh.zbar.SymbolSet;

import java.util.Hashtable;

final class DecodeHandler extends Handler {

	private static final String TAG = "Barcode_" + DecodeHandler.class.getSimpleName();

	private final CaptureActivity activity;
	private final MultiFormatReader multiFormatReader;

	DecodeHandler(CaptureActivity activity,
			Hashtable<DecodeHintType, Object> hints) {
		multiFormatReader = new MultiFormatReader();
		multiFormatReader.setHints(hints);
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case R.id.decode:
			// Log.d(TAG, "Got decode message");
            try {
				decodeZbar((byte[]) message.obj, message.arg1, message.arg2);
            }
            catch(ArrayIndexOutOfBoundsException e){
                e.printStackTrace();
            }
            catch(OutOfMemoryError e){
                e.printStackTrace();
            }
			break;
		case R.id.quit:
			Looper.myLooper().quit();
			break;
		}
	}

	/**
	 * Decode the data within the viewfinder rectangle, and time how long it
	 * took. For efficiency, reuse the same reader objects from one decode to
	 * the next.
	 * 
	 * @param data
	 *            The YUV preview frame.
	 * @param width
	 *            The width of the preview frame.
	 * @param height
	 *            The height of the preview frame.
	 */
	private void decode(byte[] data, int width, int height) {
		long start = System.currentTimeMillis();
		Result rawResult = null;
        PlanarYUVLuminanceSource source = null;

		if(data == null) {
			return;
		}
		
		// modify here
		byte[] rotatedData = new byte[data.length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++)
				rotatedData[x * height + height - y - 1] = data[x + y * width];
		}
		int tmp = width; // Here we are swapping, that's the difference to #11
		width = height;
		height = tmp;

        try {
            source = CameraManager.get().buildLuminanceSource(rotatedData, width, height);
        }
        /*catch (IllegalArgumentException e){
            Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
            message.sendToTarget();
            return;
        } */
        catch (Exception e){
            Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
            message.sendToTarget();
            return;
        }

		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

		if(bitmap == null || multiFormatReader == null) {
			return;
		}

		try {
			rawResult = multiFormatReader.decodeWithState(bitmap);
		} catch (Exception re) {
			// continue
		} finally {
			multiFormatReader.reset();
		}

		if (rawResult != null) {
			long end = System.currentTimeMillis();
			LogUtil.e(TAG, "ZXing 扫描结果 (" + (end - start) + " ms):\n"
					+ rawResult.toString());
			Message message = Message.obtain(activity.getHandler(),
					R.id.decode_succeeded, rawResult.getText());
			Bundle bundle = new Bundle();
			bundle.putParcelable(DecodeThread.BARCODE_BITMAP,
					source.renderCroppedGreyscaleBitmap());
			message.setData(bundle);
			// Log.d(TAG, "Sending decode succeeded message...");
			message.sendToTarget();
		} else {
			Message message = Message.obtain(activity.getHandler(),
					R.id.decode_failed);
			message.sendToTarget();
		}
	}

	/**
	 * 替换使用zbar处理
	 * @param data
	 *            The YUV preview frame.
	 * @param width
	 *            The width of the preview frame.
	 * @param height
	 *            The height of the preview frame.
	 */
	private void decodeZbar(byte[] data, int width, int height) {
		long start = System.currentTimeMillis();
		if(data == null) {
			return;
		}
		ImageScanner scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);
		Image barcode = new Image(width, height, "Y800");
		Rect rect = new Rect(CameraManager.get().getFramingRect());
//		scan_barcode.setCrop(rect.top, rect.left , rect.bottom - 200, rect.right - 200); //左边和下边不准  不加的话则全屏识别
		barcode.setCrop(rect.top, rect.left , rect.width() + PixelUtil.dp2px(50, XPlanApplication.getInstance()), rect.height());
		barcode.setData(data);

		int result = scanner.scanImage(barcode);
		String strResult="";
		if (result != 0){
			SymbolSet syms = scanner.getResults();
			for (Symbol sym : syms){
				strResult=sym.getData().trim();
				if(!strResult.isEmpty())
				{
					break;
				}
			}
		}

		if(!strResult.isEmpty()){
			//识别成功
			 long end = System.currentTimeMillis();
			LogUtil.e(TAG, "ZBar 扫描结果(" + (end - start) + " ms):\n"
					+ strResult);
			Message message = Message.obtain(activity.getHandler(), R.id.decode_succeeded, strResult);//Message信息传来传去,有点绕
			message.sendToTarget();
		}else {
			// 识别失败，直接继续扫描，或者用执行再识别一次
			decode(data, width, height);
//			Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
//			message.sendToTarget();
		}
	}

}
