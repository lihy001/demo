package com.heng.lucene.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileUtil {

	/**
	 * parse the file to string
	 * 
	 * @param file
	 * @return
	 */
	public static String parsefiletostring(File file) {
		String strresult = "", tmp = "";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			while ((tmp = br.readLine()) != null) {
				strresult += tmp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != br) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(strresult);
		return strresult;
	}

}