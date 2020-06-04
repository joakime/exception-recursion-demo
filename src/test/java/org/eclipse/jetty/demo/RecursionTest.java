package org.eclipse.jetty.demo;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

public class RecursionTest
{
    static
    {
        RecursiveThrowableFixTurboFilter.init();
    }

    @Test
    public void testCauseRecursion()
    {
        Exception e1 = new Exception();
        Exception e2 = new Exception();
        e2.initCause(e1);
        e1.initCause(e2);

        LoggerFactory.getLogger("testCauseRecursion").info("Oops", e1);
    }

    @Test
    public void testSuppressedRecursion()
    {
        Exception e1 = new Exception();
        Exception e2 = new Exception();
        e2.addSuppressed(e1);
        e1.addSuppressed(e2);

        LoggerFactory.getLogger("testSuppressedRecursion").info("Oops", e1);
    }
}
