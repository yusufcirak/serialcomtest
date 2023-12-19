package com.ycirak.serialcomtest;



import android.util.Log;

import com.ycirak.serialcomtest.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Huangsitao
 * @date 2019/8/22
 * @version 1.0
 */
public class SerialPortUtils {

    private final String TAG = "SerialPortUtils";
    /** 波特率 */
    private static final int BAUD_RATE = 19200;
    /** 是否打开串口标志 */
    private boolean serialPortStatus = false;
    /** 线程状态，为了安全终止线程 */
    private boolean threadStatus;

    /** 协议头 */
    private static final byte AGREEMENT_HEAD = (byte)0xA5;
    private static final byte TIME_HEAD = (byte)0xB5;
    /** 协议头长度，单位为字节 */
    private static final int HEAD_LENGTH = 1;
    /**
     * 数据长度，不是数据总长度，
     * DATA_LENGTH = 数据长度 + crc校验码长度
     * 单位为字节
     */
    private static final int DATA_LENGTH = 1;
    /** 类型长度 */
    private static final int TYPE_LENGTH = 1;
    /** CRC校验的长度，单位为字节 */
    private static final int CRC_VERIFY_LENGTH = 2;
    /** 数据最小的长度 */
    private static final int MIN_DATA_LENGTH = 6;

    private SerialPort serialPort = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private OnDataReceiveListener onDataReceiveListener = null;

    /** 缓存的数据 */
    private byte[] data = new byte[64];
    /** 累计保存的数据的长度 */
    private int dataLength;

