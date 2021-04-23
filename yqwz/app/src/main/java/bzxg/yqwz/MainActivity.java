package bzxg.yqwz;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import bzxg.yqwz.Util.LogHelper;
import bzxg.yqwz.Util.PermissionsListener;
import bzxg.yqwz.Util.PermissionsUtil;
import bzxg.yqwz.model.Api;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    PermissionsUtil mPermissionsUtil = new PermissionsUtil();
    private static String[] permissionsREAD = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};
    private TextView flagText;
    private Button getBtn;
    private Button copyBtn;
    private Button resetBtn;
    private String result;
    private TextView text;
    private Spinner spinner;
    private List<String> names = new ArrayList<>();
    private List<Api> apiList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //实例化控件
        initView();
        //配置监听器
        initListener();
        //为textView添加滚动条
        text.setMovementMethod(ScrollingMovementMethod.getInstance());
        //检查权限
        checkPermissin();
        //欢迎
        Toast.makeText(MainActivity.this, "欢迎使用（ '▿ ' ）", Toast.LENGTH_SHORT).show();
        try {
            initSpinnerData(spinner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //检查权限
    private void checkPermissin() {
        mPermissionsUtil
                // 设置回调
                .setPermissionsListener(new PermissionsListener() {
                    // 申请的权限中有被拒绝的回调该方法，可以在此进行你的逻辑
                    // 由于某些rom对权限进行了处理，第一次选择了拒绝，则不会出现第二次询问（或者没有不再询问勾选），故拒绝就回调onDenied
                    @Override
                    public void onDenied(String[] deniedPermissions) {
                        // 在deniedPermissions里面包含了所有被拒绝的权限名字
                        for (int i = 0; i < deniedPermissions.length; i++) {
                            System.out.println(deniedPermissions[i] + " 权限被拒绝");
                        }
                    }

                    // 所有申请的权限同意了回调该方法，可以在此进行你的逻辑
                    @Override
                    public void onGranted() {
                        Toast.makeText(MainActivity.this, "所有权限都被同意", Toast.LENGTH_SHORT).show();
                    }
                })
                // 设置Activity，因为检查权限必须要有activity对象
                .withActivity(this)
                // 最后调用申请权限的方法
                // 三个参数分别是Object，int，String[]
                // 第一个参数对应的是activity或者fragment，如果是在activity中申请就传入activity对象，在fragment申请就传入该fragment对象，这是用于对返回事件的分发进行处理。
                // 第二个对应的是该次申请权限的requestCode
                // 第三个就是权限列表，不定长度
                .getPermissions(MainActivity.this, 100, permissionsREAD);
    }

    // 在activity或者fragment中
    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 在onRequestPermissionsResult这个方法调用dealResult就可以了
        mPermissionsUtil.dealResult(requestCode, permissions, grantResults);
    }

    //添加创建选项菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO: Implement this method
        MenuInflater inflater = getMenuInflater();
        //通过res的资源文件来访问选项菜单
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    //创建选项菜单单击时的方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: Implement this method
        switch (item.getItemId()) {
            case R.id.item1:
                show_dialog();
                break;
            case R.id.item2:
                finish();
                break;
        }
        return true;
    }

    /*点击两次退出软件*/
    //定义一个退出的布尔值
    boolean isExit;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断返回键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public void exit() {
        //第一次按下返回键
        if (!isExit) {
            isExit = true;
            //弹出提示
            Toast toast = Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT);
            toast.show();
            //开启线程，设置时间间隔，2000毫秒为2秒
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            //第二次点击时的退出事件
            finish();
        }
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO: Implement this method
            super.handleMessage(msg);
            isExit = false;
        }

    };

    //关于我们弹窗
    public void show_dialog() {
        //创建弹窗对象
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        //点击界面其他地方弹窗不会消失
        dialog.setCancelable(false);
        //为弹窗设置标题
        dialog.setTitle("关于我们:");
        //设置图标
        dialog.setIcon(R.drawable.author);
        //设置内容
        dialog.setMessage("介绍:\n   1.名称:有趣文字" +
                "\n   2.版本:1.1\n   3.作者:搬砖小哥\n   4.开发工具:Android Studio\n   5.功能:调用有趣文字接口，获取文字\n   6.完成时间:21/3/17\n   7.技术:OkHttp3等\n   8.备注：可自定义文字接口（yqwz/data.json）,地址为直接响应文字的链接;部分代码|接口来源于网络|软件，仅供学习交流，方便日后查看( ˙-˙ )");
        //设置按钮
        //右边按钮
        dialog.setPositiveButton("空间", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface p1, int p2) {
                // TODO: Implement this method
                String url = "https://github.com/bzxg-space";
                //创建对象
                Uri uri = Uri.parse(url);
                Intent intent2 = new Intent(Intent.ACTION_VIEW, uri);
                //启动
                startActivity(intent2);
            }
        });
        //中间按钮
        dialog.setNegativeButton("关闭", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface p1, int p2) {
                // TODO: Implement this method
            }
        });
        //左边按钮
        dialog.setNeutralButton("源码|学习工具", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface p1, int p2) {
                String url = "https://mubu.com/doc/5K7poq_G3g";
                //创建对象
                Uri uri = Uri.parse(url);
                Intent intent2 = new Intent(Intent.ACTION_VIEW, uri);
                //启动
                startActivity(intent2);
            }
        });
        //显示弹窗
        dialog.show();
    }

    //初始化视图
    private void initView() {
        resetBtn = (Button) findViewById(R.id.resetBtn);
        getBtn = (Button) findViewById(R.id.getBtn);
        copyBtn = (Button) findViewById(R.id.copyBtn);
        flagText = (TextView) findViewById(R.id.flagText);
        text = (TextView) findViewById(R.id.text);
        spinner = (Spinner) findViewById(R.id.spinner);
    }

    //事件绑定
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.copyBtn:
                copy(v);
                break;
            case R.id.getBtn:
                flagText.setText("..");
                getRequest(v);
                break;
            case R.id.resetBtn:
                clearView();
                break;

        }
    }

    //重置
    private void clearView() {
        text.setText("文本结果显示区");
        spinner.setSelection(0);
        flagText.setText("..");
        Toast.makeText(MainActivity.this, "重置成功^_^", Toast.LENGTH_SHORT).show();
    }

    //初始化监听器
    private void initListener() {
        getBtn.setOnClickListener(this);
        copyBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);
    }

    //初始化Spinner下拉框数据
    private void initSpinnerData(Spinner spinner) throws IOException {
        String data = getDataString();
        try {
            //判断本地有没有data.json,如没有就生成一份
            String filePath = Environment.getExternalStorageDirectory().getPath();//获取存储卡的目录
            File file = new File(filePath + File.separator + "yqwz");
            if (!file.exists()) {
                file.mkdir();
            }
            File dataFile = new File(file.getAbsolutePath() + File.separator + "data.json");
            if (!dataFile.exists()) {
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(dataFile), "utf-8");
                osw.write(data);
                osw.close();
            } else {
                data = readDataStringByFis(new FileInputStream(dataFile));
            }
        } catch (Exception e) {
            LogHelper.ShowLog("没有允许读写权限");
        } finally {
            Log.d("【】DATA", data);
            apiList = JSON.parseArray(data, Api.class);
            if (apiList == null || apiList.size() == 0) {
                //创建弹窗对象
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                //点击界面其他地方弹窗不会消失
                dialog.setCancelable(false);
                //为弹窗设置标题
                dialog.setTitle("温馨提示");
                //设置内容
                dialog.setMessage("没有读取到接口数据，请重新安装或配置 yqwz/data.json 文件");
                //设置按钮
                //右边按钮
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                //显示弹窗
                dialog.show();
            } else {
                for (Api api : apiList) {
                    names.add(api.getName());
                }
                //为spinner设置数据
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, android.R.id.text1, names);
                spinner.setAdapter(adapter);
                //为适配器添加样式
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //通知spinner刷新数据
                adapter.notifyDataSetChanged();
                //默认选择第一项
                spinner.setSelection(0);
            }
        }
    }

    @NonNull
    private String getDataString() {
        String data = new String("");
        //String dataPath = "file:///android_asset/data.json";
        try {
            AssetManager am = MainActivity.this.getAssets();
            InputStream fis = am.open("data.json");
            //InputStreamReader reader=new InputStreamReader(new FileInputStream(new File(dataPath)),"utf-8");
            data = readDataStringByFis(fis);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    @NonNull
    private String readDataStringByFis(InputStream fis) throws IOException {
        String data = new String("");
        InputStreamReader reader = new InputStreamReader(fis, "utf-8");
        StringBuffer sb = new StringBuffer();
        Log.d("【】", (reader.ready() == true) ? "1" : "0");
        while (reader.ready()) {
            sb.append((char) reader.read());
            // 转成char加到StringBuffer对象中
        }
        reader.close();
        fis.close();
        data = sb.toString();
        return data;
    }

    //获取
    public void getRequest(View view) {
        Toast.makeText(MainActivity.this, "获取中^_^", Toast.LENGTH_SHORT).show();
        String value = spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString();
        String url = null;
        for (int i = 0; i < names.size(); i++) {
            if (value.equals(names.get(i))) {
                url = apiList.get(i).getUrl();
            }
        }
        //1.创建OkHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.创建Request对象，设置一个url地址,设置请求方式。
        final Request request = new Request.Builder().url(url).get().build();
        //3.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //4.请求加入调度，重写回调方法
        call.enqueue(new Callback() {
            //请求失败执行的方法
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        flagText.setText("失败×");
                        flagText.setTextColor(Color.parseColor("#a94442"));
                    }
                });
            }

            //请求成功执行的方法
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                Log.d("data", data);
                result = data;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //更新UI
                        text.setText(result);
                        flagText.setText("成功√");
                        flagText.setTextColor(Color.parseColor("#3c763d"));
                    }
                });
            }
        });


    }

    //复制
    public void copy(View v) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        //cm.setText(te.getText());
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", text.getText());
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
        Toast.makeText(MainActivity.this, "复制成功^_^", Toast.LENGTH_LONG).show();
    }

}
