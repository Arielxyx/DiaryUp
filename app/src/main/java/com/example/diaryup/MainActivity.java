package com.example.diaryup;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryup.adapter.MyRecyclerViewAdapter;
import com.example.diaryup.bean.Diary;
import com.example.diaryup.data.CallAlarm;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button addBtn;// 添加按钮
    private Button searchBtn;
    private Button dataSearchBtn;
    private List<Diary> diaryList = new ArrayList<>();
    public RecyclerView myRecyclerView;//定义RecyclerView
    private MyRecyclerViewAdapter myRecyclerViewAdapter;//自定义recyclerveiw的适配器
    public AlarmManager am;// 消息管理者
    public static MediaPlayer mediaPlayer;// 音乐播放器
    public static Vibrator vibrator;//手机震动器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LitePal.getDatabase();

        //2.2相关权限申请2.0
        if (ActivityCompat.checkSelfPermission( MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission( MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission( MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( MainActivity.this,
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE ,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, 123);
            return;
        }

        addBtn = (Button)findViewById(R.id.add_btn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                intent.putExtra("editModel", "newAdd");
                startActivity(intent);
            }
        });

        searchBtn = (Button)findViewById(R.id.bt_search);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
//                intent.putExtra("editModel", "newAdd");
                startActivity(intent);
            }
        });

        dataSearchBtn = (Button)findViewById(R.id.bt_data_search);
        dataSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DataSearchActivity.class);
//                intent.putExtra("editModel", "newAdd");
                startActivity(intent);
            }
        });

        if (am == null) {
            am = (AlarmManager) getSystemService(ALARM_SERVICE);
        }
        try {
            Intent intent = new Intent(MainActivity.this, CallAlarm.class);
            PendingIntent sender = PendingIntent.getBroadcast(
                    MainActivity.this, 0, intent, 0);
            am.setRepeating(AlarmManager.RTC_WAKEUP, 0, 60 , sender);
            Log.d("ddd","am");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        // 显示记事列表
        initData();
        initRecyclerView();
    }

    //初始化所有俱乐部列表
    private void initData() {
        Log.d("user", "diary列表");

        diaryList = DataSupport.findAll(Diary.class);
        Log.d("user", diaryList.size()+"");
        if(diaryList.size()==0)
            Toast.makeText(getApplicationContext(), "空空如也(～￣▽￣)～", Toast.LENGTH_SHORT).show();
    }

    //初始化RecyclerView
    private void initRecyclerView() {
        //获取RecyclerView
        myRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        //创建adapter
        myRecyclerViewAdapter = new MyRecyclerViewAdapter(this, diaryList);
        //给RecyclerView设置adapter
        myRecyclerView.setAdapter(myRecyclerViewAdapter);
        //设置layoutManager,可以设置显示效果，是线性布局、grid布局，还是瀑布流布局
        // 参数是：上下文、列表方向（横向还是纵向）、是否倒叙
        myRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        //设置item的分割线
        myRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        //RecyclerView中没有item的监听事件，需要自己在适配器中写一个监听事件的接口。参数根据自定义
        myRecyclerViewAdapter.setOnItemClickListener(new MyRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(final View view, final Diary diary) {
                if (!diary.isLockType()) {//判断是否添加了秘密锁 false没有
                    Intent intent = new Intent(MainActivity.this,
                            AddActivity.class);//跳转到添加日记页
                    intent.putExtra("editModel", "update");//传递编辑信息
                    intent.putExtra("id", diary.getId());//传递id信息
                    startActivity(intent);//开始跳转
                } else {//有秘密锁
                    // 弹出输入密码框
                    inputTitleDialog(diary.getLock(), 0, diary.getId());
                }
            }
        });

        myRecyclerViewAdapter.setOnItemLongClickListener(new MyRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void OnItemLongClick(final View view, final Diary diary) {

                //实例化AlertDialog
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                //设置弹窗标题
                alertDialogBuilder.setTitle("选择操作");
                //设置弹窗图片
                alertDialogBuilder.setIcon(R.drawable.fenxiang);
                //设置弹窗选项内容
                alertDialogBuilder.setItems(R.array.itemOperation,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    // 编辑
                                    case 0:
                                        if (!diary.isLockType()) {//判断是否添加了秘密锁 false没有
                                            Intent intent = new Intent(MainActivity.this,
                                                    AddActivity.class);//跳转到添加日记页
                                            intent.putExtra("editModel", "update");//传递编辑信息
                                            intent.putExtra("id", diary.getId());//传递id信息
                                            startActivity(intent);//开始跳转
                                        } else {//有秘密锁
                                            // 弹出输入密码框
                                            inputTitleDialog(diary.getLock(), 0, diary.getId());
                                        }
                                        break;
                                    // 删除
                                    case 1:
                                        if (!diary.isLockType()) {// 判断是否是加密日记 false没有
                                            DataSupport.delete(Diary.class,diary.getId());
                                            myRecyclerViewAdapter.removeData();
                                        } else {//有秘密锁
                                            // 弹出输入密码框
                                            inputTitleDialog(diary.getLock(), 1, diary.getId());
                                        }
                                        break;
                                }
                            }
                        });
                alertDialogBuilder.create();//创造弹窗
                alertDialogBuilder.show();//显示弹窗
            }
        });
    }

    // 加密日记打开弹出的输入密码框
    public void inputTitleDialog(final String lock, final int idType, final int id) {// 密码输入框
        final EditText inputServer = new EditText(this);
        inputServer.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputServer.setFocusable(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入密码").setView(inputServer)
                .setNegativeButton("取消", null);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                String inputName = inputServer.getText().toString();
                if ("".equals(inputName)) {
                    Toast.makeText(MainActivity.this, "密码不能为空请重新输入！",
                            Toast.LENGTH_LONG).show();
                } else {
                    if (inputName.equals(lock)) { //密码匹配
                        if (0 == idType) { //弹出提示框选择编辑则打开编辑页面
                            Intent intent = new Intent(MainActivity.this,
                                    AddActivity.class);
                            intent.putExtra("editModel", "update");
                            intent.putExtra("id", id);
                            startActivity(intent);
                        } else if (1 == idType) { //弹出提示框选择删除则删除数据库相关内容
                            DataSupport.delete(Diary.class,id);
                            myRecyclerViewAdapter.removeData();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "密码不正确！",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        builder.show();
    }


}
