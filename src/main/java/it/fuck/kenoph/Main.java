package it.fuck.kenoph;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDStream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Arguments required: <input.pdf> <output.pdf> <watermark text> [auto-title]");
            return;
        }
        String path = args[0];
        String outPath = args[1];
        String watermark = args[2];
        boolean regex = watermark.startsWith("match:");
        if (regex) {
            watermark = watermark.substring("match:".length(), watermark.length());
        }

        boolean changeTitle = args.length == 4;

        String name = new File(path).getName();
        name = name.substring(0, name.lastIndexOf("."));

        System.out.println("Title: " + name);

        PDDocument doc = PDDocument.load(new File(path));
        doc.setAllSecurityToBeRemoved(true);
        if (doc.isEncrypted()) {
            System.out.println("Document is encrypted");
        }
        if (changeTitle) {
            PDDocumentInformation info = doc.getDocumentInformation();
            info.setTitle(name);
        }

        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        PDDocumentNameDictionary names = catalog.getNames();
        if (names != null) {
            names.setEmbeddedFiles(null);
            names.setJavascript(null);
        }

        for (int i=0; i<doc.getNumberOfPages(); i++) {
            cleanPage(doc, doc.getPage(i), watermark, regex);
        }

        doc.save(outPath);
        doc.close();
    }

    static void cleanPage(PDDocument doc, PDPage page, String watermark, boolean regex) throws IOException {
        page.setAnnotations(new ArrayList<>());

        PDFStreamParser parser = new PDFStreamParser(page);
        parser.parse();

        ArrayList<Object> tokens = new ArrayList<>(parser.getTokens());
        ArrayList<Integer> remove = new ArrayList<>();
        for (int i=0; i<tokens.size(); i++) {
            Object token = tokens.get(i);
             if (token instanceof COSName) {
            	COSName cosname = (COSName) token;
            	if(cosname.getName().startsWith("Fm")) {
            		 i = removeState(tokens, remove, i);
            	}
            } else if (token instanceof COSString) {
                String str = ((COSString) token).getString();

                if (regex) {
                    if (str.matches(watermark)) {
                        i = removeState(tokens, remove, i);
                    }
                } else if (str.contains(watermark)) {
                    i = removeState(tokens, remove, i);
                }
            }
        }

        remove.sort((o, t1) -> t1 - o);
        for (Integer i : remove) {
            tokens.remove((int) i);
        }

        PDStream newContent = new PDStream(doc);
        OutputStream out = newContent.createOutputStream();
        ContentStreamWriter writer = new ContentStreamWriter(out);
        writer.writeTokens(tokens);
        out.close();
        page.setContents(newContent);
    }

    static int removeState(ArrayList<Object> tokens, ArrayList<Integer> remove, int i) {
        remove.add(i);
        for (int j=i-1; j >= 0; j--) {
            Object token = tokens.get(j);
            remove.add(j);
            if (token instanceof Operator) {
                if (((Operator) token).getName().equals("q")) {
//                    System.out.println("q");
                    break;
                }
            }
        }
        for (int j=i+1; j < tokens.size(); j++) {
            i = j;
            Object token = tokens.get(j);
            remove.add(j);
            if (token instanceof Operator) {
                if (((Operator) token).getName().equals("Q")) {
//                    System.out.println("Q");
                    break;
                }
            }
        }

        return i + 1;
    }
}
