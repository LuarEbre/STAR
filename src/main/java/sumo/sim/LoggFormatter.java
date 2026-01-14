package sumo.sim;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;


/**
 * Formats Logging Messages
 * @author simonr
 * @see StarLogger
 */
public class LoggFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        return  "["+ new Date(record.getMillis())+ "]" + "::"
                +record.getSourceClassName() + "::"
                +record.getSourceMethodName() + "::"
                +record.getMessage()+"\n";
    }
}
