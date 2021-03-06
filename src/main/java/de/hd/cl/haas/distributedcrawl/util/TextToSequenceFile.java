package de.hd.cl.haas.distributedcrawl.util;

import de.hd.cl.haas.distributedcrawl.common.URLText;
import de.hd.cl.haas.distributedcrawl.common.WebDBURL;
import de.hd.cl.haas.distributedcrawl.common.WebDBURLList;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;

/**
 *
 * Converts a seed text file to a SequenceFile.
 *
 * This is used to construct an initial WebDB from user-supplied seed URLs.
 *
 * The seed.txt file contains list of host names and associated URLs, one host
 * name per line. Entries on a given line are delimited by the TAB character.
 *
 * @author Michael Haas <haas@cl.uni-heidelberg.de>
 */
public class TextToSequenceFile {

    private static void printFileNotExist() {
        System.err.println("File does not exist");
    }

    private static void printUsage() {
        System.err.println("Please provide seed text file as first argument.");
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {

        if (args.length < 1) {
            TextToSequenceFile.printUsage();
            System.exit(1);
        }

        String in = args[0];
        File inFile = new File(in);
        if (!inFile.exists()) {
            TextToSequenceFile.printFileNotExist();
            TextToSequenceFile.printUsage();
            System.exit(2);
        }

        Configuration conf = new Configuration();
        String curDir = System.getProperty("user.dir");
        String out = "file://" + curDir + "/webdb.dat";

        FileSystem fs = FileSystem.get(URI.create(out), conf);
        Path path = new Path(out);

        SequenceFile.Writer w = SequenceFile.createWriter(fs, conf, path, URLText.class, WebDBURLList.class);
        BufferedReader r = new BufferedReader(new FileReader(inFile));
        String line;

        while ((line = r.readLine()) != null) {
            ArrayList<WebDBURL> urlList = new ArrayList<WebDBURL>();
            String[] tokens = line.split("\t");
            String domain = tokens[0];
            for (int ii = 1; ii < tokens.length; ii++) {
                String url = tokens[ii];
                urlList.add(new WebDBURL(new URLText(url), 0));
            }
            WebDBURLList list = new WebDBURLList();
            list.fromCollection(urlList);
            w.append(new URLText(domain), list);
        }
        System.err.println("Finished processing file.");
        w.close();
    }
}
