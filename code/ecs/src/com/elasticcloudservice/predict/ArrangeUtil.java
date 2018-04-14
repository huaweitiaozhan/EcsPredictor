package com.elasticcloudservice.predict;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thinkpad on 2018/3/28.
 */
public class ArrangeUtil {


    //注意有的是以GB为单位，而虚拟机规格是MB,所以要乘以1024
    //注意predictresult不再是15个int!!!!!!!!!!
    public static ArrayList<ArrayList<Integer>> arrangeVm(StandardUtil standardUtil,boolean resourceKind,int[] predictResult){
        List<ArrayList<String>> arrangeResult = new ArrayList<ArrayList<String>>();

        //虚拟机规格，注意全部是从0开始,因为是从1开始，所以在0处垫一位
        int [] VmCpuStandard = new int[]{0,1,1,1,2,2,2,4,4,4,8,8,8,16,16,16};
        int [] VmMemStandard = new int[]{0,1,2,4,2,4,8,4,8,16,8,16,32,16,32,64};




        if(resourceKind){
            return packingSolution(standardUtil.getMem(),standardUtil.getCPU(),predictResult,VmCpuStandard,VmMemStandard);
        }

        else{
            return packingSolution(standardUtil.getCPU(),standardUtil.getMem(),predictResult,VmMemStandard,VmCpuStandard);
        }




    }

    /**
     * 求解0-n背包问题的私有方法
     * @param minor 上界元素的容量，即只需要保证不超过即可
     * @param main 优化元素的容量
     * @param volumn 虚拟机数量
     * @param MainStandrad 不同规格优化元素的规格
     * @param MinorStandard 不同规格虚拟机上界元素的规格
     * @return
     */
    private static ArrayList<ArrayList<Integer>> packingSolution(int minor,int main,int[] volumn,int[] MainStandrad,int[] MinorStandard){

        /*

        System.out.println("minor="+minor+"  main="+main);

        for(int i=0;i<=volumn.length-1;i++){
            System.out.println("volumn"+i+" "+volumn[i]);
        }
        */
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();

        while(true) {
            boolean judge = true;
            for (int i = 0; i <= volumn.length - 1; i++) {
                if (volumn[i] > 0) {
                    judge = false;
                    List<Integer> mid = BackPackSolution(main, minor, volumn, MainStandrad, MinorStandard);


                    ArrayList<Integer> midlist = new ArrayList<Integer>();

                    /*
                   for(int o=0;o<=mid.size()-1;o++){
                       System.out.println(mid.get(o));
                   }
                   */

                    //因为要有15个位置，所以需要15个
                    for(int k=1;k<=16;k++){
                        midlist.add(0);
                    }
                    for(int j=0;j<=mid.size()-1;j++){


                        midlist.set(volumn[mid.get(j)],midlist.get(volumn[mid.get(j)])+1);


                        //设为0
                        //volumn[mid.get(i)] = 0;//2017-4-8
                        volumn[mid.get(j)] = 0;

                        for(int k=0;k<=volumn.length-1;k++){
                            System.out.print(volumn[k]);
                        }
                        System.out.println();
                    }


                    System.out.println(midlist.get(1));
                    result.add(midlist);

                }
            }

            if(judge){
                break;
            }
        }
        return result;

    }


