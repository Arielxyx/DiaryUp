package com.example.diaryup;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryup.bean.Diary;
import com.example.diaryup.data.DateTimePickerDialog;
import com.example.diaryup.view.LineEditText;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddActivity extends AppCompatActivity {

    private RelativeLayout data_rl;
    private TextView data_tv;
    private TextView tv_title;
    private Button backBtn;
    private Button saveBtn;
    private LineEditText et_Notes;
    private LineEditText et_Title;
    private ImageButton ib_lk;
    private ScrollView sc_lv;
    private GridView bottomMenu;
    // 底部按钮菜单按钮图片集合
    private int[] bottomItems = { R.drawable.tabbar_microphone,
            R.drawable.tabbar_photo, R.drawable.tabbar_camera,
            R.drawable.tabbar_appendix};

    int diaryId;
    String title;
    String context;
    String time;
    String[] images;
    private List<Map<String, String>> imgList = new ArrayList<Map<String, String>>();
    public boolean dataType = false;// 判断是否开启记录开启了提醒功能
    public String dataTime = "0";// 提醒时间
    public boolean lockType = false;// 判断是否打开密码锁
    public String lock = "0";// 密码

    InputMethodManager imm;//控制手机键盘
    Intent intent;
    String editModel = null;
    Diary diary;
    String FXName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        tv_title = (TextView) findViewById(R.id.tv_title);
        backBtn = (Button) findViewById(R.id.bt_back);
        backBtn.setOnClickListener(new ClickEvent());
        saveBtn = (Button) findViewById(R.id.bt_save);
        saveBtn.setOnClickListener(new ClickEvent());
        et_Notes = (LineEditText) findViewById(R.id.et_note);
        et_Title = (LineEditText) findViewById(R.id.et_title);
        ib_lk = (ImageButton) findViewById(R.id.ib_lk);
        data_rl = (RelativeLayout) findViewById(R.id.data_rl);
        data_tv = (TextView) findViewById(R.id.data_tv);
        bottomMenu = (GridView)findViewById(R.id.bottomMenu);
        // 配置菜单
        initBottomMenu();
        // 为菜单设置监听器
        bottomMenu.setOnItemClickListener(new MenuClickEvent());

        // 默认关闭软键盘,可以通过失去焦点设置
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_Notes.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(et_Title.getWindowToken(), 0);

        intent = getIntent();
        editModel = intent.getStringExtra("editModel");
        diaryId = intent.getIntExtra("id",0);

        loadData();

        et_Notes.setOnClickListener(new TextClickEvent());
        et_Title.setOnClickListener(new TextClickEvent());
    }

    // 配置菜单
    private void initBottomMenu() {
        //菜单集合
        ArrayList<Map<String, Object>> menus = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < bottomItems.length; i++) {//循环菜单集合
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("image", bottomItems[i]);//循环图片集合添加到菜单中
            menus.add(item);//添加图片菜单到底部菜单
        }
        //菜单长度
        bottomMenu.setNumColumns(bottomItems.length);
        //底部菜单
        bottomMenu.setSelector(R.drawable.bottom_item);
        //实例化底部菜单适配器
        SimpleAdapter mAdapter = new SimpleAdapter(AddActivity.this, menus,
                R.layout.item_button, new String[]{"image"},
                new int[]{R.id.item_image});
        bottomMenu.setAdapter(mAdapter);//为底部菜单添加适配器
    }

    // 设置菜单项监听器
    class MenuClickEvent implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Intent intent;
            switch (position) {
//                // 手写
//                case 0:
//                    intent = new Intent(AddActivity.this, HandWriteActivity.class);
//                    startActivityForResult(intent, 5);
//                    break;
//                // 绘图
//                case 1:
//                    intent = new Intent(AddActivity.this, PaintActivity.class);
//                    startActivityForResult(intent, 3);
//                    break;
                // 语音
                case 0:
                    intent = new Intent(AddActivity.this, ActivityRecord.class);
                    startActivityForResult(intent, 4);
                    break;
                // 照片
                case 1: //相册
                    // 添加图片的主要代码
                    //点击图片的话会打开相册、如果相册没有图片会自动打开相机
                    intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            "image/*");
                    // 选中相片后返回本Activity
                    startActivityForResult(intent, 1);
                    break;
                // 拍照
                case 2:
                    if (Build.VERSION.SDK_INT >= 23) {

                        if(ContextCompat.checkSelfPermission(AddActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                            //申请权限
                            ActivityCompat.requestPermissions(AddActivity.this,
                                    new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    1);
                        }else {
                            // 调用系统拍照界面
                            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            // 区分选择相片
                            startActivityForResult(intent, 2);
                        }
                    } else {
                        // Pre-Marshmallow
                        // 调用系统拍照界面
                        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // 区分选择相片
                        startActivityForResult(intent, 2);
                    }
                    break;
                // 提醒设置
                case 3:
                    setReminder();
                    break;
            }
        }
    }

    // 加载数据
    private void loadData() {
        // 如果是新增记事模式，则将editText清空
        if (editModel.equals("newAdd")) {
            tv_title.setText("新增记事");
            et_Notes.setText("");
            et_Title.setText("");
        }
        // 如果编辑的是已存在的记事，则将数据库的保存的数据取出，并显示在EditText中
        else if (editModel.equals("update")) {
            tv_title.setText("编辑记事");
            diary = DataSupport.find(Diary.class, diaryId);
            context = diary.getContext();
            title = diary.getTitle();
            lockType = diary.isLockType();
            lock = diary.getLock();
            dataType = diary.isDataType();
            dataTime = diary.getDataTime();

            et_Title.setText(title);
//            et_Notes.setText(context);
            if (lockType)
                ib_lk.setBackgroundResource(R.drawable.locky);
            else
                ib_lk.setBackgroundResource(R.drawable.un_locky);
            if(!dataType)
                data_rl.setVisibility(View.GONE);
            else{
                data_rl.setVisibility(View.VISIBLE);
                data_tv.setText("提醒时间：" + dataTime);
            }
            // 定义正则表达式，用于匹配路径
            Pattern p = Pattern.compile("/([^\\.]*)\\.\\w{3}");
            Matcher m = p.matcher(context);
            int startIndex = 0;
            while (m.find()) {
                // 取出路径前的文字
                if (m.start() > 0) {
                    et_Notes.append(context.substring(startIndex, m.start()));
                }
                // 取出路径
                String path = m.group().toString();
                //Log.d("ddd","1---"+path);
                // 取出路径的后缀
                String type = path.substring(path.length() - 3, path.length());
                //Log.d("ddd",type);
                Bitmap bm ;
                Bitmap rbm ;
                // 判断附件的类型，如果是录音文件，则从资源文件中加载图片
                if (type.equals("amr")) {
                    bm = BitmapFactory.decodeResource(getResources(),
                            R.drawable.record_icon);
                    // 缩放图片
                    rbm = resize(bm, 300);
                } else {
                    // 取出图片
//                    path = path.substring(0, path.length() - 3)+"jpg";
                    bm = BitmapFactory.decodeFile(path);
                    if(bm==null)
                        Log.d("ddd",null+"");
                    // 缩放图片
                    rbm = resize(bm, 480);
                    // 为图片添加边框效果
                    rbm = getBitmapHuaSeBianKuang(rbm);
                }
                SpannableString ss = new SpannableString(m.group().toString());
                ImageSpan span = new ImageSpan(this, rbm);
                ss.setSpan(span, 0, m.end() - m.start(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                System.out.println(m.start() + "-------" + m.end());
                et_Notes.append(ss);
                startIndex = m.end();
                // 用List记录该录音的位置及所在路径，用于单击事件
                Map<String, String> map = new HashMap<String, String>();
                map.put("location", m.start() + "-" + m.end());
                map.put("path", path);
                imgList.add(map);
            }
            // 将最后一个图片之后的文字添加在TextView中
            et_Notes.append(context.substring(startIndex, context.length()));
//            dop.close_db();
        }
    }

    //数据回调方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri photoUri;
            photoUri = data.getData();
            //实例化数据表
            ContentResolver cr = AddActivity.this.getContentResolver();
            //图片用于储存选择后转换成Bitmap类型
            Bitmap bitmap = null;
            //接收返回信息
            Bundle extras = null;
            if (requestCode == 1) {
                if (data != null) {
                    // 取得选择照片的路径
                    String[] proj = {MediaStore.Images.Media.DATA};
                    //用于查询指定图片位置
                    Cursor actualimagecursor = managedQuery(photoUri, proj, null, null,
                            null);
                    //读取媒体数据库
                    int actual_image_column_index = actualimagecursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    //移动数据到第一个
                    actualimagecursor.moveToFirst();
                    //图片路径
                    String path = actualimagecursor.getString(actual_image_column_index);
                    Log.d("ddd","2---"+path);

                    try {
                        // 将对象存入Bitmap中
                        bitmap = BitmapFactory.decodeStream(cr.openInputStream(photoUri));
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // 插入图片
                    InsertBitmap(bitmap, 480, path);
                }
            }
            // 拍照
            else if (requestCode == 2) {
                try {
                    if (photoUri != null){
                        // 这个方法是根据Uri获取Bitmap图片的静态方法
                        bitmap = MediaStore.Images.Media.getBitmap(cr, photoUri);
                        // 这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片
                    }else {
                        //得到返回数据
                        extras = data.getExtras();
                        //获取图片
                        bitmap = extras.getParcelable("data");
                    }
                    // 将拍的照片存入指定的文件夹下
                    // 获得系统当前时间，并以该时间作为文件名
                    SimpleDateFormat formatter = new SimpleDateFormat(
                            "yyyyMMddHHmmss");
                    // 获取当前时间
                    Date curDate = new Date(System.currentTimeMillis());
                    // 当前时间保存成String类型
                    String str = formatter.format(curDate);
                    //用于记录图片路径
                    String paintPath = "";
                    //图片路径
                    str += "paint.png";
                    //新建文件夹
                    File dir = new File("/sdcard/notes/");
                    //新建文件
                    File file = new File("/sdcard/notes/", str);
                    if (!dir.exists()) {// 判断文件夹创建是否成功
                        dir.mkdir();// 创建文件夹
                    } else {
                        if (file.exists()) {// 判断文件是否创建
                            file.delete();// 删除文件
                        }
                    }
                    //新建文件流
                    FileOutputStream fos = new FileOutputStream(file);
                    // 将 bitmap 压缩成其他格式的图片数据
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();//结束流传输
                    fos.close();//关闭流
                    //图片路径
                    String path = "/sdcard/notes/" + str;
                    //插入图片
                    InsertBitmap(bitmap, 480, path);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // 返回的是录音文件
            else if (requestCode == 4) {
                //创建接收器
                extras = data.getExtras();
                //接收返回的信息
                String path = extras.getString("audio");
                //转换图片成bitmap形式
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.record_icon);
                // 插入录音图标
                InsertBitmap(bitmap, 300, path);
            }
        }
    }

    // 设置按钮监听器
    class ClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_back:
                    // 当前Activity结束，则返回上一个Activity
                    AddActivity.this.finish();
                    break;
                // 将记事添加到数据库中
                case R.id.bt_save:
                    // 取得EditText中的内容
                    context = et_Notes.getText().toString();
                    title = et_Title.getText().toString();
//                    if (context.isEmpty()) {
//                        Toast.makeText(AddActivity.this, "记事为空!", Toast.LENGTH_LONG)
//                                .show();
//                    }else
                    if(title.isEmpty()){
                        Toast.makeText(AddActivity.this, "标题为空!", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        // 取得当前时间
                        SimpleDateFormat formatter = new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm");
                        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
                        time = formatter.format(curDate);
                        Log.d("ddd",time);
                        // 打开数据库
//                        dop.create_db();
                        // 判断是更新还是新增记事
                        if (editModel.equals("newAdd")) {
                            // 将记事插入到数据库中
                            diary = new Diary(title,context,new String[]{},time,dataType,dataTime,lockType,lock);
                            diary.save();
                            Log.d("ddd",diary.getTime());
//                            dop.insert_db(title, context, time, datatype, datatime,
//                                    locktype, lock);
                        }
                        // 如果是编辑则更新记事即可
                        else if (editModel.equals("update")) {
                            diary.setContext(context);
                            diary.setTime(time);
                            diary.setTitle(title);
                            diary.setDataTime(dataTime);
                            diary.setLock(lock);
                            if(lockType)
                                diary.setLockType(lockType);
                            else
                                diary.setToDefault("lockType");
                            if(dataType)
                                diary.setDataType(dataType);
                            else
                                diary.setToDefault("dataType");
                            diary.setImages(new String[]{});
                            diary.update(diaryId);
//                            dop.update_db(title, context, time, datatype, datatime,
//                                    locktype, lock, item_Id);
                        }
//                        dop.close_db();
                        // 结束当前activity
                        AddActivity.this.finish();
                    }
                    break;
            }
        }
    }


    // 将图片等比例缩放到合适的大小并添加在EditText中
    void InsertBitmap(Bitmap bitmap, int S, String imgPath) {
        bitmap = resize(bitmap, S);
        // 添加边框效果
        // bitmap = getBitmapHuaSeBianKuang(bitmap);
        // bitmap = addBigFrame(bitmap,R.drawable.line_age);
        //设置图片 SPAN_MARK_MARK：像文本标记长度为0的跨度与SPAN_MARK_MARK型：他们仍然在原来的偏移该偏移处插入文本时
        final ImageSpan imageSpan = new ImageSpan(this, bitmap);
        SpannableString spannableString = new SpannableString(imgPath);
        spannableString.setSpan(imageSpan, 0, spannableString.length(), SpannableString.SPAN_MARK_MARK);
        // 光标移到下一行
//        et_Notes.append("\n");
        Editable editable = et_Notes.getEditableText();
        int selectionIndex = et_Notes.getSelectionStart();
          spannableString.getSpans(0, spannableString.length(), ImageSpan.class);
        // 将图片添加进EditText中
        editable.insert(selectionIndex, spannableString);
        // 添加图片后自动空出两行
        et_Notes.append("\n");
        // 用List记录该录音的位置及所在路径，用于单击事件
        Map<String, String> map = new HashMap<String, String>();
        map.put("location", selectionIndex + "-"
                + (selectionIndex + spannableString.length()));
        map.put("path", imgPath);
        imgList.add(map);
    }

    // 等比例缩放图片
    private Bitmap resize(Bitmap bitmap, int S) {
        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();
        double partion = imgWidth * 1.0 / imgHeight;
        double sqrtLength = Math.sqrt(partion * partion + 1);
        // 新的缩略图大小
        double newImgW = S * (partion / sqrtLength);
        double newImgH = S * (1 / sqrtLength);
        float scaleW = (float) (newImgW / imgWidth);
        float scaleH = (float) (newImgH / imgHeight);
        Matrix mx = new Matrix();
        // 对原图片进行缩放
        mx.postScale(scaleW, scaleH);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx,
                true);
        return bitmap;
    }

    // 给图片加边框，并返回边框后的图片
    public Bitmap getBitmapHuaSeBianKuang(Bitmap bitmap) {
        float frameSize = 0.2f;
        Matrix matrix = new Matrix();

        // 用来做底图
        Bitmap bitmapbg = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        // 设置底图为画布
        Canvas canvas = new Canvas(bitmapbg);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));

        float scale_x = (bitmap.getWidth() - 2 * frameSize - 2) * 1f
                / (bitmap.getWidth());
        float scale_y = (bitmap.getHeight() - 2 * frameSize - 2) * 1f
                / (bitmap.getHeight());
        matrix.reset();
        matrix.postScale(scale_x, scale_y);
        // 对相片大小处理(减去边框的大小)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL);

        // 绘制底图边框
        canvas.drawRect(
                new Rect(0, 0, bitmapbg.getWidth(), bitmapbg.getHeight()),
                paint);

        // 绘制灰色边框
        paint.setColor(Color.GRAY);
        canvas.drawRect(
                new Rect((int) (frameSize), (int) (frameSize), bitmapbg
                        .getWidth() - (int) (frameSize), bitmapbg.getHeight()
                        - (int) (frameSize)), paint);

        canvas.drawBitmap(bitmap, frameSize + 1, frameSize + 1, paint);
        return bitmapbg;
    }

    //为EditText设置监听器
    class TextClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Spanned s = et_Notes.getText();
            ImageSpan[] imageSpans;
            imageSpans = s.getSpans(0, s.length(), ImageSpan.class);
            int selectionStart = et_Notes.getSelectionStart();
            for (ImageSpan span : imageSpans) {
                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                // 找到图片
                if (selectionStart >= start && selectionStart < end) {
                    // 查找当前单击的图片是哪一个图片
                    String path = null;
                    for (int i = 0; i < imgList.size(); i++) {
                        Map map = imgList.get(i);
                        // 找到了
                        if (map.get("location").equals(start + "-" + end)) {
                            path = imgList.get(i).get("path");
                            Log.d("ddd","3---"+path);
                            break;
                        }
                    }
                    // 接着判断当前图片是否是录音，如果为录音，则跳转到试听录音的Activity，如果不是，则跳转到查看图片的界面
                    // 录音，则跳转到试听录音的Activity
                    if (path.substring(path.length() - 3, path.length())
                            .equals("amr")) {
                        Intent intent = new Intent(AddActivity.this,
                                ShowRecord.class);
                        intent.putExtra("audioPath", path);
                        startActivity(intent);
                    }
                    // 图片，则跳转到查看图片的界面
                    else {
                        /**
                         * 调用系统图库查看图片
                         * 有两种方法，查看图片，第一种就是直接调用系统的图库查看图片，第二种是自定义Activity
                         * 其原因是Android 7.0 做了一些系统权限更改，为了提高私有文件的安全性，面向 Android 7.0 或更高版本的应用私有目录被限制访问，此设置可防止私有文件的元数据泄漏，如它们的大小或存在性。
                         */
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        File file = new File(path);
                        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), "com.example.fileprovider", file);//file即为所要共享的文件的file
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//授予临时权限别忘了
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(photoURI, "image/*");
                        startActivity(intent);
                        Log.d("ddd","4---"+path);
                    }
                } else
                    // 如果单击的是空白出或文字，则获得焦点，即打开软键盘
                    imm.showSoftInput(et_Notes, 0);
            }
        }
    }

    // 提醒设置
    private void setReminder() {
        DateTimePickerDialog d;
        if (!dataType) {//判断是否设置过事件０没有设置过提醒时间
            d = new DateTimePickerDialog(this,System.currentTimeMillis());//设置自定义时间弹出显示系统时间
        } else {
            d = new DateTimePickerDialog(this,getDayTime(dataTime));//设置自定义时间弹出显示设置过的时间
        }
        d.setOnDateTimeSetListener(new DateTimePickerDialog.OnDateTimeSetListener() {
            public void OnDateTimeSet(AlertDialog dialog, long date) {
                // 取得当前时间
                SimpleDateFormat formatter = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm");//设计格式
                dataTime = formatter.format(date);//以自己设置的时间格式显示时间 date为当前选择的时间
                dataType = true;
                data_rl.setVisibility(View.VISIBLE);
                data_tv.setText("提醒时间：" + dataTime);
            }
        });
        d.show();
    }

    public static long getDayTime(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date dt2 = null;
        try {
            dt2 = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt2.getTime();
    }

    // 取消闹钟
    public void onDataCancel(View v) {
        data_rl.setVisibility(View.GONE);
        dataType = false;// 判断是否开启记录开启了提醒功能
        dataTime = "0";
    }

    // 修改闹钟提醒时间
    public void onDataChange(View v) {
        setReminder();
    }

    // 添加日记锁 取消日记锁
    public void onLOCK(View v) {
        //判断是否设置了密码
        if (!lockType) { //无锁
            inputLockDialog();//弹出设置密码弹窗
        } else {
            inputUnlockDialog();//弹出取消密码弹窗
        }
    }

    //设置密码弹窗
    private void inputLockDialog() {
        final EditText inputServer = new EditText(this);//创建EditText输入框
        inputServer.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);//设置输入框类型
        inputServer.setFocusable(true);//获取焦点
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//创建弹出框
        builder.setTitle("设置密码").setView(inputServer)
                .setNegativeButton("取消", null);//在弹窗上设置标题添加输入框
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {//设置确认按钮
                String inputName = inputServer.getText().toString();
                if ("".equals(inputName)) {//判断输入框内容是否为空
                    Toast.makeText(AddActivity.this, "密码不能为空 请重新输入！",
                            Toast.LENGTH_LONG).show();
                } else {//输入框内容不为空　
                    lock = inputName;//密码
                    lockType = true;//添加了密码锁
                    ib_lk.setBackgroundResource(R.drawable.locky);//设置添加锁图案
                    Toast.makeText(AddActivity.this, "密码设置成功！",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.show();//弹出设置密码弹窗
    }

    //取消密码
    private void inputUnlockDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//创建弹出框
        builder.setTitle("是否取消密码")
                .setNegativeButton("取消", null);//在弹窗上设置标题设置取消按钮
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {//设置确认按钮
                lockType = false;//设置没有设置密码
                lock = "0";//设置密码
                ib_lk.setBackgroundResource(R.drawable.un_locky);
                Toast.makeText(AddActivity.this, "密码已取消",
                        Toast.LENGTH_LONG).show();
            }
        });
        builder.show();//弹出取消密码弹窗
    }

    // 分享功能
    public void onFX(View v) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        FXName = format.format(date);
        sc_lv = (ScrollView)findViewById(R.id.scroll);
        Bitmap c = getBitmapByView(sc_lv);//获取到图片
