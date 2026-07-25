// SPDX-License-Identifier: Unlicense
package hayanesuru;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

public class SimpleLogger implements Logger {
    StringBuffer cache = new StringBuffer(1024);
    private static final String TRACE = "[TRACE] ";
    private static final String DEBUG = "[DEBUG] ";
    private static final String INFO = "[INFO] ";
    private static final String WARN = "[WARN] ";
    private static final String ERROR = "[ERROR] ";

    private void append(String a, String b) {
        synchronized (this) {
            if (a != null) cache.append(a);
            if (b != null) cache.append(b);
            cache.append('\n');
        }
    }

    public void logThrowable(Throwable throwable) {
        if (throwable != null) {
            synchronized (this) {
                this.flush();
                throwable.printStackTrace(System.err);
            }
        }
    }

    public void flush() {
        synchronized (this) {
            if (cache.isEmpty()) {
                return;
            }
            System.out.print(cache);
            cache.setLength(0);
        }
    }

    @Override
    public String getName() {
        return "SimpleLogger";
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(final String msg) {
        append(TRACE, msg);
    }

    @Override
    public void trace(final String format, final Object arg) {
        var x = MessageFormatter.format(format, arg);
        append(TRACE, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        var x = MessageFormatter.format(format, arg1, arg2);
        append(TRACE, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    @Override
    public void trace(final String format, final Object... arguments) {
        var x = MessageFormatter.arrayFormat(format, arguments);
        append(TRACE, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    @Override
    public void trace(final String msg, final Throwable t) {
        append(TRACE, msg);
        this.logThrowable(t);
    }

    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return true;
    }

    @Override
    public void trace(final Marker marker, final String msg) {
        this.trace(msg);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg) {
        this.trace(format, arg);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        this.trace(format, arg1, arg2);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object... argArray) {
        this.trace(format, argArray);
    }

    @Override
    public void trace(final Marker marker, final String msg, final Throwable t) {
        this.trace(msg, t);
    }

    public boolean isDebugEnabled() {
        return false;
    }

    public void debug(String message) {
        append(DEBUG, message);
    }

    @Override
    public void debug(final String format, final Object arg) {
        var x = MessageFormatter.format(format, arg);
        append(DEBUG, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        var x = MessageFormatter.format(format, arg1, arg2);
        append(DEBUG, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    public void info(String message) {
        append(INFO, message);
    }

    @Override
    public void info(final String format, final Object arg) {
        var x = MessageFormatter.format(format, arg);
        append(INFO, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        var x = MessageFormatter.format(format, arg1, arg2);
        append(INFO, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    public void warn(String message) {
        append(WARN, message);
    }

    @Override
    public void warn(final String format, final Object arg) {
        var x = MessageFormatter.format(format, arg);
        append(WARN, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    public void error(String message) {
        append(ERROR, message);
    }

    @Override
    public void error(final String format, final Object arg) {
        var x = MessageFormatter.format(format, arg);
        append(ERROR, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        var x = MessageFormatter.format(format, arg1, arg2);
        append(ERROR, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    public void debug(String message, Object... args) {
        var x = MessageFormatter.arrayFormat(message, args);
        append(DEBUG, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    @Override
    public void debug(final String msg, final Throwable t) {
        append(DEBUG, msg);
        this.logThrowable(t);
    }

    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return true;
    }

    @Override
    public void debug(final Marker marker, final String msg) {
        this.debug(msg);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg) {
        this.debug(format, arg);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        this.debug(format, arg1, arg2);
    }

    public void debug(Marker marker, String message, Object... args) {
        var x = MessageFormatter.arrayFormat(message, args);
        append(DEBUG, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    @Override
    public void debug(final Marker marker, final String msg, final Throwable t) {
        this.debug(msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    public void info(String message, Object... args) {
        var x = MessageFormatter.arrayFormat(message, args);
        append(INFO, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    @Override
    public void info(final String msg, final Throwable t) {
        append(INFO, msg);
        this.logThrowable(t);
    }

    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return true;
    }

    @Override
    public void info(final Marker marker, final String msg) {
        this.info(msg);
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg) {
        this.info(format, arg);
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        this.info(format, arg1, arg2);
    }

    @Override
    public void info(final Marker marker, final String format, final Object... arguments) {
        this.info(format, arguments);
    }

    @Override
    public void info(final Marker marker, final String msg, final Throwable t) {
        this.info(msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    public void warn(String message, Object... args) {
        var x = MessageFormatter.arrayFormat(message, args);
        append(WARN, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        var x = MessageFormatter.format(format, arg1, arg2);
        append(WARN, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    @Override
    public void warn(final String msg, final Throwable t) {
        append(WARN, msg);
        this.logThrowable(t);
    }

    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return true;
    }

    @Override
    public void warn(final Marker marker, final String msg) {
        this.warn(msg);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg) {
        this.warn(format, arg);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        this.warn(format, arg1, arg2);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object... arguments) {
        this.warn(format, arguments);
    }

    @Override
    public void warn(final Marker marker, final String msg, final Throwable t) {
        this.warn(msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    public void error(String message, Object... args) {
        var x = MessageFormatter.arrayFormat(message, args);
        append(ERROR, x.getMessage());
        this.logThrowable(x.getThrowable());
    }

    @Override
    public void error(final String msg, final Throwable t) {
        append(WARN, msg);
        this.logThrowable(t);
    }

    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return true;
    }

    @Override
    public void error(final Marker marker, final String msg) {
        this.error(msg);
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg) {
        this.error(format, arg);
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        this.error(format, arg1, arg2);
    }

    public void error(Marker marker, String message, Object... args) {
        this.error(message, args);
    }

    @Override
    public void error(final Marker marker, final String msg, final Throwable t) {
        this.error(msg, t);
    }
}
