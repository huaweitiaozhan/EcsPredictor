package com.elasticcloudservice.predict;

/**
 * Created by thinkpad on 2018/3/28.
 */
public class StandardUtil {

    //cpu核数，以个为单位
    private int CPU;

    //内存大小，以GB为单位
    private int Mem;

    //硬盘大小，以GB为单位，暂时不需要使用
    private int hardDrive;

    public StandardUtil(int CPU, int mem, int hardDrive) {
        this.CPU = CPU;
        Mem = mem;
        this.hardDrive = hardDrive;
    }


    public int getCPU() {
        return CPU;
    }

    public void setCPU(int CPU) {
        this.CPU = CPU;
    }

    public int getMem() {
        return Mem;
    }

    public void setMem(int mem) {
        Mem = mem;
    }

    public int getHardDrive() {
        return hardDrive;
    }

    public void setHardDrive(int hardDrive) {
        this.hardDrive = hardDrive;
    }
}
