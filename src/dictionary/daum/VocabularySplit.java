package dictionary.daum;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class VocabularySplit {
    public static void main(String[] args) {
        Path path = Paths.get("E:/temp_dic", "영단어.txt");
        Charset charset = Charset.forName("UTF-8");
        
        try{
            List<String> lines = Files.readAllLines(path, charset);
            System.out.println(lines.size());
            for(String line : lines){
            	int hanPos = getFirstHanPosition(line);
            	if (  hanPos > -1 ) {
            		String eng = line.substring(0, hanPos - 1);
            		String han = line.substring(hanPos);
            		System.out.println( eng.trim().replaceAll("[-~]$", "") + " : " + han.trim());
            	} else {
            		System.out.println("_ERR : "  + line);
            	}
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
 
    public static  int getFirstHanPosition(String text) {
    	int pos = -1;
        int text_count = text.length();
     
        for (int i = 0; i < text_count; i++) {
            char oneWord = text.charAt(i);
     
            if  ( HANGUL_UNICODE_START <= oneWord && oneWord <= HANGUL_UNICODE_END ) {
                pos = i;
                break;
            }
        }
    
        return pos;
    }
 
    
    final static int HANGUL_UNICODE_START = 0xAC00;
    final static int HANGUL_UNICODE_END = 0xD7AF;
     
    static enum SYLLABLE_HANGUL
    {
        FULL_HANGUL, PART_HANGUL, NOT_HANGUL
    }
      
    public static SYLLABLE_HANGUL IsHangul(String text)
    {
        int text_count = text.length();
        SYLLABLE_HANGUL is_syllable_hangul;
     
        int is_hangul_count = 0;
     
        for (int i = 0; i < text_count; i++)
        {
            char syllable = text.charAt(i);
     
            if ((HANGUL_UNICODE_START <= syllable)
                    && (syllable <= HANGUL_UNICODE_END))
            {
                is_hangul_count++;
            }
     
        }
     
        if (is_hangul_count == text_count)
        {
            is_syllable_hangul = SYLLABLE_HANGUL.FULL_HANGUL;
     
        }
        else if (is_hangul_count == 0)
        {
            is_syllable_hangul = SYLLABLE_HANGUL.NOT_HANGUL;
        }
        else
        {
            is_syllable_hangul = SYLLABLE_HANGUL.PART_HANGUL;
        }
     
        return is_syllable_hangul;
    }
}
