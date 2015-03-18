/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.project.intersectionbib;

import com.ironiacorp.computer.ComputerSystem;
import com.ironiacorp.computer.Filesystem;
import com.ironiacorp.computer.OperationalSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.imports.BibtexParser;
import net.sf.jabref.imports.ParserResult;

/**
 *
 * @author mariane
 */
public class IntersectionBib {

    private static final int maxCharacters = 2000;

    private static final String FILENAME_EXTENSION = ".bib";


    private BibtexDatabase databaseDefault; // base que esta no padrão.
    private BibtexDatabase database2;

    private File inputFileDefault;
    private InputStream inputStreamDefault;

    private File inputFile2;
    private InputStream inputStream2;

    private Charset encoding = Charset.defaultCharset();

    private BibtexDatabase databaseIntersection;

    public IntersectionBib(File filename1, File filename2) {
        setInputFile(filename1, filename2);
    }

    public void setInputFile(File file1, File file2) {
        String extension1;
        String extension2;

        if ((!file1.exists() || !file1.isFile()) && (!file2.exists() || !file2.isFile())) {
            throw new IllegalArgumentException("Invalid BibTeX file");
        }
        this.inputFileDefault = file1;
        this.inputFile2 = file2;

        OperationalSystem os = ComputerSystem.getCurrentOperationalSystem();
        Filesystem fs = os.getFilesystem();
        extension1 = fs.getExtension(file1);
        extension2 = fs.getExtension(file2);
    }

    public void readData() {
        ParserResult parseResult1;
        ParserResult parserResult2;

        Reader reader1 = null;
        Reader reader2 = null;

        try {
            if (inputFileDefault != null && inputFile2 != null) {
                reader1 = new InputStreamReader(new FileInputStream(inputFileDefault), encoding);
                reader2 = new InputStreamReader(new FileInputStream(inputFile2), encoding);
            }
            if (inputStreamDefault != null && inputFile2 != null) {
                reader1 = new InputStreamReader(inputStreamDefault, encoding);
                reader2 = new InputStreamReader(inputStream2, encoding);
            }
            parseResult1 = BibtexParser.parse(reader1);
            parserResult2 = BibtexParser.parse(reader2);

            databaseDefault = parseResult1.getDatabase();
            database2 = parserResult2.getDatabase();
        } catch (IOException e) {
            throw new RuntimeException("Error reading data", e);
        } finally {
            try {
                reader1.close();
                reader2.close();
            } catch (IOException e) {
            }
        }

    }

    /*verifica interseção entre as bases databaseDefault e database2*/
    public ArrayList<BibtexEntry> verifyIntersection() {
        ArrayList<BibtexEntry> dataIntersection = new ArrayList<BibtexEntry>();

        for (BibtexEntry entry : this.databaseDefault.getEntries()) {
            for (BibtexEntry elementDatabase2 : this.database2.getEntries()) {
                /*primeiro tenta verifica pelo doi
                  se não tiver doi, verifica por titulo, ano e páginas.
                  se não pegar pelo título (as vezes por ter apenas uma letra de diferente não pega pelo titulo)
                  então verifica por autor ano e páginas.
                */
                if (entry.getField("doi") != null && elementDatabase2.getField("doi") != null) {
                    if (verifyDOI(entry, elementDatabase2) == true) {
                        dataIntersection.add(entry);
                        System.out.println(entry.getField("title"));
                        break;
                    }
                } else if (entry.getField("title") != null && elementDatabase2.getField("title") != null) {
                    if (verifyTitleYearPages(entry, elementDatabase2) == true) {
                        dataIntersection.add(entry);
                        System.out.println(entry.getField("title"));
                        break;
                    } else if (entry.getField("author") != null && elementDatabase2.getField("author") != null) {
                        if (verifyAuthorYearPages(entry, elementDatabase2) == true) {
                            dataIntersection.add(entry);
                            System.out.println(entry.getField("title"));
                            break;
                        }
                    }
                }
            }
        }
        System.out.println("Total de artigos iguais entre as duas bases: " + dataIntersection.size());
        return dataIntersection;
    }

    public String cleanLongText(String text) {
        if (text == null) {
            return null;
        } else {
            text = text.replace('-', ' ');
            text = text.replace('(', ' ');
            text = text.replace(')', ' ');
            text = text.replace(": ", " ");
            text = text.replace(':', ' ');
            text = text.replace(", ", " ");
            text = text.replace(',', ' ');
            text = text.replace("; ", " ");
            text = text.replace(';', ' ');
            text = text.replace('*', ' ');
            text = text.replace('ç', ' ');
            text = text.replace("{", "");
            text = text.replace("}", "");
            text = text.substring(0, (text.length() > 100 ? 99 : text.length()));
            return text.trim();
        }
    }

