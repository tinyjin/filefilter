package com.koi.filefilter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.appcompat.*;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    custom_list cl;
    custom_adap mAdapter;
    ArrayList<custom_list> mList;

    String root = Environment.getExternalStorageDirectory().getAbsolutePath();

    final String froot = root;
    ListView FileList;
    TextView roottext;

    String[] arr = new String[] {"확장자 필터링","종류 필터링", "오토 필터링"};

    SharedPreferences sp;
    SharedPreferences.Editor spedit;

    LayoutInflater li;

    boolean clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0054FF")));
        li = (LayoutInflater)getSystemService(MainActivity.LAYOUT_INFLATER_SERVICE);

        sp = getSharedPreferences("sp", MODE_PRIVATE);
        spedit = sp.edit();

        FileList = (ListView)findViewById(R.id.FileList);
        roottext = (TextView)findViewById(R.id.roottext);
        mList = new ArrayList<>();

        mAdapter = new custom_adap(this,R.layout.list_item, mList);
        FileList.setAdapter(mAdapter);

        update(root);

        View.OnTouchListener ot = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == event.ACTION_DOWN){
                    ((ImageButton)v).setColorFilter(0xaa111111, PorterDuff.Mode.SRC_OVER);
                } else if(event.getAction() == event.ACTION_UP){
                    ((ImageButton)v).setColorFilter(0x00000000, PorterDuff.Mode.SRC_OVER);
                }
                return false;
            }
        };

        ImageButton search, refresh, add_f, filtering, delete_f, sort;

        search = (ImageButton)findViewById(R.id.search);
        refresh = (ImageButton)findViewById(R.id.refresh);
        add_f = (ImageButton)findViewById(R.id.add);
        delete_f = (ImageButton)findViewById(R.id.trash);
        filtering = (ImageButton)findViewById(R.id.filter);
        sort = (ImageButton)findViewById(R.id.sort);

        search.setOnTouchListener(ot);
        refresh.setOnTouchListener(ot);
        add_f.setOnTouchListener(ot);
        delete_f.setOnTouchListener(ot);
        filtering.setOnTouchListener(ot);
        sort.setOnTouchListener(ot);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(root);
            }
        });

        add_f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ViewGroup vg = (ViewGroup)li.inflate(R.layout.add_folder, null);
                new AlertDialog.Builder(MainActivity.this).setTitle("폴더 추가").setIcon(R.mipmap.ico_add).setView(vg).
                        setNegativeButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!((EditText) vg.findViewById(R.id.ed)).getText().toString().equals("")) {
                                    File add_dir = new File(root + "/" + ((EditText) vg.findViewById(R.id.ed)).getText().toString());
                                    add_dir.mkdirs();
                                    update(root);
                                } else {
                                    Toast.makeText(MainActivity.this,"폴더명을 입력하세요.",Toast.LENGTH_LONG).show();
                                }

                            }
                        }).setPositiveButton("취소", null).show();

            }
        });

        filtering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File f = new File(root);
                if(f.isDirectory()){
                    new AlertDialog.Builder(MainActivity.this).setIcon(R.mipmap.ico_filter).setTitle("필터링").
                            setItems(arr, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        ExtensionFiltering(root);
                                    }
                                    else if (which == 1) {
                                        KindFiltering(root);
                                    }
                                    else if (which == 2) {
                                        AutoFiltering(root);
                                        Log.v("MainActivity.java", "clicked");
                                    }

                                }
                            }).show();
                }
            }
        });

        delete_f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });




        FileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                clicked = true;

                return false;
            }

        });

        FileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!clicked) {
                    String root2;
                    root2 = root + "/" + mList.get(position).text.toString();
                    update(root2);

                    if (new File(root2).isFile()) {
                        //파일열기
                        String fs = mList.get(position).text.toString();
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                        intent.addCategory(intent.CATEGORY_DEFAULT);

                        if (fs.toLowerCase().endsWith("mp3")) {
                            intent.setDataAndType(Uri.fromFile(new File(root2)), "audio/*");
                        } else if (fs.toLowerCase().endsWith("mp4")||fs.toLowerCase().endsWith("wav")) {
                            intent.setDataAndType(Uri.fromFile(new File(root2)), "video/*");
                        } else if (fs.toLowerCase().endsWith("jpg") || fs.toLowerCase().endsWith("jpeg") || fs.toLowerCase().endsWith("gif")
                                || fs.toLowerCase().endsWith("png") || fs.toLowerCase().endsWith("bmp")) {
                            intent.setDataAndType(Uri.fromFile(new File(root2)), "image/*");
                        } else if (fs.toLowerCase().endsWith("txt")) {
                            intent.setDataAndType(Uri.fromFile(new File(root2)), "text/*");
                        } else if (fs.toLowerCase().endsWith("doc") || fs.toLowerCase().endsWith("docx")) {
                            intent.setDataAndType(Uri.fromFile(new File(root2)), "application/msword");
                        } else if (fs.toLowerCase().endsWith("xls") || fs.toLowerCase().endsWith("xlsx")) {
                            intent.setDataAndType(Uri.fromFile(new File(root2)), "application/vnd.ms-excel");
                        } else if (fs.toLowerCase().endsWith("ppt") || fs.toLowerCase().endsWith("pptx")) {
                            intent.setDataAndType(Uri.fromFile(new File(root2)), "application/vnd.ms-powerpoint");
                        } else if (fs.toLowerCase().endsWith("pdf")) {
                            intent.setDataAndType(Uri.fromFile(new File(root2)), "application/pdf");
                        } else if (fs.toLowerCase().endsWith("apk")) {
                            intent.setDataAndType(Uri.fromFile(new File(root2)), "apk/*");
                        } else if(fs.toLowerCase().endsWith("hwp")){
                            intent.setDataAndType(Uri.fromFile(new File(root2)), "application/haansofthwp");
                        } else if(fs.toLowerCase().endsWith("zip")){
                            intent.setDataAndType(Uri.fromFile(new File(root2)), "application/zip");
                        }

                            startActivity(intent);
                        }
                    }else {
                    clicked = false;
                }
                }});

    }

    //확장자 필터링
    public void ExtensionFiltering(String Longroot) {

        File f = new File(Longroot);

        int len = sp.getInt("len", 0);
        for (int i = 0; i < len; i++) {
            final String extension = sp.getString("extension" + i, null);

            FilenameFilter Ffilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(extension);
                }
            };

            String[] files = f.list(Ffilter);

            if(files.length !=0){

                File mkf = new File(Longroot+"/"+extension.replace(".","")+" 파일");
                mkf.mkdirs();
                for(int j = 0; j < files.length; j ++){
                    File rnt = new File(Longroot+"/"+files[j]);

                    //폴더면 이동 취소
                    if(rnt.isDirectory()) continue;
                    rnt.renameTo(new File(Longroot+"/"+extension.replace(".","")+" 파일"+"/"+files[j]));
                }
            }
        }
        Toast.makeText(MainActivity.this,"확장자 필터링이 완료되었습니다.",Toast.LENGTH_LONG).show();
            update(Longroot);
    }


    //종류 필터링
    public void KindFiltering(String Longroot){

        File f = new File(Longroot);

        int len2 = sp.getInt("len2",0);
        for(int i =0; i < len2; i++){
            String name = sp.getString("kind"+i,null);
            int namelen = sp.getInt(name+"len",0);
            for(int j = 0; j < namelen; j++){
                final String thisext = sp.getString(name+j,null);
                FilenameFilter Ffilter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.endsWith(thisext);
                    }
                };
                String[] files = f.list(Ffilter);

                if(files.length !=0){
                    File mkf = new File(Longroot+"/"+name);
                    mkf.mkdirs();
                    for(int k = 0; k < files.length; k ++){
                        File rnt = new File(Longroot+"/"+files[k]);

                        //폴더면 이동 취소
                        if(rnt.isDirectory()) continue;
                        rnt.renameTo(new File(Longroot+"/"+name+"/"+files[k]));
                    }
                }
            }
        }
        Toast.makeText(MainActivity.this,"종류 필터링이 완료되었습니다.",Toast.LENGTH_LONG).show();
        update(Longroot);
    }

    //오토 필터링
    public void AutoFiltering(String targetRoot) {
        File targetFile = new File(targetRoot);

        String[] targetList = targetFile.list();
        int len = targetList.length;

        for(int i = 0; i < len; i++){
            //해당 파일명 추출
            String fileName = targetList[i];

            //해당 파일 생성
            File myFile = new File(targetRoot+"/" + fileName);

            //폴더면 돌려보내기
            if(myFile.isDirectory()) continue;

            //해당 파일 확장자 생성
            String[] splited = fileName.split("\\.");
            int len2 = splited.length;
            Log.v("Main",fileName+": "+len2);

            //확장자 없으면 돌려보내기
            if(len2 < 2) continue;

            String myExtension = splited[splited.length-1];


            //오토 확장자 폴더 생성
            String newPath = targetRoot + "/" + myExtension+" 파일";
            File mkf = new File(newPath);
            mkf.mkdirs();


            //파일이동
            myFile.renameTo(new File(newPath+"/"+fileName));



        }

        Toast.makeText(MainActivity.this,"오토 필터링이 완료되었습니다.",Toast.LENGTH_LONG).show();
        update(targetRoot);

    }

    public void update(String u_root){
        File f = new File(u_root);

        if(f.isDirectory()){
            root = u_root;
            mList.clear();
            String[] files_as = f.list();
            String[] files = f.list();
            int fn = 0;
            for(int i = 0; i< files_as.length; i++){
                if(new File(u_root + "/" + files_as[i]).isDirectory())
                files[fn++] = files_as[i];
            }
            for(int i = 0; i< files_as.length; i++){
                if(!new File(u_root + "/" + files_as[i]).isDirectory())
                    files[fn++] = files_as[i];
            }


            if(files.length==0){
                //cl = new custom_list(R.mipmap.ico_no,"폴더가 비어있습니다.");
               // mList.add(cl);
            }

            for(int i = 0; i < files.length; i++){
                File fr = new File(u_root+"/"+files[i]);
                Date d = new Date(fr.lastModified());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault());
                String date_str = dateFormat.format(d);
                if(fr.isDirectory()) {
                    cl = new custom_list(R.mipmap.ico_folder, files[i],fr.list().length+" items" , date_str);
                    mList.add(cl);
                }
                else {
                    float fs = (float)fr.length();
                    String dan[] = {"B","KB","MB","GB","TB"};
                    int dn = 0;
                    String out_t="";
                    while((long)fs>0){
                        if((long)fs/1000 > 0&&dn<4) {fs/=1000; dn++;}
                        else break;
                    }
                    if(fs<100){
                        out_t += String.format("%.2f", fs)+dan[dn];
                    }else out_t += String.format("%.0f", fs)+dan[dn];

                    if(files[i].toLowerCase().endsWith("mp4"))
                        cl = new custom_list(R.mipmap.ico_mp4, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("mp3"))
                        cl = new custom_list(R.mipmap.ico_mp3, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("zip"))
                        cl = new custom_list(R.mipmap.ico_zip, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("png"))
                        cl = new custom_list(R.mipmap.ico_png, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("jpg")||files[i].toLowerCase().endsWith("jpeg"))
                        cl = new custom_list(R.mipmap.ico_jpg, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("gif"))
                        cl = new custom_list(R.mipmap.ico_gif, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("ppt")||files[i].toLowerCase().endsWith("pptx"))
                        cl = new custom_list(R.mipmap.ico_ppt, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("xls"))
                        cl = new custom_list(R.mipmap.ico_xls, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("xlsx"))
                        cl = new custom_list(R.mipmap.ico_xlsx, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("c"))
                        cl = new custom_list(R.mipmap.ico_c, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("java"))
                        cl = new custom_list(R.mipmap.ico_java, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("cpp"))
                        cl = new custom_list(R.mipmap.ico_cpp, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("js"))
                        cl = new custom_list(R.mipmap.ico_js, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("pdf"))
                        cl = new custom_list(R.mipmap.ico_pdf, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("hwp"))
                        cl = new custom_list(R.mipmap.ico_hwp, files[i],out_t, date_str);
                    else if(files[i].toLowerCase().endsWith("apk"))
                        cl = new custom_list(R.mipmap.ico_app, files[i],out_t, date_str);
                    else if (files[i].toLowerCase().endsWith("txt"))
                        cl = new custom_list(R.mipmap.ico_txt, files[i],out_t, date_str);
                    else if (files[i].toLowerCase().endsWith("rar"))
                        cl = new custom_list(R.mipmap.ico_rar, files[i],out_t, date_str);
                    else if (files[i].toLowerCase().endsWith("wav"))
                        cl = new custom_list(R.mipmap.ico_wav, files[i],out_t, date_str);
                    else if (files[i].toLowerCase().endsWith("html"))
                        cl = new custom_list(R.mipmap.ico_html, files[i],out_t, date_str);
                    else if (files[i].toLowerCase().endsWith("php"))
                        cl = new custom_list(R.mipmap.ico_php, files[i],out_t, date_str);
                    else if (files[i].toLowerCase().endsWith("exe"))
                        cl = new custom_list(R.mipmap.ico_exe, files[i],out_t, date_str);
                    else
                        cl = new custom_list(R.mipmap.ico_default, files[i],out_t, date_str);
                    mList.add(cl);
                }
            }

            mAdapter.notifyDataSetChanged();
            roottext.setText(root);
            animationGo();
        }
    }

    //리스트 애미네이션
    public void animationGo(){
        FileList.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this,R.anim.listview_anim_controller));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.set){
            Intent i = new Intent(this,setting.class);
            startActivity(i);
            overridePendingTransition(R.anim.abc_slide_in_top,0);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if (!root.equals(froot)) {
            int num = root.lastIndexOf("/");
            root = root.substring(0, num);
            update(root);
        }
        else {
            new AlertDialog.Builder(MainActivity.this).setTitle("종료").setMessage("어플을 종료하시겠습니까?").
                    setNegativeButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setPositiveButton("취소", null).show();
        }

    }

    public class custom_list{
        int icon;
        String text;
        String item_text;
        String date_text;
        public custom_list(int a_icon, String a_text,String item, String a_date){
            icon = a_icon;
            text = a_text;
            item_text = item;
            date_text = a_date;
        }
    }
    public class custom_adap extends BaseAdapter{
        Context con;
        LayoutInflater infl;
        ArrayList<custom_list> ard;
        int layout;

        public custom_adap(Context c, int a_layout, ArrayList<custom_list> ar){
            con = c;
            layout = a_layout;
            infl = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ard = ar;
        }

        @Override
        public int getCount() {
            return ard.size();
        }

        @Override
        public Object getItem(int position) {
            return ard.get(position).text;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = infl.inflate(layout, parent, false);
            }
            ImageView image = (ImageView)convertView.findViewById(R.id.list_icon);

            image.setImageResource(ard.get(position).icon);

            TextView tv = (TextView)convertView.findViewById(R.id.list_text);
            tv.setText(ard.get(position).text);

            TextView tv2 =(TextView)convertView.findViewById(R.id.item_text);
            tv2.setText(""+ard.get(position).item_text);

            TextView tv3 =(TextView)convertView.findViewById(R.id.date_text);
            tv3.setText(""+ard.get(position).date_text);

            return convertView;
        }
    }
}
