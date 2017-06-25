package edu.stanford.nlp.sempre;

import fig.basic.Option;
import fig.basic.Utils;

/**
 * Preprocess the targetValue of an example.
 *
 * @author ppasupat
 */
public abstract class TargetValuePreprocessor {
    public static Options opts = new Options();
    private static TargetValuePreprocessor singleton;

    public static TargetValuePreprocessor getSingleton() {
        if (singleton == null) {
            if (opts.targetValuePreprocessor == null || opts.targetValuePreprocessor.isEmpty())
                singleton = new IdentityTargetValuePreprocessor();
            else
                singleton = (TargetValuePreprocessor) Utils.newInstanceHard(
                        SempreUtils.resolveClassName(opts.targetValuePreprocessor));
        }
        return singleton;
    }

    public static void setSingleton(TargetValuePreprocessor processor) {
        singleton = processor;
    }

    public abstract Value preprocess(Value value);

    public static class Options {
        @Option
        public String targetValuePreprocessor = null;
    }

}

class IdentityTargetValuePreprocessor extends TargetValuePreprocessor {
    public Value preprocess(Value value) {
        return value;
    }
}