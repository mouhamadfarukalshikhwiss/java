/**
*
* @author mouhamad faruk alsikh wiss / mouhamad.wiss@ogr.sakarya.edu.t
* @since   01/01/2024 ile   05/04/2024 arasindaki tarihler
* <p>
* bu sinifin icinde github tan okuma klonlam analiz islemler mevcutur 
* </p>
*/

package farok;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;

public class farok {

    public static void main(String[] args) throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("GitHub Depo URL giriniz: ");
        String githubLink = reader.readLine();

        String localPath = "C:/path/to/local/repository";
        		System.out.println("-----------------------------------------------------------");

        githubadresiniklonlam(githubLink, localPath);

        File dizi = new File(localPath);
        List<File> javaFiles = javadosyalar(dizi);

        for (File javaFile : javaFiles) {
            if (JAVADOSYAARAMA(javaFile)) {
            	System.out.println("Sinif: " + javaFile.getName());
            	System.out.println("Javadoc Satir Satisi: " +Docyorum(javaFile));
            	System.out.println("Yorum Satir Sayisi: " +yorumlar(javaFile));
            	System.out.println("kod Satir Sayisi: " +KodSatirSayisi(javaFile));
            	System.out.println("Loc : " +  analyzeLOC(javaFile));
            	System.out.println("Fonksiyon Sayisi: " +fonkSayisi(javaFile));
            	System.out.println("Yorum Sapma Yüzdesi:"+ yorumSapmaYuzdesi(Docyorum(javaFile),yorumlar(javaFile),fonkSayisi(javaFile),KodSatirSayisi(javaFile)));
            	System.out.println("------------------------------------------------------------");
            }
        }
        // Geçici dizini temizle
        gecicidizisilme(new File(localPath));

    }

    
    
    
    
    // gecici dizinin silmesi
    private static void gecicidizisilme(File directory) {
        if (!directory.exists()) {
            return;
        }
        File[] dosya = directory.listFiles();
        if (dosya != null) {
            for (File file : dosya) {
                if (file.isDirectory()) {
                	gecicidizisilme(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
    
    //Repository klonlanma sinifi 
    private static void githubadresiniklonlam(String githubLink, String localPath) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("git", "clone", githubLink, localPath);
        Process process = builder.start();
        process.waitFor();
       
    }

    private static List<File> javadosyalar(File directory) {
        List<File> javaFiles = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    javaFiles.addAll(javadosyalar(file));
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }

        return javaFiles;
    }

    private static boolean JAVADOSYAARAMA(File file) throws IOException {
        Pattern pattern = Pattern.compile("class\\s+\\w+");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (pattern.matcher(line).find()) {
                    return true;
                }
            }
        }
        return false;
    }

//javadoc sayisinin donduren sinif 
    public static int Docyorum(File file) throws IOException {
        int docsatir = 0;
        boolean inDoc = false;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (inDoc) {
                    if (line.endsWith("*/")) {
                        inDoc = false;
                    } else {
                    	docsatir++;
                    }
                } else {
                    if (line.startsWith("/**")) {
                        inDoc = true;
                    }
                }
            }
        }
      return docsatir;
   
       }

   
// yorum satirilari donduren sinif
    public static int yorumlar(File file) throws FileNotFoundException, IOException {
 
        int yorum = 0;
        String content = new String(Files.readAllBytes(file.toPath())); // Dosyayı oku

        // Tek ve çok satırlı yorumlar için desen
        Pattern commentPattern = Pattern.compile("//.|/\\(?!\\)[\\s\\S]?\\*/");
        Matcher commentMatcher = commentPattern.matcher(content);
        while (commentMatcher.find()) {
            yorum++;
        }

        return yorum;
    }
   
    
    
    
    
    
    
    
    
    
    
    //Kod satir sayisinin donduren sinif
    private static int KodSatirSayisi(File file) throws IOException {
        int sayac = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean inCommentBlock = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (inCommentBlock) {
                    if (line.contains("*/")) {
                        inCommentBlock = false;
                    }
                } else {
                    if (line.startsWith("//")) {
                        continue;
                    } else if (line.startsWith("/*")) {
                        if (!line.contains("*/")) {
                            inCommentBlock = true;
                        }
                        continue; 
                    } else if (line.isEmpty()) {
                        continue; 
                    }
                    sayac++;
                }
            }
        }
        return sayac;
    }
//loc(line of kod) donduren sinif
    private static int analyzeLOC(File file) throws IOException {
        int loc = 0,codeLines=0;

        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
                loc++;
                if (!line.trim().startsWith("//") && !line.trim().startsWith("/*") && !line.trim().isEmpty()) {
                    codeLines++;
                }
            }
        }
        return loc;
    }
    //fonkisyon sayisinin donduren sininf
    private static int fonkSayisi(File file) throws IOException {
        int sayac = 0;
        Pattern functionPattern = Pattern.compile("\\b(public|private|protected|static|final|abstract|synchronized)\\s+([\\w<>]+\\s+){0,2}(\\w+)\\s*\\([^)]*\\)\\s*(throws\\s+[\\w.,\\s]+)?\\s*\\{?");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher matcher = functionPattern.matcher(line);
                while (matcher.find()) {
                	sayac++;
                }
            }
        }
        return sayac;
    }

  
// yorum sapma yuzdesi hesaplamasi 
    private static double yorumSapmaYuzdesi(double doc,double yorum,double fonk,double kodSatir) {
    	
    	double YG=(((doc+yorum)*0.8)/fonk);
    	double YH=((kodSatir/fonk)*0.3);
    	double YorumSapma=((100*YG)/YH)-100;
    	
    	return YorumSapma;
 
    }
}
