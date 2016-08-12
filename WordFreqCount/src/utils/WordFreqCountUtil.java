package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 统计英文单词词频
 * 
 * @author leaderway
 *
 */
public class WordFreqCountUtil {
	
	/**
	 * 统计英文单词词频工具方法
	 * 
	 * @param fileDirectoryPath 分割后文件的目录
	 */
	public static void wordFeqCount(String fileDirectoryPath) {
		File directory = new File(fileDirectoryPath);
		File[] files = directory.listFiles();//获取所有分割后的文件
		
		String temp  = null;
		Map<String, Integer> wordCount = new HashMap<String, Integer>();//构建一个hashmap存放单词及词频
		try{
			for(File file : files){//将文件夹中的文件循环读取出来
				StringBuffer content = new StringBuffer();//重新实例化一个StringBuffer
				BufferedReader br = new BufferedReader(new FileReader(file));
				while((temp = br.readLine()) != null ){
					content.append(temp);
				}
				br.close();//关闭bufferedreader
				String contentString = content.toString().toLowerCase();//将所有单词转为小写字母
				String[] words = contentString.split("[^A-Za-z]");//分割单词
				
				for(String word : words){
					if(wordCount.containsKey(word)){//如果单词在hashmap中存在，则词频+1
						int count = wordCount.get(word);
						count++;
						wordCount.put(word, count);
					}else{//如果为新词，则将其放入hashmap中，并将其词频设为1
						wordCount.put(word, 1);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	
		Map<String, Integer> sortByWordMap = new TreeMap<String, Integer>(wordCount);//将hashmap转为具有顺序的TreeMap
		SimpleDateFormat sdf = new SimpleDateFormat("HHmmSS");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fileDirectoryPath + File.separator + "wordCount" + sdf.format(new Date()) + ".txt"));
			for(Map.Entry<String, Integer> map : sortByWordMap.entrySet()){//将treemap中的单词以及词频写到文件中
				String result = map.getKey() + " " + map.getValue();
//				System.out.println(map.getKey() + " " + map.getValue());
				bw.write(result);
				bw.newLine();
				bw.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	public static void main(String[] args) {
		wordFeqCount("H:\\case");
	}
}
