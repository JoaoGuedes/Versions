package com.boston.versions;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

public class DiffTasks {
	
	public static List<String> stringToLines(ByteArrayOutputStream stream) {
        List<String> lines = new LinkedList<String>();
        String[] streamToArray;
        try {
        	if (stream.toString("utf-8").contains("\r\n") )
        		streamToArray = stream.toString().replace("\r\n", "\n").split("\n");
        	else
        		streamToArray = stream.toString("utf-8").split("\n");
						
			for (String it: streamToArray) {
				lines.add(it);
			}					
			
			return lines;
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

        return null;
	}
	
	
}
