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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Button b1,b2,b3,b4,b5;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                notifyDataSetChanged();
            }
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
    private boolean isFileMangerInitialised=false;

    private boolean[] selection;
    private File[] files;
    private  List<String> filesList;

    @Override
    protected void onResume() {
        super.onResume();

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && arePermissionsDenied())
        {
            requestPermissions(PERMISSIONS,REQUEST_PERMISSION);
            return;
        }
        if(!isFileMangerInitialised)
        {
             String rootpath=String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            final File dir=new File(rootpath);
             files=dir.listFiles();
             TextView pathOutput=(TextView) findViewById(R.id.pathOutput);
            pathOutput.setText(rootpath.substring(rootpath.lastIndexOf('/')+1));
            final int filesFoundCount=files.length;
            final TextAdapter textAdapter=new TextAdapter();
            listView=(ListView) findViewById(R.id.listView);
            listView.setAdapter(textAdapter);
            filesList=new ArrayList<>();
            for(int i=0;i<filesFoundCount;i++)
            {
                filesList.add(String.valueOf(files[i].getAbsolutePath()));
            }
            textAdapter.setData(filesList);
            selection=new boolean[files.length];
           listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
               @Override
               public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                   selection[position]=!selection[position];
                   textAdapter.setSelection(selection);
                   boolean isAtleastOneSelected=false;
                   for(int i=0;i<selection.length;i++)
                   {
                       if(selection[i])
                       {
                           isAtleastOneSelected=true;
                           break;
                       }
                   }
                   if(isAtleastOneSelected)
                   {
                       b1.setVisibility(View.VISIBLE);
                   }
                   else
                   {
                       b1.setVisibility(View.GONE);
                   }
                   return false;
               }
           });
            b1=(Button) findViewById(R.id.b1);
            b2=(Button) findViewById(R.id.b2);
            b3=(Button) findViewById(R.id.b3);
            b4=(Button) findViewById(R.id.b4);
            b5=(Button) findViewById(R.id.b5);

            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    b1.setVisibility(View.INVISIBLE);
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
                            files=dir.listFiles();
                            filesList.clear();
                            for(int i=0;i<files.length;i++)
                            {
                                filesList.add(String.valueOf(files[i].getAbsolutePath()));
                            }
                            textAdapter.setData(filesList);
                            textAdapter.setSelection(selection);

                        }
                    });
                    deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    deleteDialog.show();
                }
            });



            isFileMangerInitialised=true;

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
