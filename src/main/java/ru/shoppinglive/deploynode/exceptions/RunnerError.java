package ru.shoppinglive.deploynode.exceptions;

public class RunnerError extends RuntimeException {
    public String reason;

    public RunnerError(String reason) {
        this.reason = reason;
    }

    public RunnerError(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public RunnerError(String message, Throwable cause, String reason) {
        super(message, cause);
        this.reason = reason;
    }

    public RunnerError(Throwable cause, String reason) {
        super(cause);
        this.reason = reason;
    }

    public RunnerError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String reason) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.reason = reason;
    }
}
