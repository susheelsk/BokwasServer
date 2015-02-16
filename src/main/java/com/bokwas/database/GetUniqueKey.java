package com.bokwas.database;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GetUniqueKey {

	public static String getKey(String fbid) {
		String key;
		try{
		 key = md5("jsbhgjfgjhahf12t@35u579015786wet4m6nu8iv81hhvh8"+fbid);
		 return key;
		}catch(Exception e){
			    int h = 0;
			    for (int i = 0; i < fbid.length(); i++) {
			        h = 31 * h + fbid.charAt(i);
			    }
			    return ""+h;
		}
	}
	 public static String md5(String input) {
         
	        String md5 = null;
	         
	        if(null == input) return null;
	         
	        try {
	             
	        //Create MessageDigest object for MD5
	        MessageDigest digest = MessageDigest.getInstance("MD5");
	         
	        //Update input string in message digest
	        digest.update(input.getBytes(), 0, input.length());
	 
	        //Converts message digest value in base 16 (hex) 
	        md5 = new BigInteger(1, digest.digest()).toString(16);
	 
	        } catch (NoSuchAlgorithmException e) {
	 
	            e.printStackTrace();
	        }
	        return md5;
	    }
}
