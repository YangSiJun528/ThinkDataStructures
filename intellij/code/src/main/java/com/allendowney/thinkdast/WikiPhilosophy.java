package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class WikiPhilosophy {

    final static List<String> visited = new ArrayList<String>();
    final static WikiFetcher wf = new WikiFetcher();

    /**
     * Tests a conjecture about Wikipedia and Philosophy.
     *
     * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
     *
     * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String destination = "https://en.wikipedia.org/wiki/Philosophy";
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";

        testConjecture(destination, source, 10);
    }

    /**
     * Starts from given URL and follows first link until it finds the destination or exceeds the limit.
     *
     * @param destination
     * @param source
     * @throws IOException
     */
    public static void testConjecture(String destination, String source, int limit) throws IOException {
        // download and parse the document
        String url = source;

        for (int i = 0; i<limit; i++){

//            System.out.println("URL : "+url);
//            System.out.println("Visited : "+visited);

            if (visited.contains(url)) {
                throw new RuntimeException("FAIL : visited url");
            }
            visited.add(url);
            Element e = getFirstValidLink(url);
            if (e == null) {
                throw new RuntimeException("FAIL : no link");
            }
            url = "https://en.wikipedia.org" + e.attr("href");
            if(url.equals(destination)) {
                System.out.println("SUCCESS : find destination");
                break;
            }
        }
    }

    public static Element getFirstValidLink(String url) throws IOException {
        Elements paragraphs = wf.fetchWikipedia(url);
        WikiParser wp = new WikiParser(paragraphs);
        return wp.findFirstLink();
    }
}
