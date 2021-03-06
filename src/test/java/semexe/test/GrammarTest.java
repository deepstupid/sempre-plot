package semexe.test;

import semexe.Grammar;
import semexe.Rule;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test that the grammar correctly parsers rules.
 */
public class GrammarTest {

    public static Grammar makeTernaryGrammar() {
        Grammar g = new Grammar();
        g.addStatement("(rule $ROOT ($X) (IdentityFn))");
        g.addStatement("(rule $X ($A $B $C) (IdentityFn))");
        g.addStatement("(rule $A (a) (ConstantFn (string a)))");
        g.addStatement("(rule $B (b) (ConstantFn (string b)))");
        g.addStatement("(rule $C (c) (ConstantFn (string c)))");
        return g;
    }

    /**
     * Checks that each rule is one of the following:
     * $Cat => token
     * $Cat => $Cat
     * $Cat => token token
     * $Cat => token $Cat
     * $Cat => $Cat token
     * $Cat => $Cat $Cat
     */
    public static boolean isValidBinaryGrammar(Grammar g) {
        List<Rule> r = g.getRules();
        return isValidBinaryGrammar(r);
    }

    private static boolean isValidBinaryGrammar(List<Rule> r) {
        for (Rule rule : r) {
            if (!Rule.isCat(rule.lhs))
                return false;
            if (rule.rhs.size() != 1 && rule.rhs.size() != 2)
                return false;
        }

        return true;
    }

    @Test
    public void testBinarizationOfTernaryGrammar() {

        Grammar.opts.binarizeRules = true;
        Grammar x = makeTernaryGrammar();
        System.out.println(x.toString());

        List<Rule> y = x.getRules();
        System.out.println(y.toString());

        assertTrue(isValidBinaryGrammar(x));

        assertEquals(6, y.size());

    }


}
