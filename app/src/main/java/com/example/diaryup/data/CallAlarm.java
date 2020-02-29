package com.example.diaryup.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import com.example.diaryup.bean.Diary;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 更新数据库提示信息
 */
public class CallAlarm extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d("ddd","broadcast");
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);

		List<Diary> diaryList = DataSupport.findAll(Diary.class);
		for (Diary diary : diaryList) {
			if (diary.isDataType() && str.equals(diary.getDataTime())) { //设置了提醒 并且 现在到了提醒时间
				diary.setToDefault("dataType");
				diary.setDataTime("0");
				diary.update(diary.getId());
				Intent myIntent = new Intent(context, AlarmAlert.class);
				Bundle bundleRet = new Bundle();
				bundleRet.putString("remindMsg", diary.getTitle());
				bundleRet.putBoolean("shake", true);
				bundleRet.putBoolean("ring", true);
				myIntent.putExtras(bundleRet);
				myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(myIntent);
			}
		}
	}
}