//        if(c==null)
//            Log.d("ddd","null");
        try {
            saveMyBitmap(FXName, c);//保存图片
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String imagePath = Environment.getExternalStorageDirectory()
                + File.separator + FXName+".jpg";//图片路径

        File file = new File(imagePath);
        Uri imageUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.fileprovider", file);//file即为所要共享的文件的file
        Log.d("ddd", "uri:" + imageUri);//打印图片路径
        Intent shareIntent = new Intent();//创建意图
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//授予临时权限别忘了
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//默认跳转类型
        shareIntent.setAction(Intent.ACTION_SEND);//过滤条件允许分享的
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);//分享图片
        shareIntent.setType("image/*");//设置类型
        startActivity(Intent.createChooser(shareIntent, "分享到："));
    }

    //保存图片
    public void saveMyBitmap(String bitName, Bitmap mBitmap) throws IOException {
        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator + FXName+".jpg");//初始化文件
        f.createNewFile();//创建图片文件
        FileOutputStream fOut = null;//创建文件流
        try {
            fOut = new FileOutputStream(f);//实例化文件流
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);//图片保存到文件中
        try {
            fOut.flush();//文件写写入操作结束
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();//关闭文件流
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //截取scrollview的屏幕
    public static Bitmap getBitmapByView(ScrollView scrollView) {
        int h = 0;//设置高度0
        Bitmap bitmap = null;//设置空的图片
        // 获取scrollview实际高度
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();//计算scrollView实际高度
            scrollView.getChildAt(i).setBackgroundColor(
                    Color.parseColor("#FFFFFF"));//设置scrollView背景颜色
        }
        h+=300;
        // 创建scrollView大小的bitmap
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h,
                Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);//绘制图片
        scrollView.draw(canvas);//绘制scrollView
        return bitmap;//返回图片
    }

}
