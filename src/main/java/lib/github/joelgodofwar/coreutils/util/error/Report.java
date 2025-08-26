package lib.github.joelgodofwar.coreutils.util.error;

public class Report {
    private final ReportType reportType;
    private final Exception exception;

    private Report(ReportType reportType, Exception exception) {
        this.reportType = reportType;
        this.exception = exception;
    }

    public static Builder newBuilder(ReportType reportType) {
        return new Builder(reportType);
    }

    public String getMessage() {
        return reportType.getMessage();
    }

    public Exception getException() {
        return exception;
    }

    public static class Builder {
        private final ReportType reportType;
        private Exception exception;

        public Builder(ReportType reportType) {
            this.reportType = reportType;
        }

        public Builder error(Exception exception) {
            this.exception = exception;
            return this;
        }

        public Report build() {
            return new Report(reportType, exception);
        }
    }
}