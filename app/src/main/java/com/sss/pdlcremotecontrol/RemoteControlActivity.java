package com.sss.pdlcremotecontrol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class RemoteControlActivity extends Activity implements View.OnClickListener {
    private static final String LogTag = "Remote";
    private static final String LogTagSocket = "Socket";
    private static final String BoardCastIP = "224.8.8.8";
    private static final int    BoardCastPort = 11888;
    public static final int MSG_HIDE_PG        = 2;
    public static final int MSG_FIND_DEV        = 3;
    public static final int MSG_LOSTALL_DEV       = 4;

    private boolean RunBoardSocketFlag;
    private Spinner spinner;
    private ProgressDialog pd;
    private boolean pdIsShow;
    private LinkedList<DeviceInfo> DevList=null;
    private String MacID;
    private InetAddress addr=null;
    private Socket tcpSocket=null;
    private int adjValue= 0x08;

    Handler handler ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_control);
        spinner = (Spinner) findViewById(R.id.spinner);

        findViewById(R.id.poweron).setVisibility(View.INVISIBLE);
        //findViewById(R.id.mute).setVisibility(View.INVISIBLE);
        findViewById(R.id.poweroff).setVisibility(View.INVISIBLE);
        //findViewById(R.id.back).setVisibility(View.INVISIBLE);
        findViewById(R.id.home).setVisibility(View.INVISIBLE);
        //findViewById(R.id.menu).setVisibility(View.INVISIBLE);

        findViewById(R.id.poweron).setOnClickListener(this);
        findViewById(R.id.mute).setOnClickListener(this);
        findViewById(R.id.poweroff).setOnClickListener(this);
        findViewById(R.id.up).setOnClickListener(this);
        findViewById(R.id.down).setOnClickListener(this);
        findViewById(R.id.left).setOnClickListener(this);
        findViewById(R.id.right).setOnClickListener(this);
        findViewById(R.id.ok).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.menu).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.play).setOnClickListener(this);
        findViewById(R.id.trans).setOnClickListener(this);
        findViewById(R.id.untrans).setOnClickListener(this);
        findViewById(R.id.prjoff).setOnClickListener(this);
        findViewById(R.id.prjon).setOnClickListener(this);



        pdShow();
        DevList = new LinkedList<DeviceInfo>();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                List<String> data_list;
                ArrayAdapter<String> arr_adapter;
                super.handleMessage(msg);

                if(msg.what == MSG_FIND_DEV) {
                    Log.d(LogTagSocket, "recv msg:MSG_FIND_DEV");
                    pdHide();
                    //数据
                    data_list = new ArrayList<String>();
                    for(int i=0;i<DevList.size();i++)
                    {
                        DeviceInfo tmp = DevList.get(i);
                        data_list.add(tmp.ID);
                        if(i==0)
                        {
                            MacID = tmp.ID;
                            addr = tmp.IP;
                        }
                    }
                    //适配器
                    arr_adapter= new ArrayAdapter<String>(RemoteControlActivity.this, android.R.layout.simple_spinner_item, data_list);
                    //加载适配器
                    spinner.setAdapter(arr_adapter);
                }
                else if(msg.what == MSG_LOSTALL_DEV) {
                    Log.d(LogTagSocket, "recv msg:MSG_LOSTALL_DEV");
                    pdShow();
                }
                else if(msg.what == MSG_HIDE_PG) {
                    pdHide();
                }
            }
        };
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Log.d(LogTag, "enter RunSocketThread");
        RunBoardSocketFlag = true;
        RunSocketThread();
    }
    @Override
    protected void onStop() {
        super.onStop();
        RunBoardSocketFlag = false;
        Log.d(LogTag, "Exit RunSocketThread");
    }
    @Override
    public void onClick(View v) {
/*
* key mute:  164
* key up:    19
* key down:  20
* key left:  21
* key right: 22
* key ok:    23
* key back:  4
* key home:
* key menu:  131
* key play:  51
* key pause: 45
* key prjoff 132
* key prjon  133
* key pdlc on  134
* key pdlc off 135
* */
        switch (v.getId()) {
            case R.id.poweron:
                Log.d(LogTag, "click id= R.id.poweron");
                break;
            case R.id.mute:
                Log.d(LogTag, "click id= R.id.mute");
                postNetKey(164);
                break;
            case R.id.up:
                Log.d(LogTag, "click id= R.id.up");
                postNetKey(19);
                break;
            case R.id.down:
                Log.d(LogTag, "click id= R.id.down");
                postNetKey(20);
                break;
            case R.id.left:
                Log.d(LogTag, "click id= R.id.left");
                postNetKey(21);
                break;
            case R.id.right:
                Log.d(LogTag, "click id= R.id.right");
                postNetKey(22);
                break;
            case R.id.ok:
                Log.d(LogTag, "click id= R.id.ok");
                postNetKey(23);
                break;
            case R.id.back:
                Log.d(LogTag, "click id= R.id.back");
                postNetKey(4);
                break;
            case R.id.menu:
                Log.d(LogTag, "click id= R.id.menu");
                postNetKey(131);
                break;
            case R.id.play:
                Log.d(LogTag, "click id= R.id.play");
                postNetKey(51);
                break;
            case R.id.stop:
                Log.d(LogTag, "click id= R.id.stop");
                postNetKey(45);
                break;
            case R.id.trans:
                Log.d(LogTag, "click id= R.id.trans on");
                postNetKey(134);
                break;
            case R.id.untrans:
                Log.d(LogTag, "click id= R.id.trans off");
                postNetKey(135);
                break;
            case R.id.prjoff:
                Log.d(LogTag, "click id= R.id.prjoff");
                postNetKey(132);
                break;
            case R.id.prjon:
                Log.d(LogTag, "click id= R.id.prjon");
                postNetKey(133);
                break;
            default:
                break;
        }
    }

    //局域网内使用,与系统提供的remoteIME.apk功能一至，在系统范围内起作用
    private void postNetKeyOld(final int code)
    {
        if(addr == null){
            return;
        }
        pdShowWait();
        new Thread() {
            public void run() {
                JSONObject msg = new JSONObject();

                try {
                    DatagramSocket socket = null;
                    try {
                        socket = new DatagramSocket(11999);
                        msg.put("value", code);
                        msg.put("action", "key");
                        msg.put("ID", MacID);

                        String sendStr = msg.toString();
                        byte buff[] = new byte[1024];
                        try {
                            buff = sendStr.getBytes("utf-8");
                            //设定UDP报文
                            DatagramPacket packet = new DatagramPacket(buff, buff.length,addr, 11999);
                            try {
                                socket.send(packet);//发送报文
                                Log.d(LogTag, "Socket Send OK");
                                /*
                                * 想设置震动大小可以通过改变pattern来设定，如果开启时间太短，震动效果可能感觉不到
                                * */
                                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                                vibrator.vibrate(100);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        socket.close();

                        try {
                            sleep(1500);
                            Message message = new Message();
                            message.what = MSG_HIDE_PG;
                            handler.sendMessage(message);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    //局域网内使用，在pdlcPlayer运行的时候才起作用
    private void postNetKey(final int code)
    {
        if(tcpSocket == null){
            return;
        }
        if(164 == code) adjValue=0x06;                    //mute
        else if(19  == code) adjValue=0x07;              //up
        else if(20  == code) adjValue=0x08;              //down
        else if(21  == code) adjValue=0x09;              //left
        else if(22  == code) adjValue=0x0A;              //right
        else if(23  == code) adjValue=0x0B;              //ok
        else if(4   == code) adjValue=0x02;              //back
        else if(131 == code) adjValue=0x01;              //menu
        else if(51  == code) adjValue=53;              //play
        else if(45  == code) adjValue=47;              //pause
        else if(132 == code) adjValue=34;              //prjoff
        else if(133 == code) adjValue=35;              //prjon
        else if(134 == code) adjValue=36;              //pdlc on             12 poweroff
        else if(135 == code) adjValue=37;              //pdlc off

        //pdShowWait();

        new Thread() {
            public void run() {
                String dest = addr.getHostName();

                try {
                    byte buff[] = new byte[32];

                    buff[0]= 0x00;buff[1]= 0x00;buff[2]= 0x00;buff[3]= 0x04;
                    buff[4]= 0x01;buff[5]= 0x00;buff[6]= (byte) (adjValue&0xff);buff[7]= 0x00;

                    OutputStream outputStream = tcpSocket.getOutputStream();

                    outputStream.write(buff,0,8);
                    outputStream.flush();
                    Log.d(LogTag, "Socket Send OK");

                    /*
                    * 想设置震动大小可以通过改变pattern来设定，如果开启时间太短，震动效果可能感觉不到
                    * */
                    Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(50);
                    buff[4]= 0x01;buff[5]= 0x01;buff[6]= (byte) (adjValue&0xff);buff[7]= 0x00;
                    outputStream.write(buff, 0, 8);
                    outputStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }


//                try {
//                    sleep(1200);
//                    Message message = new Message();
//                    message.what = MSG_HIDE_PG;
//                    handler.sendMessage(message);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }.start();

    }

    private void RunSocketThread() {
        new Thread() {
            public void run() {
                InetAddress subaddr;
                int subNetAddr=0;
                byte buffer[] = new byte[1024];
                DatagramSocket socket = null;

                WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                WifiInfo Coninfo = wifi.getConnectionInfo();
                int ipAddress = Coninfo.getIpAddress();  //获取ip地址

                String sendStr = "amlogic-client-scan";
                try {
                    buffer = sendStr.getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                try {
                    socket = new DatagramSocket();
                    while(RunBoardSocketFlag)
                    {
                        subNetAddr = (subNetAddr+1)%256;
                        String host = (ipAddress & 0xFF ) + "." +
                                ((ipAddress >> 8 ) & 0xFF) + "." +
                                ((ipAddress >> 16 ) & 0xFF) + "."+subNetAddr;
                        try {
                            subaddr = InetAddress.getByName(host);
                            //设定UDP报文
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length,subaddr, 7001);
                            try {
                                socket.send(packet);//发送报文
                                socket.setSoTimeout(10);
                                socket.receive(packet);              //接收多播报文，程序停滞等待直到接收到报文

                                DeviceInfo info = new DeviceInfo();
                                info.ID = packet.getSocketAddress().toString();
                                info.IP = packet.getAddress();
                                Date dt= new Date();
                                info.time= dt.getTime();
                                for(int i=0;i<DevList.size();i++)
                                {
                                    DeviceInfo tmp = DevList.get(i);
                                    if(tmp.ID.equals(info.ID))
                                    {
                                        DevList.remove(i);
                                        break;
                                    }
                                }

                                try {
                                    tcpSocket = new Socket(packet.getAddress().getHostName(), 7002);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                DevList.add(info);
                                Message message = new Message();
                                message.what = MSG_FIND_DEV;
                                handler.sendMessage(message);

                            } catch(SocketTimeoutException e) {
                                if(subNetAddr == 254 && DevList.size() > 0)
                                {
                                    Log.d(LogTag, "Scan Finish...");
                                    RunBoardSocketFlag = false;
                                }
                                //Log.d(LogTag, "Find Next ..."+subNetAddr);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(LogTag, "Socket Thread Finish");
                    socket.close();//关闭套接字
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void RunSocketThreadOld() {
        new Thread() {
            public void run() {
                byte buffer[] = new byte[1024];
                MulticastSocket s = null;        //生成套接字并绑定端口
                try {
                    s = new MulticastSocket(BoardCastPort);
                    InetAddress group = InetAddress.getByName(BoardCastIP);//设定多播IP
                    s.joinGroup(group);         //接受者加入多播组，需要和发送者在同一组

                    DatagramPacket packet = new DatagramPacket(buffer , 1024);//创建接收报文，以接收通过多播传递过来的报文

                    while(RunBoardSocketFlag)
                    {
                        try {
                            s.setSoTimeout(1000);
                            s.receive(packet);              //接收多播报文，程序停滞等待直到接收到报文
                            String recv = new String(packet.getData() , packet.getOffset() , packet.getLength());
                            Log.d(LogTagSocket,"Socket Recv:"+recv);
                            JSONTokener jsonParser = new JSONTokener(recv);
                            try {
                                JSONObject jsonInfo = (JSONObject) jsonParser.nextValue();
                                DeviceInfo info = new DeviceInfo();

                                info.ID = jsonInfo.getString("ID");
                                info.IP = packet.getAddress();
                                Date dt= new Date();
                                info.time= dt.getTime();
                                for(int i=0;i<DevList.size();i++)
                                {
                                    DeviceInfo tmp = DevList.get(i);
                                    if(tmp.ID.equals(info.ID))
                                    {
                                        DevList.remove(i);
                                        break;
                                    }
                                }

                                DevList.add(info);
                                Message message = new Message();
                                message.what = MSG_FIND_DEV;
                                handler.sendMessage(message);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } catch(SocketTimeoutException e) {
                            //do nothing
                            Date dt= new Date();
                            long curTime= dt.getTime();
                            for(int i=0;i<DevList.size();i++)
                            {
                                DeviceInfo tmp = DevList.get(i);
                                if(curTime - tmp.time > 10000)
                                {
                                    DevList.remove(i);
                                }
                            }

                            if(DevList.size()==0)
                            {
                                Message message = new Message();
                                message.what = MSG_LOSTALL_DEV;
                                handler.sendMessage(message);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(LogTag,"Socket Thread Finish");
                    s.leaveGroup(group);
                    s.close();//关闭套接字
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    private void pdShow()
    {
        if(!pdIsShow) {
            pd = new ProgressDialog(RemoteControlActivity.this);
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
            pd.setMessage("查找设备中，请稍后……");
            pd.show();
            //pd = ProgressDialog.show(RemoteControlActivity.this, "标题", "查找设备，请稍后……");
            pdIsShow = true;
        }
    }

    private void pdShowWait()
    {
        if(!pdIsShow) {
            pd = new ProgressDialog(RemoteControlActivity.this);
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
            pd.setMessage("请稍后……"+adjValue);
            pd.show();
            //pd = ProgressDialog.show(RemoteControlActivity.this, "标题", "请稍后……");
            pdIsShow = true;
        }
    }

    private void pdHide()
    {
        pd.dismiss();
        pdIsShow = false;
    }


}
