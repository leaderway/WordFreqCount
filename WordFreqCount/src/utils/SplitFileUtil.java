package utils;

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
	
	/**
	 * 生成测试用的大文件
	 * 
	 * @param filePath原文件路径
	 * @param count 数量
	 */
	public static void generateTestData(String filePath, int count){
		try {
			for(int i = 0; i < count; i++){
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			BufferedWriter bw = new BufferedWriter(new FileWriter(filePath+"test", true));
			String temp = null;
			while((temp = br.readLine()) != null){
				bw.write(temp);
			}
			}
			System.out.println("文件生成成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		String filePath = "H:\\case\\1.txttest";
		generateTestData(filePath,4);//生成测试文件
//		long chunkSize = 1024*1024;
//		System.out.println("======文件分割开始=======");
//		long beginTime = System.currentTimeMillis();
//		if(splitFile(filePath, chunkSize)){
//			System.out.println("分割文件成功");
//		}else{
//			System.out.println("分割文件失败");
//		}
//		long interval = System.currentTimeMillis() - beginTime;
//		System.out.println("======运行时间"+ interval);
	}
	
}
