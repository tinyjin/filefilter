package com.koi.filefilter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class setting extends AppCompatActivity {
    SharedPreferences sp;
    SharedPreferences.Editor spedit;

    LinearLayout ll;
    LayoutInflater li;

    AlertDialog ab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0054FF")));

        li = (LayoutInflater)getSystemService(setting.LAYOUT_INFLATER_SERVICE);

        sp = getSharedPreferences("sp",MODE_PRIVATE);
        spedit = sp.edit();



        ListView setList = (ListView)findViewById(R.id.setList);
        ListView moreSetList = (ListView)findViewById(R.id.moreSetList);

        ArrayList<String> mList = new ArrayList<String>();
        ArrayList<String> mList2 = new ArrayList<String>();

        mList.add("확장자 목록");
        mList.add("종류 목록");
        mList.add("확장자 추가");
        mList.add("종류 추가");

        mList2.add("앱버전: 1.0 beta");
        mList2.add("확장자필터링 튜토리얼");
        mList2.add("종류필터링 튜토리얼");

        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this,R.layout.list_layout,mList);
        ArrayAdapter<String> mAdapter2 = new ArrayAdapter<String>(this,R.layout.list_layout,mList2);

        setList.setAdapter(mAdapter);
        moreSetList.setAdapter(mAdapter2);

        moreSetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    new AlertDialog.Builder(setting.this).setView(li.inflate(R.layout.tutorial,null)).show();
                }
                else if(position == 2){
                    new AlertDialog.Builder(setting.this).setView(li.inflate(R.layout.tutorial2,null)).show();
                }
            }
        });

        setList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    final int len = sp.getInt("len", 0);
                    String[] strarr = new String[len];
                    for (int i = 0; i < len; i++) {
                        strarr[i] = sp.getString("extension" + i, null);
                    }
                    new AlertDialog.Builder(setting.this).setTitle("확장자 목록").setItems(strarr, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final int which2 = which;

                            new AlertDialog.Builder(setting.this).setTitle("삭제 확인").setMessage(sp.getString("extension" + which, null)
                                    + "확장자를 삭제하시겠습니까?").setNegativeButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(setting.this, sp.getString("extension" + which2, null) + "확장자가 삭제되었습니다.", Toast.LENGTH_LONG).show();
                                    for (int i = which2 + 1; i < len; i++) {
                                        String tmp = sp.getString("extension" + i, null);
                                        spedit.putString("extension" + (i - 1), tmp);
                                        spedit.commit();
                                    }
                                    spedit.putString("extension" + (len - 1), null);
                                    spedit.putInt("len", len - 1);
                                    spedit.commit();
                                }
                            }).setPositiveButton("취소", null).show();
                        }
                    }).show();
                }
                else if(position == 1){
                    ll = (LinearLayout)li.inflate(R.layout.kind_layout,null);
                    final ExpandableListView kind_List = (ExpandableListView)ll.findViewById(R.id.kind_List);
                    final int len2 = sp.getInt("len2", 0);
                    final String[] kind_parent = new String[len2];
                    String[][] kind_child = new String[len2][9999];
                    for (int i = 0; i < len2; i++) {
                        kind_parent[i] = sp.getString("kind" + i, null);
                    }
                    for(int i = 0; i < len2; i++){
                        int len3 = sp.getInt(kind_parent[i]+"len",0);
                        for(int j = 0; j < len3; j++){
                            kind_child[i][j] = sp.getString(kind_parent[i]+j,null);
                        }
                    }

                    List<Map<String,String>> parent_data = new ArrayList<Map<String,String>>();
                    List<List<Map<String,String>>> child_data = new ArrayList<List<Map<String,String>>>();

                    for(int i = 0; i < kind_parent.length; i ++){
                        Map<String,String> parents = new HashMap<String, String>();
                        parents.put("parents",kind_parent[i]);
                        parent_data.add(parents);

                        List<Map<String,String>> children = new ArrayList<Map<String,String>>();

                        for(int j = 0; j < sp.getInt(kind_parent[i]+"len",0); j ++){
                            Map<String,String> childs = new HashMap<String, String>();
                            childs.put("childs",kind_child[i][j]);
                            children.add(childs);
                        }
                        child_data.add(children);
                    }

                    ExpandableListAdapter exadapter = new SimpleExpandableListAdapter(
                            setting.this,parent_data,
                            android.R.layout.simple_expandable_list_item_1,
                            new String[] {"parents"},
                            new int[] {android.R.id.text1},
                            child_data,
                            android.R.layout.simple_expandable_list_item_1,
                            new String[] {"childs"},
                            new int[] {android.R.id.text1}
                    );

                    kind_List.setAdapter(exadapter);

                    kind_List.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent,final View view, final int position,final long id) {
                            int itemType = ExpandableListView.getPackedPositionType(id);
                            if(itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP){

                                new AlertDialog.Builder(setting.this).setTitle("옵션").setItems(new String[]{"서브확장자 추가", "종류 삭제"}, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final int Groupnum = kind_List.getPackedPositionGroup(id);
                                        final String Groupstr = sp.getString("kind" + Groupnum, null);
                                        if (which == 0) {
                                            ab.dismiss();
                                            kind_extension_add_method(Groupstr, false);
                                        } else {
                                            new AlertDialog.Builder(setting.this).setTitle("삭제 확인").setMessage("'" + Groupstr + "'" +
                                                    "종류를 삭제하시겠습니까?").setNegativeButton("확인", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ab.dismiss();
                                                    int len2 = sp.getInt("len2", 0);
                                                    for (int i = 0; i < sp.getInt(Groupstr + "len", 0); i++) {
                                                        spedit.putString(Groupstr + i, null);
                                                    }
                                                    for (int i = Groupnum + 1; i < len2; i++) {
                                                        spedit.putString("kind" + (i - 1), sp.getString("kind" + i, null));
                                                    }
                                                    spedit.putString("kind" + (len2 - 1), null);
                                                    spedit.putInt("len2", len2 - 1);
                                                    spedit.putInt(Groupstr + "len", 0);
                                                    spedit.commit();
                                                    Toast.makeText(setting.this, "'" + Groupstr + "'" + "종류가 삭제되었습니다.", Toast.LENGTH_LONG).show();
                                                }
                                            }).setPositiveButton("취소", null).show();
                                        }
                                    }
                                }).show();
                            }
                            return false;
                        }
                    });

                    ab =  new AlertDialog.Builder(setting.this).setTitle("종류 목록").setView(ll).show();

                }
                else if(position == 2){
                    extension_add();
                }
                else if(position == 3){
                    kind_add();
                }
            }
            });

    }
    public void extension_add(){

        ll = (LinearLayout)li.inflate(R.layout.add_layout,null);

        new AlertDialog.Builder(setting.this).setTitle("확장자 추가").setIcon(R.mipmap.ic_add).setView(ll)
                .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        boolean flag = false;
                        EditText ed = (EditText) ll.findViewById(R.id.ed);
                        int len = sp.getInt("len", 0);
                        for (int i = 0; i < len; i++) {
                            if (ed.getText().toString().equals(sp.getString("extension" + i, null))) {
                                flag = true;
                                break;
                            }
                        }
                        if(ed.getText().toString().replaceAll(" ", "").equals(""))
                            Toast.makeText(setting.this, ed.getText().toString() + "확장자를 입력하세요.", Toast.LENGTH_LONG).show();
                         else if (!flag) {
                             if (!ed.getText().toString().startsWith(".")) {
                                 ed.setText("."+ed.getText().toString());
                             }
                            spedit.putString("extension" + len, ed.getText().toString());
                            spedit.putInt("len", len + 1);
                            spedit.commit();
                            Toast.makeText(setting.this, ed.getText().toString() + "확장자가 추가되었습니다.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(setting.this, ed.getText().toString() + "확장자가 이미 추가되어 있습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                }).setPositiveButton("취소", null).show();
    }

    public void kind_add(){

        ll = (LinearLayout)li.inflate(R.layout.add_layout2,null);

        new AlertDialog.Builder(setting.this).setTitle("종류 추가").setIcon(R.mipmap.ic_add).setView(ll)
                .setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        EditText ed = (EditText) ll.findViewById(R.id.ed);
                        final String str = ed.getText().toString();
                        boolean flag = false;
                        int len2 = sp.getInt("len2", 0);

                        for (int i = 0; i < len2; i++) {
                            if (str.equals(sp.getString("kind" + i, null))) {
                                flag = true;
                            }
                        }
                        if (str.replaceAll(" ", "").equals("")) {
                            Toast.makeText(setting.this, "종류명을 입력해주세요.", Toast.LENGTH_LONG).show();
                        } else if (flag) {
                            Toast.makeText(setting.this, "'" + str + "'" + "종류가 이미 추가되어있습니다.", Toast.LENGTH_LONG).show();
                        } else {
                            spedit.putString("kind" + len2, str);
                            spedit.putInt("len2", len2 + 1);
                            spedit.commit();
                            kind_extension_add_method(str, true);
                        }
                    }
                }).setPositiveButton("취소", null).show();
    }

    public void kind_extension_add_method(final String str2, final boolean where){
        ll = (LinearLayout)li.inflate(R.layout.add_layout,null);

        new AlertDialog.Builder(setting.this).setTitle("'"+str2+"'"+"서브 확장자 추가").setIcon(R.mipmap.ic_add).
                setView(ll).setNegativeButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                EditText ed2 = (EditText) ll.findViewById(R.id.ed);
                int kindlen = sp.getInt(str2 + "len", 0);
                String edstr = ed2.getText().toString();
                boolean flag = false;
                for (int i = 0; i < kindlen; i++) {
                    if (edstr.equals(sp.getString(str2 + i, null))) {
                        flag = true;
                        break;
                    }
                }
                if(edstr.replaceAll(" ", "").equals("")){
                    Toast.makeText(setting.this, "확장자를 입력하세요.", Toast.LENGTH_LONG).show();
                }
                else if (flag) {
                    Toast.makeText(setting.this, "'" + str2 + "'" + "종류에 " + edstr + "확장자가 이미 추가되어 있습니다.", Toast.LENGTH_LONG).show();
                } else {
                    if (!edstr.startsWith(".")) {
                        edstr="."+edstr;
                    }
                    spedit.putString(str2 + kindlen, edstr);
                    spedit.putInt(str2 + "len", kindlen + 1);
                    spedit.commit();
                }
                kind_extension_add_method(str2, where);
            }
        }).setPositiveButton("끝내기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (where)
                    Toast.makeText(setting.this, "'" + str2 + "'" + "종류가 추가되었습니다.", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(setting.this, "'" + str2 + "'" + "의 서브확장자가 추가되었습니다.", Toast.LENGTH_LONG).show();
            }
        }).show();

    }

    @Override
    public void onBackPressed(){
        finish();
        overridePendingTransition(R.anim.abc_slide_in_bottom,0);
    }
}
