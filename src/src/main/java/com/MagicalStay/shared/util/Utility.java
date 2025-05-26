package com.MagicalStay.shared.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class Utility {
    private static final Random random;


    //constructor estatico, inicializador estatico
    static {
        // semilla para el random
        long seed = System.currentTimeMillis();
        random = new Random(seed);
    }

    public static int random(int bound) {
        //return (int)Math.floor(Math.random()*bound); //forma 1
        return 1 + random.nextInt(bound);
    }
    

    public static void fill(int[] a) {
        for (int i = 0; i < a.length; i++) {
            a[i] = random(99);
        }
    }


    public static String format(long n) {
        return new DecimalFormat("###,###,###.##").format(n);
    }

    public static int min(int x, int y) {
        return x < y ? x : y;
    }

    public static int max(int x, int y) {
        return x > y ? x : y;
    }

    public static String show(int[] a) {
        String result = "";
        for (int item : a) {
            if (item == 0) break; //si es cero es xq no hay mas elementos
            result += item + " ";
        }
        return result;
    }

    public static int compare(Object a, Object b) {
        switch (instanceOf(a, b)) {
            case "Integer":
                Integer int1 = (Integer) a;
                Integer int2 = (Integer) b;
                return int1 < int2 ? -1 : int1 > int2 ? 1 : 0;

            case "String":
                String str1 = (String) a;
                String str2 = (String) b;
                return str1.compareTo(str2) < 0 ? -1 : str1.compareTo(str2) > 0 ? 1 : 0;

            case "Character":
                Character ch1 = (Character) a;
                Character ch2 = (Character) b;
                return ch1.compareTo(ch2) < 0 ? -1 : ch1.compareTo(ch2) > 0 ? 1 : 0;
        }
        return 2; //Unknown
    }


    public static String instanceOf(Object a, Object b) {
        if (a instanceof Integer && b instanceof Integer) return "Integer";
        if (a instanceof String && b instanceof String) return "String";
        if (a instanceof Character && b instanceof Character) return "Character";
        return "Unknown";
    }


    public static String dateFormat(Date value){
        return new SimpleDateFormat("dd/MM/yyyy").format(value);
    }

    public static int getAge(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar birth = Calendar.getInstance();
        birth.setTime(date);

        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        int monthDiff = today.get(Calendar.MONTH) - birth.get(Calendar.MONTH);

        if (monthDiff < 0 || (monthDiff == 0 && today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        return age;
    }

}
