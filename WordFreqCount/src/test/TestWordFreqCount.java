package test;

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