    /**
     * 打开串口
     */
    public void openSerialPort(String path, String suPath){

        try {
            serialPort = new SerialPort(new File(path), suPath, BAUD_RATE,0);
            this.serialPortStatus = true;
            //线程状态
            threadStatus = false;

            //获取打开的串口中的输入输出流，以便于串口数据的收发
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();

            new ReadThread().start(); //开始线程监控是否有数据要接收
        } catch (IOException e) {
            System.out.println(TAG + " openSerialPort: cc" + e.toString());
        }
        System.out.println(TAG + " openSerialPort: 打开串口");
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort(){
        try {
            inputStream.close();
            outputStream.close();

            this.serialPortStatus = false;
            //线程状态
            this.threadStatus = true;
            serialPort.close();
        } catch (IOException e) {
            System.out.println(TAG + " closeSerialPort: 关闭串口异常："+e.toString());
            return;
        }
        System.out.println(TAG + " closeSerialPort: 关闭串口成功");
    }

    /**
     * 发送串口指令（字符串）
     * @param data String数据指令
     */
    public void sendSerialPort(String data){
        System.out.println(TAG + " sendSerialPort: 发送数据");

        try {
            //string转byte[]
            byte[] sendData = data.getBytes();
            if (sendData.length > 0) {
                outputStream.write(sendData);
                outputStream.write('\n');
                //outputStream.write('\r'+'\n');
                outputStream.flush();
                System.out.println(TAG + " sendSerialPort: 串口数据发送成功");
            }
        } catch (IOException e) {
            System.out.println(TAG + " sendSerialPort: 串口数据发送失败："+e.toString());
        }



    }

    //    public boolean sendBytesSerialPort(byte[] data){
    //      boolean result = false;
    //      byte[] bytes = DataOperationUtils.addCycBytes(data, CRC_VERIFY_LENGTH);

    //      if(bytes.length > CRC_VERIFY_LENGTH){
    //         try {
    //              outputStream.write(bytes);
    //              outputStream.flush();
    //              System.out.println(TAG + " 发送数据成功");
    //            result = true;
    //       } catch (IOException e) {
    //              System.out.println(TAG + " 发送数据失败");
    //             e.printStackTrace();
    //             result = false;
    //          }
    //     }
    //      return result;
    //  }

    public String bytes2HexString(byte[] b, int length) {
        String r = "";
        for (int i = 0; i < length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            r += hex.toUpperCase();

        }
        return r;
    }

    int tmp =0;
    byte[] cmd = new byte[64];
    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            //判断进程是否在运行，更安全的结束进程
            while (!threadStatus){
                try {
                    //64   1024
                    byte[] buffer = new byte[64];
                    //读取数据的大小
                    int size;
                    size = inputStream.read(buffer);
                    int start = checkhead(buffer, size);
                    Log.d("TAG", "onDataReceivestart=: "+ start);
                    Log.d("TAG", "onDataReceive: "+ bytes2HexString(buffer, size));
                    if (start == -1 ){
                        if (size >= 7){
                            System.arraycopy(buffer,0,cmd,0,7);
                            tmp = size -7;
                            onDataReceiveListener.onDataReceive(cmd[0],cmd);
                            if (tmp != 0)
                                System.arraycopy(buffer,size,cmd,0,size);

                        }else if (size < 7){
                            System.arraycopy(buffer,0,cmd,0,size);
                            tmp = size;
                        }
                    }else if (start ==-2){
                        if (size >= 17){
                            System.arraycopy(buffer,0,cmd,0,17);
                            Log.d("TAG", "run: "+cmd[0]);
                            onDataReceiveListener.onDataReceive(cmd[0],cmd);
                            tmp = size -17;
                            if (tmp != 0)
                                System.arraycopy(buffer,size,cmd,0,size);
                        }else if (size < 17){
                            System.arraycopy(buffer,0,cmd,0,size);
                            tmp = size;
                        }
                    }else if (start == -10){
                        System.arraycopy(buffer,0,cmd,tmp,size);
                        tmp+= size;
                        if (tmp == 17 || tmp == 7)
                            onDataReceiveListener.onDataReceive(cmd[0],cmd);
                    }else {
                        System.arraycopy(buffer,0,cmd,tmp,start);
                        onDataReceiveListener.onDataReceive(cmd[0],cmd);
                        tmp = 0;
                        System.arraycopy(buffer,start,cmd,tmp,size - start);
                    }
                } catch (IOException e) {
                    System.out.println(TAG + " run: 数据读取异常：" +e.toString());
                } catch (Exception e){
                    dataLength = 0;
                    data = null;
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 这是获得数据的监听接口，实现该接口的类，能够获得接口传来的数据
     */
    public interface OnDataReceiveListener {
        /**
         * 用于获得串口发来的数据
         * @param data 数据的格式
         */
        void onDataReceive(byte data,byte[] buff);
    }

    /**
     * 用于检测 是否是协议头
     * @param lenth
     * @return Boolean
     */
    private int  checkhead(byte[] buffer ,int lenth){
        for (int i = 0; i < lenth - 1; i++){
            if (buffer[i] == AGREEMENT_HEAD || buffer[i]  == TIME_HEAD){
                if (i == 0 && buffer[i] == AGREEMENT_HEAD ){
                    return -1;
                }else if (i == 0 && buffer[i] == TIME_HEAD){
                    return -2;
                }

                return i;
            }
        }
        return -10;
    }
    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

    /**
     *
     * @param data 原始的数据
     * @param size 传来的数据长度
     * @return 返回解析出来的数据，如果数据错误的话，则返回一个空的serialPortData
     */
    public byte verifyDataAndParseData(byte[] data, int size) throws ArrayIndexOutOfBoundsException {
        byte[] buffer = new byte[size];
        System.arraycopy(data, 0, buffer, 0, size);
        // 最小长度验证
//        if(size < MIN_DATA_LENGTH
//                // crc 校验
//                ||!DataOperationUtils.crcVerifyData(DataOperationUtils.byteArrayToShortArray(buffer), CRC_VERIFY_LENGTH)
//                // 判断指令头是否正确
//                || DataOperationUtils.mergeData(buffer[0], buffer[1]) != AGREEMENT_HEAD
//                // 比较数据长度
//                || buffer[2] != buffer.length - HEAD_LENGTH - DATA_LENGTH){
//        }
        return buffer[1];

    }

}

