package com.heng.lucene.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import org.wltea.analyzer.lucene.IKAnalyzer;

import com.heng.lucene.dto.DocDto;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service("indexService")
public class IndexServiceImpl implements IndexService {
	private String lunceneIndexPath;

	private String lunceneDataPath;
	@Autowired
	public IndexServiceImpl(Environment ev) {
		lunceneIndexPath=ev.getProperty("luncene.index-path");
		lunceneDataPath=ev.getProperty("luncene.data-path");
		this.createIndex(lunceneDataPath);
	}
	private static StringBuffer txt2String(File file) {
		StringBuffer sb = new StringBuffer();
		BufferedReader reader = null;
		try {
			  String code = getFilecharset(file);  
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), code));
			String str;
			while ((str = reader.readLine()) != null) {
				sb.append(str);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return sb;

	}

	public boolean createIndex(String path) {
		Date date1 = new Date();
		// 此处仅测试 目录下全部是文件
		List<File> fileList = Arrays.asList(new File(path).listFiles());

		File indexFile = new File(lunceneIndexPath);
		// 避免重复索引
		if (indexFile.exists()) {

			deleteDir(indexFile);
		} else {
			indexFile.mkdirs();
		}
		Analyzer analyzer = null;
		Directory directory = null;
		IndexWriter indexWriter = null;

		for (File file : fileList) {
			String	content = "";
			// 获取文件后缀,只对.doc和.txt文件建索引
			String type = file.getName().substring(file.getName().lastIndexOf(".") + 1);
			if ("txt".equalsIgnoreCase(type)) {
				content += txt2String(file);
			}

			try {

				// 使用第三方中文分词器IKAnalyzer
				analyzer = new IKAnalyzer(true);
				directory = FSDirectory.open(indexFile.toPath());
				IndexWriterConfig config = new IndexWriterConfig(analyzer);
				
				indexWriter = new IndexWriter(directory, config);
				Document document = new Document();
				document.add(new TextField("filename", file.getName(), Field.Store.YES));
				document.add(new TextField("content", content, Field.Store.YES));
				document.add(new TextField("path", file.getPath(), Field.Store.YES));
				indexWriter.addDocument(document);
				indexWriter.commit();
				indexWriter.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Date date2 = new Date();
		System.out.println("创建索引-----耗时：" + (date2.getTime() - date1.getTime()) + "ms\n");
		return false;
	}

	public List<DocDto> searchIndex(String queryStr) {
		Analyzer analyzer = null;
		Directory directory = null;
		String prefixHTML = "<font color='red'>";
		String suffixHTML = "</font>";
		List<DocDto> docDtoList = new ArrayList<>();
		try {
			directory = FSDirectory.open(new File(lunceneIndexPath).toPath());
			analyzer = new IKAnalyzer(true);
			DirectoryReader ireader = DirectoryReader.open(directory);
			IndexSearcher isearcher = new IndexSearcher(ireader);

			QueryParser parser = new QueryParser("content", analyzer);
			Query query = parser.parse(queryStr);
			ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs;
			// ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs;

			for (int i = 0; i < hits.length; i++) {
				DocDto docDto = new DocDto();
				Document hitDoc = isearcher.doc(hits[i].doc);
				// 自动摘要，查询关键词高亮
				SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter(prefixHTML, suffixHTML);
				Highlighter highlighter = new Highlighter(simpleHTMLFormatter, new QueryScorer(query));
				String highLightText = highlighter.getBestFragment(analyzer, "content", hitDoc.get("content"));

				docDto.setDocName(hitDoc.get("filename"));
				String path = hitDoc.get("path");
				path = path.replaceAll("\\\\", "/");
				docDto.setDocPath(path);
				docDto.setDocAbstract(highLightText + "...");
				docDtoList.add(docDto);
			}
			ireader.close();
			directory.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return docDtoList;
	}
	
	  /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
    
    //判断编码格式方法
    private static  String getFilecharset(File sourceFile) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1) {
                return charset; //文件编码为 ANSI
            } else if (first3Bytes[0] == (byte) 0xFF
                    && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE"; //文件编码为 Unicode
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE"; //文件编码为 Unicode big endian
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8"; //文件编码为 UTF-8
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80
                            // - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }
}