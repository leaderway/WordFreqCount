# 统计大文件中英文单词词频
##目录
* [思路说明](#思路说明)
* [代码说明](#代码说明)
	* [SplitFileUtil类](#SplitFileUtil类)
	* [WordFreqCountUtil类](#WordFreqCountUtil类)
* [使用样例](#使用样例)
<a name="思路说明"></a>
##思路说明
* 由于文件的大小比较大，如果采用一次性读取整个文件的做法会导致内存溢出，因此可将大文件按照指定的大小切割为若干个相对较小的文件，再依次使用切割后的文件进行词频统计。

* 在词频统计时可以使用**HashMap**，将单词作为key，词频作为value。统计完所有词频后，再将无序的**HashMap**转换为有序的**TreeMap**，即可实现按照key排序的功能。

<a name="代码说明"></a>
##代码说明
* 获取代码：github项目主页：https://github.com/leaderway/WordFreqCount

* 本程序重点写了两个工具类：

1. **SplitFileUtil**：将大文件分割成若干个小文件的工具类
2. **WordFreqCountUtil**：统计英文词频的工具类

<a name="SplitFileUtil类"></a>
###SplitFileUtil类
调用**splitFile(String filePath, long chunkSize)**方法，传入大文件路径以及每个块的大小，即可实现大文件切割。

	import java.io.BufferedReader;
	import java.io.BufferedWriter;
	import java.io.File;
	import java.io.FileOutputStream;
	import java.io.FileReader;
	import java.io.FileWriter;
	import java.io.RandomAccessFile;
	import java.text.SimpleDateFormat;
	import java.util.Date;
	
	/**
	 * 分割大文件的工具类
	 * 
	 * @author leaderway
	 *
	 */
	public class SplitFileUtil {
	
	private static String fileName;//待分割文件名
	private static long fileSize;//待分割文件大小
	private static long chunkNum;//分割的块数
	
	/**
	 * 获取待分割文件的文件名和文件大小
	 * 
	 * @param filePath待分割文件路径
	 */
	public static void getAttribute(String filePath){
		File file = new File(filePath);
		fileName = file.getName();
		fileSize = file.length();
	}
	
	/**
	 * 获取分割的块数
	 * 
	 * @param chunkSize 单个分割文件大小
	 * @return 
	 */
	public static void getChunkNum(long chunkSize){
		if(fileSize <= chunkSize){//如果待分割文件大小<=单个分割文件大小，则返回1
			chunkNum = 1;
		}else{
			if(fileSize % chunkSize > 0){//分割的最后一个文件大小小于chunkSize
				chunkNum = fileSize / chunkSize + 1;
			}else{
				chunkNum = fileSize / chunkSize;
			}
		}
	}
	
	/**
	 * 获取当前块的分割文件保存路径
	 * 
	 * @param filePath 原文件路径
	 * @return 块的分割文件夹路径
	 */
	public static String getChunkSaveDirectory(String filePath){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String directoryPath = filePath.substring(0, filePath.lastIndexOf("\\")) + File.separator + sdf.format(new Date());
		File directory = new File(directoryPath);
		if(!directory.exists()) directory.mkdir();//如果文件夹不存在，则创建
		return  directoryPath;
	}
	
	/**
	 * 获取当前块的分割文件保存路径
	 * 
	 * @param filePath 原文件路径
	 * @param currentChunk 当前块的序号
	 * @return 块的分割文件保存路径
	 */
	public static String getChunkSavePath(String filePath, int currentChunk){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String directoryPath = getChunkSaveDirectory(filePath);
		return  directoryPath + File.separator + "part" + currentChunk;
	}
	
	/**
	 * 将块写入文件中
	 * 
	 * @param filePath 原文件路径
	 * @param chunkSavePath 块保存路径
	 * @param chunkSize 块大小
	 * @param readPos 随机读取文件读取指针
	 * @return true：文件写入成功； false：文件写入失败
	 */
	public static boolean writeChunkFile(String filePath, String chunkSavePath, long chunkSize, long readPos){
		RandomAccessFile raf = null;
		FileOutputStream fos = null;
		byte[] b = new byte[1024];//准备空间读取文件内容
		long writtenSize = 0;//已经写入分割文件的字节数
		int len = 0;//本次读取的字节数
		try {
			raf = new RandomAccessFile(filePath, "r");
			raf.seek(readPos);//设置读取指针
			fos = new FileOutputStream(chunkSavePath);
			
			while((len = raf.read(b)) > 0 && writtenSize <= chunkSize){//原文件还没有读取完毕并且当前分割文件还没有写满
					writtenSize += len;
					if(writtenSize <= chunkSize){//如果完全写入本次读取的字节后分割文件还未写满，则将本次读取的字节完全写入
						fos.write(b, 0, len);
					}else{
						len = len - (int)(writtenSize - chunkSize);
						fos.write(b, 0, len);
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally{
			try{//关闭流
				if(fos != null) fos.close();
				if(raf != null) raf.close();
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 分割大文件的方法
	 * 
	 * @param filePath 原文件路径
	 * @param chunkSize 每个分割后的文件大小
	 * @return
	 */
	public static boolean splitFile(String filePath, long chunkSize){
		getAttribute(filePath);//获取原文件文件名及文件大小
		getChunkNum(chunkSize);//获取分块数
		if(chunkNum == 1) chunkSize = fileSize;//如果只有一块，则直接写入分割文件
		long writtenSize = 0;//单次写入的字节数
		long writtenTotal = 0;//总写入字节数
		String chunkSavePath = null;//单次分割文件保存路径
		for(int i = 1; i <= chunkNum; i++ ){
			if(i < chunkNum){//如果不是最后一个块，则单次写入的字节数与分块大小相等
				writtenSize = chunkSize;
			}else{//如果是最后一个块，则单次写入的字节数为剩余的未写入文件大小
				writtenSize = fileSize - writtenTotal;
			}
			
			if(chunkNum == 1){
				chunkSavePath = filePath + ".bak";
			}else{
				chunkSavePath = getChunkSavePath(filePath, i);
			}
			
			//保存分割文件
			if(!writeChunkFile(filePath, chunkSavePath, writtenSize, writtenTotal)) return false;
			writtenTotal += writtenSize;
		}
		return true;
	}

<a name="WordFreqCountUtil类"></a>
###WordFreqCountUtil类
调用**wordFeqCount(String fileDirectoryPath)**方法，传入保存切割后的文件的文件夹，即可在该文件夹下生成英语词频文件

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

<a name="使用样例"></a>
##使用样例
###样例代码
	import utils.SplitFileUtil;
	import utils.WordFreqCountUtil;
	
	public class TestWordFreqCount {
	public static void main(String[] args) {
		String filePath = "H:\\case\\1.txttest";//原文件位置
		long chunkSize = 1024*1024*20;//单个文件大小20M
		
		System.out.println("==========开始分割文件=========");
		SplitFileUtil.splitFile(filePath, chunkSize);
		System.out.println("==========分割文件完成=========");
		String fileDirectoryPath = SplitFileUtil.getChunkSaveDirectory(filePath);//获取分割后文件所处的文件夹
		System.out.println("==========词频统计开始=========");
		WordFreqCountUtil.wordFeqCount(fileDirectoryPath);//开始统计英文单词词频
		System.out.println("==========词频统计结束=========");
		}
	}

###样例代码运行效果
![运行效果](http://i.imgur.com/sDGfn2H.png)