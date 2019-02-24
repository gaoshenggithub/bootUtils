package com.alisms.utils;

import java.util.Random;

public class RandomUtils {

	public static String randomNumber(){
		Random random = new Random();
		String randoms = String.valueOf(random.nextInt(999999));
		if (Integer.valueOf(randoms)<100000) {
			randoms = String.valueOf(Integer.valueOf(randoms)+100000);
		}
		return randoms;
	}
	public static void main(String[] args) {
		String str = RandomUtils.randomNumber();
		System.out.println(str);
	}
}