    /**
     * 由于二维数组中有一维是main的大小，现在是之间拿main的大小，以后或许可以改进一下，把main表示成最小的单位1024的商
     * @param main
     * @param minor
     * @param volumn
     * @param MainStandard
     * @param MinorStandard
     * @return
     */
    private static List<Integer> BackPackSolution(int main, int minor, int[] volumn, int[] MainStandard, int[] MinorStandard){


       // System.out.println(volumn[0]+" "+volumn[1]);

        //volumn==0则表示无效

        //p表示每个虚拟机的价值，在目前情况下默认是1
     //   int[] val = new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};

        //这里存在疑问
        int n = getLength(volumn);


        int[][] f = new int[n+1][main+1];

        /**这里是用来表示辅助界限的数组**/
        int[][] ceil = new int[n+1][main+1];
       // int[][] path = new int[n+1][main+1];

        //System.out.println(n);
      //  System.out.println("行："+n+1+"  "+"列："+(main+1));
        //初始化第一列和第一行
        for(int i=0;i<f.length;i++){
            f[i][0] = 0;
        }
        for(int i=0;i<f[0].length;i++){
            f[0][i] = 0;
        }

        for(int i=0;i<ceil.length;i++){
            ceil[i][0] = 0;
        }
        for(int i=0;i<ceil[0].length;i++){
            ceil[0][i] = 0;
        }


       //int ceil = 0;
        //通过公式迭代计算
        //这里ceil还是有bug,需要重新考虑
        for(int i=1;i<f.length;i++){
          //###  int ceil = 0;
            for(int j=1;j<f[0].length;j++){
               //## if(MainStandard[volumn[getIndex(volumn,i-1)]]>j||MinorStandard[volumn[getIndex(volumn,i-1)]]>minor-ceil)
                if(MainStandard[volumn[getIndex(volumn,i-1)]]>j) {
                   //System.out.println("?????????????");
                    f[i][j] = f[i - 1][j];
                    ceil[i][j] = ceil[i - 1][j];
                }
                else{
                   //## if(f[i-1][j]<f[i-1][j-MainStandard[volumn[getIndex(volumn,i-1)]]]+MainStandard[volumn[getIndex(volumn,i-1)]]){
                    if(f[i-1][j]<f[i-1][j-MainStandard[volumn[getIndex(volumn,i-1)]]]+MainStandard[volumn[getIndex(volumn,i-1)]]&&ceil[i-1][j-MainStandard[volumn[getIndex(volumn,i-1)]]]+MinorStandard[volumn[getIndex(volumn,i-1)]]<=minor){
                        //System.out.println("!!!!!!!!!!!!!!");
                        f[i][j] = f[i-1][j-MainStandard[volumn[getIndex(volumn,i-1)]]]+MainStandard[volumn[getIndex(volumn,i-1)]];
                        ceil[i][j] = ceil[i-1][j-MainStandard[volumn[getIndex(volumn,i-1)]]]+MinorStandard[volumn[getIndex(volumn,i-1)]];
                        //##  ceil = ceil+ MinorStandard[volumn[getIndex(volumn,i-1)]];
                       // path[i][j] = 1;
                    }else{
                       // System.out.println("@@@@@@");
                        f[i][j] = f[i-1][j];
                        ceil[i][j] = ceil[i-1][j];
                    }
                    //f[i][j] = Math.max(f[i-1][j], f[i-1][j-weight[i-1]]+val[i-1]);
                }
            }

        }

        int max = f[n][main];

        //System.out.println(max);
        for(int i=0;i<=f.length-1;i++){
            for(int j=0;j<=f[i].length-1;j++){
                System.out.print(f[i][j]+"  ");
            }
            System.out.println();
        }

        System.out.println("**************");
        for(int i=0;i<=f.length-1;i++){
            for(int j=0;j<=f[i].length-1;j++){
                System.out.print(ceil[i][j]+"  ");
            }
            System.out.println();
        }
        int row = n;
        int column = main;


        List<Integer> result = new ArrayList<Integer>();
       while(max>0){
           if(f[row][column]==f[row-1][column]){
               row--;
           }
           else{
               if(f[row][column]==f[row][column-1]){
                   column--;
               }
               else{
                   //result.add(getIndex(volumn,row));
                   result.add(getIndex(volumn,row-1));
                   System.out.println("装入"+volumn[getIndex(volumn,row-1)]);

                   max = max - MainStandard[volumn[getIndex(volumn,row-1)]];


                   /*
                   for(int i=0;i<=main;i++){
                       if(f[n][i]==max){
                           row = n;
                           column = i;
                       }
                   }
                   */

                   for(int i=0;i<=main;i++){
                       if(f[row][i]==max){

                           column = i;
                       }
                   }

                  // result.add(getIndex(volumn,row));
                  // System.out.println("装入"+volumn[getIndex(volumn,row)]);

               }
           }
       }



       return result ;


    }

    private static int getLength(int[] volumn){
        int num =0;
        for(int i=0;i<=volumn.length-1;i++){
            if(volumn[i]!=0){
                num++;
            }
        }
        return num;
    }

    private static int getIndex(int[] volumn,int index){
        int result =-1;
        for(int i=0;i<=volumn.length-1;i++){

            if(volumn[i]!=0){
                result++;
            }

            if(result == index){
                return i;
            }


        }
        return 0;
    }

    public static void main(String[] args){
        int[] predict = new int[]{1,1,1,1,1,1,4,7,11,13,14,14,12,11,6,5,4,3,2,15};

        StandardUtil standardUtil =new StandardUtil(33,248,1200);

        ArrayList<ArrayList<Integer>> arrayLists = arrangeVm(standardUtil,true,predict);

    }

}
