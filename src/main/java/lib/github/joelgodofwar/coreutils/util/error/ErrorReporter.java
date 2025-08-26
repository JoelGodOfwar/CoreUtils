package lib.github.joelgodofwar.coreutils.util.error;

public interface ErrorReporter {
    void reportDetailed(Object context, Report report);
}