    /*
        Pega o campo paginas e coloca em um vetor de duas posições.
    */
    private int[] pages(String field) {
        int[] pages = new int[2];
        if (field != null) {
            String p[] = new String[2];

            if (field.contains("--")) {
                p = field.split("--");
            } else if (field.contains("-")) {
                p = field.split("-");
            }
            // usando try/catch pois tem alguns campos das bases scopus que vem com letras no campo page.
            try{
                pages[0] = Integer.parseInt(p[0]);
                pages[1] = Integer.parseInt(p[1]);
            }catch(Exception e){
                return null;
            }
        return pages;
        }
       return pages;
    }

    private Boolean verifyDOI(BibtexEntry e1, BibtexEntry e2) {
        String doi1 = cleanLongText(e1.getField("doi"));
        String doi2 = cleanLongText(e2.getField("doi"));

        if (doi1.equalsIgnoreCase(doi2)) {
            return true;
        }
        return false;
    }

    private Boolean verifyTitleYearPages(BibtexEntry element1, BibtexEntry element2) {
        // element 1
        String title1 = cleanLongText(element1.getField("title")).toLowerCase();
        String y1 = cleanLongText(element1.getField("year")).toLowerCase();
        Integer year1 = Integer.parseInt(y1);
        String p1 = element1.getField("pages");
        int[] pages1 = pages(p1);
        
        // element 2
        String title2 = cleanLongText(element2.getField("title")).toLowerCase();
        String y2 = cleanLongText(element2.getField("year")).toLowerCase();
        Integer year2 = Integer.parseInt(y2);
        String p2 = element2.getField("pages");
        int[] pages2 = pages(p2);
        
        if(pages1 == null || pages2 == null){
            p1 = null;
            p2 = null;
        }

        /*considições para os elementos serem iguais:
         1 - title, year e pages iguais
         2 - title e year iguais
         3 - title e pages iguais
         Caso title1 e title2 sejam nulos retorna false.
         Se year1 é nulo e pages2 é nulo ou vice-versa retonra false.
        */
        if (y1 == null && p2 == null || y2 == null && p1 == null) {
            return false;
        } else if (p1 == null && p2 == null) { // conferindo por titulo e ano, com páginas sendo nulo.
            return title1.equalsIgnoreCase(title2) && year1.equals(year2);
        } else if (y1 == null && y2 == null) { // conferindo por título e páginas, com ano sendo nulo.
            return title1.equalsIgnoreCase(title2) && pages1[0] == pages2[0] && pages1[1] == pages2[1];
        } else if (title1.equals(title2) && year1.equals(year2) && pages1[0] == pages2[0] && pages1[1] == pages2[1]) {
            return true;
        }
        return false;
    }

    private boolean verifyAuthorYearPages(BibtexEntry element1, BibtexEntry element2) {
        // element 1
        String author1 = cleanLongText(element1.getField("author")).split(" ")[0].toLowerCase();
        String y1 = cleanLongText(element1.getField("year")).toLowerCase();
        Integer year1 = Integer.parseInt(y1);
        String p1 = element1.getField("pages");
        int[] pages1 = pages(p1);
        // element 2
        String author2 = cleanLongText(element2.getField("author")).split(" ")[0].toLowerCase();
        String y2 = cleanLongText(element2.getField("year")).toLowerCase();
        Integer year2 = Integer.parseInt(y2);
        String p2 = element2.getField("pages");
        int[] pages2 = pages(p2);
        
        if(pages1 == null || pages2 == null){
            p1 = null;
            p2 = null;
        }

        if (y1 == null && p2 == null || y2 == null && p1 == null) {
            return false;
        } else if (p1 == null && p2 == null) {
            return author1.equalsIgnoreCase(author2) && year1.equals(year2);
        } else if (y1 == null && y2 == null) {
            return author1.equalsIgnoreCase(author2) && pages1[0] == pages2[0] && pages1[1] == pages2[1];
        } else if (author1.equals(author2) && year1.equals(year2) && pages1[0] == pages2[0] && pages1[1] == pages2[1]) {
            return true;
        }
        return false;
    }
}
