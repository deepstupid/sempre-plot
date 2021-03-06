package semexe;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import semexe.basic.LispTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A simple type system for Formulas.  SemType represents a union over base
 * types, where each base type is either
 * - entity type, or
 * - entity type -> base type Example of a 0-ary (for booleans) (type bool)
 * <p>
 * Example of a unary (for Obama)
 * (union fb:government.politician fb:government.us_president ...)
 * <p>
 * Example of a binary (for born in) [remember, arg1 is the argument, arg0 is
 * the return type]
 * (-> fb:location.location fb:people.person)
 * <p>
 * Note: type equality is not implemented, since it's better to use meet() to
 * exploit the finer lattice structure of the type system.
 *
 * @author Percy Liang
 */
public abstract class SemType {
    // Common types
    public static final SemType topType = new TopSemType();
    public static final SemType bottomType = new UnionSemType();
    public static final SemType stringType = new AtomicSemType(CanonicalNames.TEXT);
    public static final SemType intType = new AtomicSemType(CanonicalNames.INT);
    public static final SemType floatType = new AtomicSemType(CanonicalNames.FLOAT);
    public static final SemType dateType = new AtomicSemType(CanonicalNames.DATE);
    public static final SemType timeType = new AtomicSemType(CanonicalNames.TIME);
    public static final SemType numberType = new AtomicSemType(CanonicalNames.NUMBER);
    public static final SemType numberOrDateType = new UnionSemType(numberType, dateType);
    public static final SemType entityType = new AtomicSemType(CanonicalNames.ENTITY);

    // Create a new instance of SemType from type names (Strings)
    public static final SemType anyType = new AtomicSemType(CanonicalNames.ANY);
    public static final FuncSemType topTopFunc = new FuncSemType(topType, topType);
    public static final FuncSemType anyAnyFunc = new FuncSemType(anyType, anyType);
    public static final FuncSemType compareFunc = new FuncSemType(numberOrDateType, numberOrDateType);

    @JsonCreator
    public static SemType fromString(String s) {
        return fromLispTree(LispTree.proto.tree(s));
    }

    public static SemType fromLispTree(LispTree tree) {
        if (tree.isLeaf()) {
            if (tree.value.equals("top")) return topType;
            return new AtomicSemType(tree.value);
        }
        if ("union".equals(tree.child(0).value)) {
            int n = tree.children.size();
            List<SemType> result = new ArrayList<>(n);
            for (int i = 1; i < n; i++)
                result.add(fromLispTree(tree.child(i)));
            return new UnionSemType(result);
        }
        if ("->".equals(tree.child(0).value)) {
            SemType result = fromLispTree(tree.child(tree.children.size() - 1));
            for (int i = tree.children.size() - 2; i >= 1; i--)
                result = new FuncSemType(fromLispTree(tree.child(i)), result);
            return result;
        }
        throw new RuntimeException("Invalid type: " + tree);
    }

    public static SemType newAtomicSemType(String type) {
        return new AtomicSemType(type);
    }

    public static SemType newFuncSemType(String argType, String retType) {
        return new FuncSemType(argType, retType);
    }

    public static SemType newUnionSemType(Collection<String> types) {
        List<SemType> t = new ArrayList<>(types.size());
        for (String x : types)
            t.add(new AtomicSemType(x));
        return new UnionSemType(t).simplify();
    }

    public static SemType newUnionSemType(String... types) {
        return newUnionSemType(Arrays.asList(types));
    }

    // Return whether the type is valid (not bottom).
    public abstract boolean isValid();

    // Return the meet of |this| and |that|.
    public abstract SemType meet(SemType that);

    // Return the reversed type for functions: (s -> t) to (t -> s)
    public abstract SemType reverse();

    // Treat |this| as a function type and apply it to the argument |that|.
    // This is just an internal function.
    public abstract SemType apply(SemType that);

    // These are really the primitives.
    public SemType getArgType() {
        return reverse().apply(SemType.topType);
    }

    public SemType getRetType() {
        return apply(SemType.topType);
    }

    public abstract LispTree toLispTree();

    @JsonValue
    @Override
    public String toString() {
        return toLispTree().toString();
    }
}
