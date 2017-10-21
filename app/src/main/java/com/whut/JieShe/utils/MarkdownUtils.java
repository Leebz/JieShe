package com.whut.JieShe.utils;


import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.markdown4j.Markdown4jProcessor;

import java.io.IOException;

public class MarkdownUtils {
    public static String[] getConciseMarkdown(String md) {
        if(TextUtils.isEmpty(md)){
            return null;
        }
        StringBuffer sb = new StringBuffer();
        String html = null;
        try {
            html = new Markdown4jProcessor().process(md);
            Document doc = Jsoup.parse(html);
            String url = getImgUrl(doc);
            doc = removeImgNode(doc);
            doc = modifyCodeBlock(doc);
            Elements eles = doc.getElementsByTag("body");
            Element body = eles.get(0);
            eles = body.children();
            for (Element ele : eles) {
                String temp = ele.text();
                sb.append(temp);
                if (sb.length() > 100) {
                    sb.append("...");
                    break;
                }
            }
            return new String[]{url,sb.toString()};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * 拼接图片的md格式的链接
     */
    public static String getImgUrl(Document doc){
        Elements imgs = doc.select("img[src]");
        if(imgs.size()==0){
            return "";
        }
        Element img = imgs.get(0);//提取首个图片的链接
        String src = img.attr("src");
        return src;
    }
    /*
     * 删除所有img格式的节点
     */
    public static Document removeImgNode(Document doc){

        doc.select("img[src]").remove();
        return doc;
    }
    /*
     * 解析code段
     */
    public static Document modifyCodeBlock(Document doc){
        Elements eles = doc.select("code");
        for(Element ele:eles){
            Element parentNode = ele.parent();
            String text = ele.text();
            ele.remove();
            parentNode.text(text);
        }

        return doc;
    }
    /*
     * 字符串长度调整
     */
    public static String modifyRes(String str){

        str = str.substring(0, 100);
        return str;
    }
}
