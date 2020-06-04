package org.eclipse.jetty.demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class RecursiveThrowableFixTurboFilter extends TurboFilter
{
    public static void init()
    {
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.getLoggerContext().addTurboFilter(new RecursiveThrowableFixTurboFilter());
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable cause)
    {
        if (hasRecursiveThrowableReference(cause, null))
        {
            int locationAwareLoggerInteger = Level.toLocationAwareLoggerInteger(level);
            logger.log(marker, logger.getName(), locationAwareLoggerInteger, format, params, new SafeException(cause));
            return FilterReply.DENY;
        }

        return FilterReply.NEUTRAL;
    }

    private boolean hasRecursiveThrowableReference(Throwable cause, Set<Throwable> seen)
    {
        if (cause == null)
            return false;

        if (seen == null)
        {
            seen = new HashSet<>();
        }
        else if (!seen.add(cause))
        {
            return true;
        }

        for (Throwable suppressed : cause.getSuppressed())
        {
            if (hasRecursiveThrowableReference(suppressed, seen))
            {
                return true;
            }
        }
        return hasRecursiveThrowableReference(cause.getCause(), seen);
    }

    public static String toStackTrace(Throwable cause)
    {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw))
        {
            cause.printStackTrace(pw);
            return sw.toString();
        }
        catch (IOException e)
        {
            return "Unable to produce Stacktrace for " + cause;
        }
    }

    public static class SafeException extends RuntimeException
    {
        public SafeException(Throwable cause)
        {
            super(toStackTrace(cause));
        }
    }
}
