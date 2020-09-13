package com.example.filemanager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityManagerCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.inputmethodservice.AbstractInputMethodService;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Button btnDelete,btnRename,b3,b4,b5;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            }

    private boolean isFileMangerInitialised;

    private boolean[] selection;
    private File[] files;
    private File dir;
    private  List<String> filesList;
    private Button refresh;
    private int filesFoundCount;
    private TextView pathOutput;
    private String currentPath;
    private boolean isLongClick;
    private int selectedItemIndex;
    private LinearLayout bottomBar;
    private String copyPath;


    @Override
    protected void onResume() {
        super.onResume();
        bottomBar=(LinearLayout) findViewById(R.id.bottomBar);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && arePermissionsDenied())
        {
            requestPermissions(PERMISSIONS,REQUEST_PERMISSION);
            return;
        }
        if(!isFileMangerInitialised)
        {
            currentPath=String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
            final String rootPath=currentPath.substring(0,currentPath.lastIndexOf('/'));
            pathOutput=(TextView) findViewById(R.id.pathOutput);
            final TextAdapter textAdapter=new TextAdapter();
            listView=(ListView) findViewById(R.id.listView);
            listView.setAdapter(textAdapter);
            filesList=new ArrayList<>();



            refresh=(Button) findViewById(R.id.refresh);
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dir=new File(currentPath);
                    files=dir.listFiles();
                    filesFoundCount=files.length;
                    pathOutput.setText(currentPath.substring(currentPath.lastIndexOf('/')+1));
                    selection=new boolean[files.length];
                    textAdapter.setSelection(selection);
                    filesList.clear();
                    for(int i=0;i<filesFoundCount;i++)
                    {
                        filesList.add(String.valueOf(files[i].getAbsolutePath()));
                    }
                    textAdapter.setData(filesList);


                }
            });
            refresh.callOnClick();

            final Button btnBack=(Button) findViewById(R.id.backbutton);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(currentPath.equalsIgnoreCase(rootPath))
                    {
                        return;
                    }
                    currentPath=currentPath.substring(0,currentPath.lastIndexOf('/'));
                    dir=new File(currentPath);
                    pathOutput.setText(currentPath.substring(currentPath.lastIndexOf('/')+1));
                    refresh.callOnClick();



                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!isLongClick)
                            {
                                if(files[position].isDirectory())
                                {
                                    currentPath=files[position].getAbsolutePath();
                                     refresh.callOnClick();

                                }
                            }
                        }
                    },50);


                }
            });
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    isLongClick=true;

                    selection[position]=!selection[position];
                    textAdapter.setSelection(selection);
                    int selectionCount=0;
                    for(int i=0;i<selection.length;i++)
                    {
                        if(selection[i])
                        {
                           selectionCount++;
                        }
                    }
                    if(selectionCount>0)
                    {
                        if(selectionCount==1)
                        {
                            selectedItemIndex=position;
                            btnRename.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            btnRename.setVisibility(View.GONE);
                        }
                        btnDelete.setVisibility(View.VISIBLE);
                        bottomBar.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        bottomBar.setVisibility(View.GONE);
                        btnDelete.setVisibility(View.INVISIBLE);
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isLongClick=false;
                        }
                    },1000);
                    return false;
                }
            });
            btnDelete=(Button) findViewById(R.id.delete);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnDelete.setVisibility(View.INVISIBLE);

                    final AlertDialog.Builder deleteDialog= new AlertDialog.Builder(MainActivity.this);
                    deleteDialog.setTitle("Delete");
                    deleteDialog.setMessage("Do you want to delete it?");
                    deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int j) {
                            for(int i=0;i<files.length;i++)
                            {
                                if(selection[i])
                                {
                                    deleteFileOrFolder(files[i]);
                                    selection[i]=false;
                                }
                            }
                            refresh.callOnClick();
                            textAdapter.setSelection(selection);
                            bottomBar.setVisibility(View.INVISIBLE);

                        }
                    });
                    deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int j) {
                            for(int i=0;i<files.length;i++)
                            {
                                if(selection[i])
                                {
                                    selection[i]=false;
                                }
                            }
                            dialogInterface.cancel();
                            textAdapter.setSelection(selection);
                            bottomBar.setVisibility(View.INVISIBLE);


                        }
                    });
                    deleteDialog.show();
                }
            });

            final Button newFolder=(Button) findViewById(R.id.newFolder);
            newFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder newFolderDialog= new AlertDialog.Builder(MainActivity.this);
                    newFolderDialog.setTitle("New Folder");
                    final EditText input=new EditText(MainActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    newFolderDialog.setView(input);
                    newFolderDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int j) {
                            final File newFolder=new File(currentPath+"/"+input.getText());
                            if(!newFolder.exists())
                            {
                                newFolder.mkdir();
                                refresh.callOnClick();
                                selection=new boolean[files.length];
                                textAdapter.setSelection(selection);


                            }
                        }
                    });
                    newFolderDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int j) {
                            dialogInterface.cancel();
                        }
                    });
                    newFolderDialog.show();
                }
            });

             btnRename=findViewById(R.id.rename);
             btnRename.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     final AlertDialog.Builder renameDialog= new AlertDialog.Builder(MainActivity.this);
                     renameDialog.setTitle("Rename to:");
                     final EditText input=new EditText(MainActivity.this);
                     final String renamePath=files[selectedItemIndex].getAbsolutePath();
                     input.setText(renamePath.substring(renamePath.lastIndexOf('/')));
                     input.setInputType(InputType.TYPE_CLASS_TEXT);
                     renameDialog.setView(input);
                     renameDialog.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int j) {

                             String s=new File(renamePath).getParent()+"/"+input.getText();
                             File newFile=new File(s);
                             new File(renamePath).renameTo(newFile);
                             refresh.callOnClick();
                             selection=new boolean[files.length];
                             textAdapter.setSelection(selection);
                             bottomBar.setVisibility(View.INVISIBLE);

                         }
                     });
                     renameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int j) {
                             dialogInterface.cancel();
                             selection=new boolean[files.length];
                             textAdapter.setSelection(selection);
                             bottomBar.setVisibility(View.INVISIBLE);

                         }
                     });
                     renameDialog.show();

                 }
             });

             final Button copyButton=(Button) findViewById(R.id.btnCopy);
             copyButton.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                      copyPath=files[selectedItemIndex].getAbsolutePath();
                     selection=new boolean[files.length];
                     textAdapter.setSelection(selection);
                     findViewById(R.id.paste).setVisibility(View.VISIBLE);



                 }
             });


             final Button pasteButton=(Button) findViewById(R.id.paste);
             pasteButton.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     pasteButton.setVisibility(View.GONE);
                     String dstPath=currentPath+copyPath.substring(copyPath.lastIndexOf('/'));
                     copy(new File(copyPath),new File(dstPath));
                     files=new File(currentPath).listFiles();
                     selection=new boolean[files.length];
                     textAdapter.setSelection(selection);
                     refresh.callOnClick();
                 }
             });
            isFileMangerInitialised=true;

        }else{
            refresh.callOnClick();

        }
    }

    private void copy(File src,File dst)
    {
        try{
            InputStream in=new FileInputStream(src);
            OutputStream out=new FileOutputStream(dst);
            byte[] buf=new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void deleteFileOrFolder(File fileOrFolder){
        if(fileOrFolder.isDirectory()){
            if(fileOrFolder.list().length==0)
            {
                fileOrFolder.delete();
            }
            else
            {
                String files[]=fileOrFolder.list();
                for(String temp:files)
                {
                    File fileToDelete=new File(fileOrFolder,temp);
                    deleteFileOrFolder(fileToDelete);
                }
                if(fileOrFolder.list().length==0)
                {
                    fileOrFolder.delete();
                }
            }
        }
        else
        {
            fileOrFolder.delete();
        }


    }

    class TextAdapter extends BaseAdapter{

        private List<String> data=new ArrayList<>();
        private boolean[] selection;

        private void setData(List<String> data)
        {
            if(data !=null)
            {
                this.data.clear();
            }
            if(data.size()>0)
            {
                this.data.addAll(data);
            }
            notifyDataSetChanged();
        }
        void setSelection(boolean[] selection)
        {
            if(selection!=null)
            {
                this.selection=new boolean[selection.length];
                for(int i=0;i<selection.length;i++)
                {
                    this.selection[i]=selection[i];
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null)
            {
                convertView= LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
                convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.textItem)));
            }
            ViewHolder holder=(ViewHolder) convertView.getTag();
            final String item=getItem(position);

            holder.info.setText(item.substring(item.lastIndexOf('/')+1));
            if(selection!=null)
            {

                    if(selection[position])
                    {
                        holder.info.setBackgroundColor(Color.GRAY);
                    }
                    else
                    {
                        holder.info.setBackgroundColor(Color.WHITE);
                    }



            }


            return convertView;
        }

        class ViewHolder{
            TextView info;

            ViewHolder(TextView info)
            {
                this.info=info;
            }

        }
    }

    private static final int REQUEST_PERMISSION=1234;

    private static final String[] PERMISSIONS={
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSIONS_COUNT=2;

    private boolean arePermissionsDenied(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            int p=0;
            while(p<PERMISSIONS_COUNT)
            {
                if(checkSelfPermission(PERMISSIONS[p])!= PackageManager.PERMISSION_GRANTED)
                {
                    return true;
                }
                p++;
            }

        }
        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==REQUEST_PERMISSION && grantResults.length>0)
        {
            if(arePermissionsDenied()){
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
                recreate();
            }
            else
            {
               onResume();
            }


        }
    }
}
