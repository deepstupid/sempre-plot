package semexe.freebase.test;

import semexe.freebase.BinaryLexicon;
import semexe.freebase.UnaryLexicon;
import semexe.freebase.lexicons.EntrySource;
import semexe.basic.LogInfo;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import semexe.freebase.lexicons.LexicalEntry;

import java.io.IOException;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

public class LexiconTest {
    @Test
    public void unary() throws IOException {
        UnaryLexicon.opts.unaryLexiconFilePath = "unaryInfoStringAndAlignment.txt";
        UnaryLexicon unary = UnaryLexicon.getInstance();
        boolean existsAlignment = false, existsStringMatch = false;
        double popularity = 0.0;
        double intersection = 0.0;

        List<LexicalEntry.UnaryLexicalEntry> entries = unary.lookupEntries("continent");
        LogInfo.logs("Num of unary entries for 'continent': %s", entries.size());
        for (LexicalEntry.UnaryLexicalEntry entry : entries) {
            if (entry.formula.toString().equals("(fb:type.object.type fb:location.continent)")) {
                if (entry.source == EntrySource.ALIGNMENT) {
                    existsAlignment = true;
                    intersection = entry.alignmentScores.get(UnaryLexicon.INTERSECTION);
                } else if (entry.source == EntrySource.STRING_MATCH) {
                    existsStringMatch = true;
                    popularity = entry.popularity;
                }
            }
        }
        assertEquals(true, existsAlignment);
        assertEquals(true, existsStringMatch);
        assertEquals(7.0, popularity, 0.0001);
        assertEquals(5.0, intersection, 0.0001);

        existsAlignment = false;
        existsStringMatch = false;
        popularity = 0.0;
        intersection = 0.0;
        entries = unary.lookupEntries("lawyer");
        LogInfo.logs("Num of unary entries for 'lawyer': %s", entries.size());
        for (LexicalEntry.UnaryLexicalEntry entry : entries) {
            if (entry.formula.toString().equals("(fb:people.person.profession fb:en.attorney)")) {
                if (entry.source == EntrySource.ALIGNMENT) {
                    existsAlignment = true;
                    intersection = entry.alignmentScores.get(UnaryLexicon.INTERSECTION);
                } else if (entry.source == EntrySource.STRING_MATCH) {
                    existsStringMatch = true;
                    popularity = entry.popularity;
                }
            }
        }
        assertEquals(true, existsAlignment);
        assertEquals(true, existsStringMatch);
        assertEquals(12282.0, popularity, 0.0001);  // Based on 93.exec (full Freebase)
        assertEquals(26.0, intersection, 0.0001);
    }

    @Test
    public void binary() throws IOException {
        BinaryLexicon.opts.binaryLexiconFilesPath = "binaryInfoStringAndAlignment.txt";
        BinaryLexicon.opts.keyToSortBy = BinaryLexicon.INTERSECTION;

        BinaryLexicon lexicon = BinaryLexicon.getInstance();
        List<LexicalEntry.BinaryLexicalEntry> entries = lexicon.lookupEntries("bear in");
        LogInfo.logs("Num of binary entries for 'bear in': %s", entries.size());
        LexicalEntry.BinaryLexicalEntry top = entries.get(0);
        assertEquals("people born here", top.fbDescriptions.iterator().next());
        AssertJUnit.assertEquals("!fb:location.location.people_born_here", top.formula.toString());
        assertEquals("ALIGNMENT", top.source.toString());
        assertEquals(759773.0, top.popularity, 0.00001);  // Based on 93.exec (full Freebase)
        assertEquals("fb:people.person", top.expectedType1);
        assertEquals("fb:location.location", top.expectedType2);
        assertEquals(16184.0, top.alignmentScores.get("FB_typed_size"), 0.0001);
        assertEquals(13856.0, top.alignmentScores.get("Intersection_size_typed"), 0.0001);
        assertEquals(15765.0, top.alignmentScores.get("NL_typed_size"), 0.0001);
    }
}
